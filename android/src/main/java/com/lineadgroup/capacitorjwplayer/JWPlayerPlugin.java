package com.lineadgroup.capacitorjwplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.events.EventListener;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@CapacitorPlugin(name = "JWPlayer",
  permissions = {
    @Permission(
      strings = {Manifest.permission.READ_EXTERNAL_STORAGE},
      alias = "storage"
    )
  })
public class JWPlayerPlugin extends Plugin {


  private JWPlayer implementation = new JWPlayer();
  private JWPlayerView jwPlayerView = null;
  private com.jwplayer.pub.api.JWPlayer mPlayer = null;
  private String JWPLAYER_KEY = "";
  private String GOOGLE_CAST_ID = "";
  private boolean toBack = false;
  private int containerViewId = 20;
  private boolean autostart;
  private boolean forceFullScreenOnLandscape;
  private boolean forceFullScreen;
  public static final String JWPLAYER_CHANGE_EVENT = "playerEvent";
  public static final String JWPLAYER_FULL_SCREEN_EVENT = "fullScreenPlayerEvent";
  public static final String JWPLAYER_READY_EVENT = "readyPlayerEvent";
  private CastContext mCastContext;

  @PluginMethod
  public void echo(PluginCall call) {
    String value = call.getString("value");

    JSObject ret = new JSObject();
    ret.put("value", implementation.echo(value));
    call.resolve(ret);
  }

  @PluginMethod()
  public void initialize(PluginCall call) {
    /*
     *  TODO: Check API key
     */
    this.JWPLAYER_KEY = call.getString("androidLicenseKey", "");
    if (TextUtils.isEmpty(this.JWPLAYER_KEY)) {
      call.reject("License key missing! You need to set up your JWPlayer license key");
      return;
    }

    LicenseUtil.setLicenseKey(getBridge().getContext(), this.JWPLAYER_KEY);

    call.resolve();
  }

