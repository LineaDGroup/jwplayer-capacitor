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
import com.jwplayer.pub.api.configuration.RelatedConfig;
import com.jwplayer.pub.api.events.CaptionsChangedEvent;
import com.jwplayer.pub.api.events.CaptionsListEvent;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.EventListener;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.IdleEvent;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.PlaylistCompleteEvent;
import com.jwplayer.pub.api.events.PlaylistItemEvent;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.markers.*;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

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
    private JWPlayerHandler jwPlayerHandler;


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
        LicenseUtil licenseUtil = new LicenseUtil();
        licenseUtil.setLicenseKey(getBridge().getContext(), this.JWPLAYER_KEY);
        this.jwPlayerHandler = new JWPlayerHandler();
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
                    final Integer width = nativeConfiguration.getInteger("width", 0);
                    final Integer height = nativeConfiguration.getInteger("height", 0);
                    final Integer x = nativeConfiguration.getInteger("x", 0);
                    final Integer y = nativeConfiguration.getInteger("y", 0);
                    GOOGLE_CAST_ID = nativeConfiguration.getString("googleCastId", "");
                    toBack = !nativeConfiguration.getBoolean("front", false);
                    autostart = nativeConfiguration.getBoolean("autostart", false);
                    forceFullScreenOnLandscape = nativeConfiguration.getBoolean("forceFullScreenOnLandscape", false);
                    forceFullScreen = nativeConfiguration.getBoolean("forceFullScreen", false);

                    List<PlaylistItem> playlist = new ArrayList<>();
                    JSONArray playList = nativeConfiguration.getJSONArray("playlist");
                    if (playList.length() > 0) {
                        for (int i = 0; i < playList.length(); i++) {
                            JSONObject data = (JSONObject) playList.get(i);
                            String videoURL = data.getString("file");
                            String title = data.getString("title");
                            String description = data.getString("title");
                            double starttime = data.has("starttime") ? data.getDouble("starttime") : 0;
                            List<Caption> captionTracks = new ArrayList<>();
                            if (data.has("captions")) {
                                JSONArray arrayCaptions = data.getJSONArray("captions");
                                if (arrayCaptions.length() > 0) {
                                    for (int j = 0; j < arrayCaptions.length(); j++) {
                                        JSONObject captionData = (JSONObject) arrayCaptions.get(i);
                                        Caption captionEn = new Caption.Builder()
                                                .file((String) captionData.get("file"))
                                                .label((String) captionData.get("label"))
                                                .kind(CaptionType.CAPTIONS)
                                                .isDefault((Boolean) captionData.get("default"))
                                                .build();
                                        captionTracks.add(captionEn);
                                    }
                                }
                            }
                            PlaylistItem playlistItem = new PlaylistItem.Builder()
                                    .file(videoURL)
                                    .title(title)
                                    .description(description)
                                    .startTime(starttime)
                                    .tracks(captionTracks)
                                    .build();
                            playlist.add(playlistItem);
                        }
                    }
                    jwPlayerView = new JWPlayerView(getBridge().getContext());
                    mPlayer = jwPlayerView.getPlayer();
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width == 0 ? FrameLayout.LayoutParams.MATCH_PARENT : getScaledPixels(width), height == 0 ? FrameLayout.LayoutParams.MATCH_PARENT : getScaledPixels(height));
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
                    PlayerConfig config = new PlayerConfig.Builder()
                            .playlist(playlist)
                            .autostart(autostart)
                            .build();
                    mPlayer.setup(config);
                    if (forceFullScreen) {
                        mPlayer.setFullscreen(true, false);
                    }
                    mPlayer.addListener(EventType.PLAY, jwPlayerHandler);
                    mPlayer.addListener(EventType.PAUSE, jwPlayerHandler);
                    mPlayer.addListener(EventType.IDLE, jwPlayerHandler);
                    mPlayer.addListener(EventType.COMPLETE, jwPlayerHandler);
                    mPlayer.addListener(EventType.TIME, jwPlayerHandler);
                    mPlayer.addListener(EventType.PLAYLIST_ITEM, jwPlayerHandler);
                    mPlayer.addListener(EventType.PLAYLIST_COMPLETE, jwPlayerHandler);
                    mPlayer.addListener(EventType.READY, jwPlayerHandler);
                    mPlayer.addListener(EventType.FULLSCREEN, jwPlayerHandler);
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
    public void addCuePoints(final PluginCall call) {
        try {
            JSArray cuePoints = call.getArray("cuePoints", null);
            if (cuePoints != null) {
                for (int i = 0; i < cuePoints.length(); i++) {
                    JSONObject cuePoint = (JSONObject) cuePoints.get(i);
                    String text = cuePoint.getString("text");
                    String begin = cuePoint.getString("begin");
                    CueMarker cueMarker = new CueMarker(begin, begin, text, CueMarker.CUE_TYPE_CHAPTERS);
                    //mPlayer.getConfig().
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }

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
                    mPlayer.removeListeners(jwPlayerHandler);
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
        if (mPlayer != null) {
            ret.put("value", mPlayer.getPosition());
        } else {
            ret.put("value", 0);
        }
        call.resolve(ret);
    }

    @PluginMethod
    public void seek(PluginCall call) {
        final Double position = call.getDouble("position", 0.0);
        mPlayer.addListener(EventType.PLAY, new VideoPlayerEvents.OnPlayListener() {
            @Override
            public void onPlay(PlayEvent playEvent) {
                mPlayer.seek(position);
                mPlayer.removeListener(EventType.PLAY, this);
            }
        });
        mPlayer.play();
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

    class JWPlayerHandler implements VideoPlayerEvents.OnPlaylistCompleteListener, VideoPlayerEvents.OnPlayListener, VideoPlayerEvents.OnPauseListener,
            VideoPlayerEvents.OnIdleListener, VideoPlayerEvents.OnCompleteListener, VideoPlayerEvents.OnTimeListener, VideoPlayerEvents.OnPlaylistItemListener,
            VideoPlayerEvents.OnReadyListener, VideoPlayerEvents.OnFullscreenListener, VideoPlayerEvents.OnCaptionsListListener, VideoPlayerEvents.OnCaptionsChangedListener {
        @Override
        public void onPlaylistComplete(PlaylistCompleteEvent playlistCompleteEvent) {

            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("playlistComplete", playlistCompleteEvent));
        }

        @Override
        public void onPlay(PlayEvent playEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("play", playEvent));
        }

        @Override
        public void onPause(PauseEvent pauseEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("pause", pauseEvent));
        }

        @Override
        public void onIdle(IdleEvent idleEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("idle", idleEvent));
        }

        @Override
        public void onComplete(CompleteEvent completeEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("complete", completeEvent));
        }

        @Override
        public void onTime(TimeEvent timeEvent) {
            JSObject ret = new JSObject();
            ret.put("position", timeEvent.getPosition());
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("time", ret));
        }

        @Override
        public void onPlaylistItem(PlaylistItemEvent playlistItemEvent) {
            JSObject ret = new JSObject();
            ret.put("index", playlistItemEvent.getIndex());
            ret.put("item", playlistItemEvent.getPlaylistItem());
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("playlistItem", ret));

        }

        @Override
        public void onReady(ReadyEvent readyEvent) {
            notifyListeners(JWPLAYER_READY_EVENT, getStatusJSObject(JWPLAYER_READY_EVENT, readyEvent));
        }

        @Override
        public void onFullscreen(FullscreenEvent fullscreenEvent) {
            notifyListeners(JWPLAYER_FULL_SCREEN_EVENT, getStatusJSObject(JWPLAYER_FULL_SCREEN_EVENT, fullscreenEvent.getFullscreen()));
        }

        @Override
        public void onCaptionsList(CaptionsListEvent captionsListEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("captionsList", captionsListEvent.getCaptions()));
        }

        @Override
        public void onCaptionsChanged(CaptionsChangedEvent captionsChangedEvent) {
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("captionsChanged", captionsChangedEvent));

        }
    }

}
