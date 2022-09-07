package com.lineadgroup.capacitorjwplayer;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

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
import com.jwplayer.pub.api.configuration.ads.AdvertisingConfig;
import com.jwplayer.pub.api.configuration.ads.VastAdvertisingConfig;
import com.jwplayer.pub.api.configuration.ads.ima.ImaAdvertisingConfig;
import com.jwplayer.pub.api.events.CaptionsChangedEvent;
import com.jwplayer.pub.api.events.CaptionsListEvent;
import com.jwplayer.pub.api.events.CompleteEvent;
import com.jwplayer.pub.api.events.ErrorEvent;
import com.jwplayer.pub.api.events.EventListener;
import com.jwplayer.pub.api.events.EventType;
import com.jwplayer.pub.api.events.FirstFrameEvent;
import com.jwplayer.pub.api.events.FullscreenEvent;
import com.jwplayer.pub.api.events.IdleEvent;
import com.jwplayer.pub.api.events.PauseEvent;
import com.jwplayer.pub.api.events.PlayEvent;
import com.jwplayer.pub.api.events.PlaylistCompleteEvent;
import com.jwplayer.pub.api.events.PlaylistItemEvent;
import com.jwplayer.pub.api.events.ReadyEvent;
import com.jwplayer.pub.api.events.SetupErrorEvent;
import com.jwplayer.pub.api.events.TimeEvent;
import com.jwplayer.pub.api.events.listeners.VideoPlayerEvents;
import com.jwplayer.pub.api.fullscreen.FullscreenHandler;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.ads.AdBreak;
import com.jwplayer.pub.api.media.captions.Caption;
import com.jwplayer.pub.api.media.captions.CaptionType;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.lang.reflect.Constructor;
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


    private static String TAG = "JWPlayerPlugin";
    private JWPlayer implementation = new JWPlayer();
    private JWPlayerView jwPlayerView = null;
    private com.jwplayer.pub.api.JWPlayer mPlayer = null;
    private String JWPLAYER_KEY = "";
    private String GOOGLE_CAST_ID = "";
    private boolean toBack = false;
    private int containerViewId = 20;
    private int grayViewBackId = 21;
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
        this.showProgress();
        call.resolve();
    }

    private void showProgress() {
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    RelativeLayout containerView2 = getBridge().getActivity().findViewById(grayViewBackId);
                    if (containerView2 == null) {
                        final ProgressBar progressBar = new ProgressBar(getBridge().getActivity());
                        progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        progressBar.setBackgroundColor(Color.TRANSPARENT);
                        RelativeLayout.LayoutParams params = new
                                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        RelativeLayout rl = new RelativeLayout(getBridge().getActivity());
                        rl.setBackgroundColor(Color.BLACK);
                        rl.setId(grayViewBackId);
                        rl.setGravity(Gravity.CENTER);
                        rl.addView(progressBar);
                        ((ViewGroup) getBridge().getWebView().getParent()).addView(rl, params);
                        getBridge().getWebView().getParent().bringChildToFront(rl);

                    }
                    getBridge().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage());
                }
            }
        });
    }


    private void removeProgress() {
        RelativeLayout relativeLayout = getBridge().getActivity().findViewById(grayViewBackId);
        if (relativeLayout != null) {
            relativeLayout.removeAllViews();
            ((ViewGroup) getBridge().getWebView().getParent()).removeView(relativeLayout);
        }
    }

    private void hideProgress() {
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "hiding progress");
                    removeProgress();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage());
                }
            }
        });
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
                    final JSObject advertisingConfigObject = call.getObject("advertisingConfig", new JSObject());

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
                            if (data.has("tracks")) {
                                JSONArray arrayCaptions = data.getJSONArray("tracks");
                                if (arrayCaptions.length() > 0) {
                                    for (int j = 0; j < arrayCaptions.length(); j++) {
                                        JSONObject captionData = (JSONObject) arrayCaptions.get(j);
                                        if (captionData.has("kind") && captionData.getString("kind").equals("captions")) {
                                            Caption captionEn = new Caption.Builder()
                                                    .file((String) captionData.get("file"))
                                                    .label((String) captionData.get("label"))
                                                    .kind(CaptionType.CAPTIONS)
                                                    .build();
                                            captionTracks.add(captionEn);
                                        }
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

                    Log.i("PLAYER/Advertising", "generating advertising");


                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width == 0 ? FrameLayout.LayoutParams.MATCH_PARENT : getScaledPixels(width), height == 0 ? FrameLayout.LayoutParams.MATCH_PARENT : getScaledPixels(height));
                    lp.topMargin = getScaledPixels(y);
                    lp.leftMargin = getScaledPixels(x);

                    jwPlayerView.setLayoutParams(lp);
                    jwPlayerView.setKeepScreenOn(true);
                    PlayerConfig.Builder config = new PlayerConfig.Builder()
                            .playlist(playlist)
                            .autostart(autostart);
                    AdvertisingConfig advertisingConfig = generateAdConfiguration(advertisingConfigObject);
                    if (advertisingConfig != null) {
                        config.advertisingConfig(advertisingConfig);
                    }
                    mPlayer.setFullscreenHandler(new FullScreenHandler_NoRotation(jwPlayerView));
                    mPlayer.setup(config.build());
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
                    mPlayer.addListener(EventType.ERROR, jwPlayerHandler);
                    mPlayer.addListener(EventType.FIRST_FRAME, jwPlayerHandler);
                    mPlayer.addListener(EventType.SETUP_ERROR, jwPlayerHandler);
                    mPlayer.allowBackgroundAudio(false);
                    FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);
                    if (containerView == null) {
                        containerView = new FrameLayout(getActivity().getApplicationContext());
                        containerView.setId(containerViewId);
                        if (toBack) {
                            getBridge().getWebView().setBackgroundColor(Color.TRANSPARENT);
                            getBridge().getWebView().getParent().bringChildToFront(getBridge().getWebView());
                        }
                        containerView.addView(jwPlayerView);
                        ((ViewGroup) getBridge().getWebView().getParent()).addView(containerView);
                        RelativeLayout relativeLayout = getBridge().getActivity().findViewById(grayViewBackId);
                        getBridge().getWebView().getParent().bringChildToFront(relativeLayout);
                    } else {
                        call.reject("ContainerView already started");
                    }
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
                    // CueMarker cueMarker = new CueMarker(begin, begin, text, CueMarker.CUE_TYPE_CHAPTERS);
                    //mPlayer.getConfig().
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }

    }

    private void hideTransitionToRemove() {
        RelativeLayout containerView2 = getBridge().getActivity().findViewById(grayViewBackId);
        if (containerView2 == null) {
            RelativeLayout.LayoutParams params = new
                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            RelativeLayout rl = new RelativeLayout(getBridge().getActivity());
            rl.setBackgroundColor(Color.BLACK);
            rl.setId(grayViewBackId);
            rl.setGravity(Gravity.CENTER);
            ((ViewGroup) getBridge().getWebView().getParent()).addView(rl, params);
            getBridge().getWebView().getParent().bringChildToFront(rl);

        }
    }

    @PluginMethod()
    public void remove(final PluginCall call) {
        bridge.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPlayer.closeRelatedOverlay();
                removeProgress();
                hideTransitionToRemove();
                getBridge().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getBridge().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                FrameLayout containerView = getBridge().getActivity().findViewById(containerViewId);
                if (containerView != null) {
                    containerView.setBackgroundColor(Color.BLACK);
                    mPlayer.stop();
                    mPlayer.removeListeners(jwPlayerHandler);
                    mPlayer = null;
                    containerView.removeAllViews();
                    ((ViewGroup) getBridge().getWebView().getParent()).removeView(containerView);
                } else {
                    call.reject("Player already stopped");
                }
                CastContext castContext = CastContext.getSharedInstance(bridge.getActivity());
                SessionManager mSessionManager = castContext.getSessionManager();
                mSessionManager.endCurrentSession(true);
            }
        });

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(700);
                } catch (Exception e) {
                } // Just catch the InterruptedException
                bridge.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        RelativeLayout relativeLayout = getBridge().getActivity().findViewById(grayViewBackId);
                        // Set the View's visibility back on the main UI Thread
                        if (relativeLayout != null) {
                            relativeLayout.setVisibility(View.INVISIBLE);
                            TranslateAnimation animate = new TranslateAnimation(0, relativeLayout.getWidth(), 0, 0);
                            animate.setDuration(400);
                            animate.setRepeatMode(2);
                            animate.setFillAfter(true);
                            animate.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    removeProgress();
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            relativeLayout.startAnimation(animate);
                        }
                    }
                });
            }
        }).start();
        call.success();
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

    private AdvertisingConfig generateAdConfiguration(JSONObject advertisingConfig) throws JSONException {
        List<AdBreak> adSchedule = new ArrayList<>();
        try {
            if (advertisingConfig != null) {
                JSONArray advertisingConfigArray = advertisingConfig.getJSONArray("schedule");
                if (advertisingConfigArray.length() > 0) {
                    for (int i = 0; i < advertisingConfigArray.length(); i++) {
                        JSONObject data = (JSONObject) advertisingConfigArray.get(i);
                        AdBreak adBreak = new AdBreak.Builder()
                                .tag(data.getString("url"))
                                .offset(data.getString("begin"))
                                .build();
                        adSchedule.add(adBreak);
                    }
                }
                switch (advertisingConfig.getString("type")) {
                    case "ima":
                        return new ImaAdvertisingConfig.Builder()
                                .schedule(adSchedule)
                                .build();
                    case "vast":
                        return new VastAdvertisingConfig.Builder()
                                .schedule(adSchedule)
                                .build();
                    default:
                        return null;
                }
            }
        } catch (Exception exception) {
            Log.d(TAG, exception.getMessage());
        }
        return null;
    }

    class JWPlayerHandler implements VideoPlayerEvents.OnPlaylistCompleteListener, VideoPlayerEvents.OnPlayListener, VideoPlayerEvents.OnPauseListener,
            VideoPlayerEvents.OnIdleListener, VideoPlayerEvents.OnCompleteListener, VideoPlayerEvents.OnTimeListener, VideoPlayerEvents.OnPlaylistItemListener,
            VideoPlayerEvents.OnReadyListener, VideoPlayerEvents.OnFullscreenListener, VideoPlayerEvents.OnCaptionsListListener, VideoPlayerEvents.OnCaptionsChangedListener,
            VideoPlayerEvents.OnErrorListener, VideoPlayerEvents.OnFirstFrameListener, VideoPlayerEvents.OnSetupErrorListener {
        @Override
        public void onPlaylistComplete(PlaylistCompleteEvent playlistCompleteEvent) {
            Log.d(TAG, "onPlaylistComplete");
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
            Log.d(TAG, "onComplete");
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

        @Override
        public void onError(ErrorEvent errorEvent) {
            hideProgress();
            JSObject ret = new JSObject();
            ret.put("error", errorEvent.getMessage());
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("error", ret));
        }

        @Override
        public void onFirstFrame(FirstFrameEvent firstFrameEvent) {
            hideProgress();
        }


        @Override
        public void onSetupError(SetupErrorEvent setupErrorEvent) {
            hideProgress();
            JSObject ret = new JSObject();
            ret.put("error", setupErrorEvent.getMessage());
            notifyListeners(JWPLAYER_CHANGE_EVENT, getStatusJSObject("setupError", ret));
        }
    }

    public class FullScreenHandler_NoRotation implements FullscreenHandler {
        JWPlayerView mPlayerView;
        ViewGroup.LayoutParams mDefaultParams;
        ViewGroup.LayoutParams mFullscreenParams;

        public FullScreenHandler_NoRotation(JWPlayerView view) {
            mPlayerView = view;
            mDefaultParams = mPlayerView.getLayoutParams();
        }

        @Override
        public void onFullscreenRequested() {
            doFullscreen(true);
        }

        @Override
        public void onFullscreenExitRequested() {
            doFullscreen(false);
        }

        @Override
        public void onAllowRotationChanged(boolean allowRotation) {

        }

        @Override
        public void updateLayoutParams(ViewGroup.LayoutParams layoutParams) {

        }

        @Override
        public void setUseFullscreenLayoutFlags(boolean flags) {

        }

        private void doFullscreen(boolean fullscreen) {
            if (fullscreen) {
                mFullscreenParams = fullscreenLayoutParams(mDefaultParams);
                mPlayerView.setLayoutParams(mFullscreenParams);
            } else {
                mPlayerView.setLayoutParams(mDefaultParams);
            }
            mPlayerView.requestLayout();
            mPlayerView.postInvalidate();
        }

        /**
         * Creates a clone of srcParams with the width and height set to MATCH_PARENT.
         *
         * @param srcParams
         * @return LayoutParams in fullscreen.
         */
        protected ViewGroup.LayoutParams fullscreenLayoutParams(ViewGroup.LayoutParams srcParams) {
            ViewGroup.LayoutParams params = null;
            try {
                Constructor<? extends ViewGroup.LayoutParams> ctor =
                        srcParams.getClass().getConstructor(ViewGroup.LayoutParams.class);
                params = ctor.newInstance(srcParams);
            } catch (Exception e) {
                params = new ViewGroup.LayoutParams(srcParams);
            }
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            return params;
        }
    }
}