  @PluginMethod()
  public void create(PluginCall call) {
    final JWPlayerPlugin ctx = this;
    getBridge().getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        try {
          final JSObject nativeConfiguration = call.getObject("nativeConfiguration", new JSObject());
          final String videoURL = nativeConfiguration.getString("file", "");
          final Integer width = nativeConfiguration.getInteger("width", 0);
          final Integer height = nativeConfiguration.getInteger("height", 0);
          final Integer x = nativeConfiguration.getInteger("x", 0);
          final Integer y = nativeConfiguration.getInteger("y", 0);
          GOOGLE_CAST_ID = nativeConfiguration.getString("googleCastId", "");
          toBack = !nativeConfiguration.getBoolean("front", false);
          autostart = nativeConfiguration.getBoolean("autostart", false);
          forceFullScreenOnLandscape = nativeConfiguration.getBoolean("forceFullScreenOnLandscape", false);
          forceFullScreen = nativeConfiguration.getBoolean("forceFullScreen", false);
          if (TextUtils.isEmpty(videoURL)) {
            call.reject("You have to provide a fileURL to playback");
            return;
          }
          List<Caption> captionTracks = new ArrayList<>();

          JSArray array = call.getArray("captions", null);
          if (array != null) {
            for (int i = 0; i < array.length(); i++) {
              JSONObject data = (JSONObject) array.get(i);
              Caption captionEn = new Caption.Builder()
                .file((String) data.get("file"))
                .label((String) data.get("label"))
                .kind(CaptionType.CAPTIONS)
                .isDefault((Boolean) data.get("default"))
                .build();
              captionTracks.add(captionEn);
            }
          }

          PlaylistItem playlistItem = new PlaylistItem.Builder()
            .file(videoURL)
            .tracks(captionTracks)
            .build();

          jwPlayerView = new JWPlayerView(getBridge().getContext());
          mPlayer = jwPlayerView.getPlayer();
          //Set view layout. Player will take the whole screen if no parameters are set.
          FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width == 0 ? FrameLayout.LayoutParams.WRAP_CONTENT : getScaledPixels(width), height == 0 ? FrameLayout.LayoutParams.WRAP_CONTENT : getScaledPixels(height));
          lp.topMargin = getScaledPixels(y);
          lp.leftMargin = getScaledPixels(x);

          jwPlayerView.setLayoutParams(lp);


          FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);
          if (containerView == null) {
            containerView = new FrameLayout(getActivity().getApplicationContext());
            containerView.setId(containerViewId);

            ((ViewGroup) getBridge().getWebView().getParent()).addView(containerView);
            if (toBack) {
              getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
              getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
            }
            containerView.addView(jwPlayerView);

          } else {
            call.reject("Player already started");
          }
          List<PlaylistItem> playlist = new ArrayList<>();
          playlist.add(playlistItem);
          PlayerConfig config = new PlayerConfig.Builder()
            .playlist(playlist)
            .autostart(false)
            .build();
          mPlayer.setup(config);
          mPlayer.addListener(EventType.READY, new VideoPlayerEvents.OnReadyListener() {
            @Override
            public void onReady(ReadyEvent readyEvent) {
              notifyListeners(JWPLAYER_READY_EVENT, getStatusJSObject(JWPLAYER_READY_EVENT, readyEvent));
              if (forceFullScreen) {
                mPlayer.setFullscreen(true, false);
              }
            }
          });
          mPlayer.addListener(EventType.FULLSCREEN, new VideoPlayerEvents.OnFullscreenListener() {
            @Override
            public void onFullscreen(FullscreenEvent fullscreenEvent) {
              notifyListeners(JWPLAYER_FULL_SCREEN_EVENT, getStatusJSObject(JWPLAYER_FULL_SCREEN_EVENT, fullscreenEvent.getFullscreen()));
            }
          });
//                jwPlayerView.getPlayerAsync(ctx);
        } catch (Exception e) {
          e.printStackTrace();
          call.reject(e.getMessage());
        }
      }
    });
    call.resolve();
  }

  @PluginMethod()
  public void addButton(final PluginCall call) {

  }

  @PluginMethod()
  public void addCuePoints(final PluginCall call){
  }

  @PluginMethod()
  public void remove(final PluginCall call) {
    bridge.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        CastContext castContext = CastContext.getSharedInstance(bridge.getActivity());
        SessionManager mSessionManager = castContext.getSessionManager();
        mSessionManager.endCurrentSession(true);
        FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);

        // allow orientation changes after closing camera:
        //getBridge().getActivity().setRequ estedOrientation(previousOrientationRequest);
        if (containerView != null) {
          mPlayer.stop();
          containerView.removeAllViews();
          ((ViewGroup) getBridge().getWebView().getParent()).removeView(containerView);
          getBridge().getWebView().setBackgroundColor(Color.WHITE);
          call.success();
        } else {
          call.reject("camera already stopped");
        }
      }
    });
  }


  @Override
  protected void handleOnConfigurationChanged(Configuration newConfig) {
    super.handleOnConfigurationChanged(newConfig);
    if (!this.forceFullScreen) {
      if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        if (mPlayer.getFullscreen()) {
          mPlayer.setFullscreen(false, true);
        }
      } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && this.forceFullScreenOnLandscape) {
        if (!mPlayer.getFullscreen()) {
          mPlayer.setFullscreen(true, true);
        }
      }
    }

  }

  @PluginMethod
  public void getPosition(PluginCall call) {
    JSObject ret = new JSObject();
    ret.put("value", mPlayer.getPosition());
    call.resolve(ret);
  }

  @PluginMethod
  public void seek(PluginCall call) {
    final Double position = call.getDouble("position", 0.0);
    mPlayer.play();
    mPlayer.addListener(EventType.PLAY, new VideoPlayerEvents.OnPlayListener() {
      @Override
      public void onPlay(PlayEvent playEvent) {
        mPlayer.seek(position);
        mPlayer.removeListener(EventType.PLAY, this);
      }
    });
  }

  public int getScaledPixels(float pixels) {
    // Get the screen's density scale
    final float scale = getBridge().getActivity().getResources().getDisplayMetrics().density;
    // Convert the dps to pixels, based on density scale
    return (int) (pixels * scale + 0.5f);
  }

  private JSObject getStatusJSObject(String name, Object data) {
    JSObject ret = new JSObject();
    ret.put("name", name);
    ret.put("data", data);
    return ret;
  }

}
