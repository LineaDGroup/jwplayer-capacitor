package com.lineadgroup.capacitorjwplayer;

import android.Manifest;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import com.getcapacitor.annotation.Permission;
import com.jwplayer.pub.api.configuration.PlayerConfig;
import com.jwplayer.pub.api.license.LicenseUtil;
import com.jwplayer.pub.api.media.playlists.PlaylistItem;
import com.jwplayer.pub.view.JWPlayerView;

import java.util.ArrayList;
import java.util.List;


@CapacitorPlugin(name = "JWPlayer",
        permissions = {
                @Permission(
                        strings = { Manifest.permission.READ_EXTERNAL_STORAGE },
                        alias = "storage"
                )
        })
public class JWPlayerPlugin extends Plugin {

    private JWPlayer implementation = new JWPlayer();
    String JWPLAYER_KEY = "";
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
        if(TextUtils.isEmpty(this.JWPLAYER_KEY)){
            call.reject("License key missing! You need to set up your JWPlayer license key");
            return;
        }

        LicenseUtil.setLicenseKey(getBridge().getContext(), this.JWPLAYER_KEY);

        call.resolve();
    }

    @PluginMethod()
    public void create(PluginCall call) {
        final String videoURL = call.getString("videoURL", "");
        
        //Get player dimensions
        final Integer width = call.getInt("width", 0);
        final Integer height = call.getInt("height", 0);
        final Integer x = call.getInt("x", 0);
        final Integer y = call.getInt("y", 0);

        if(TextUtils.isEmpty(videoURL)){
            call.reject("You have to provide a fileURL to playback");
            return;
        }

        final JWPlayerPlugin ctx = this;
        getBridge().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlaylistItem playlistItem = new PlaylistItem.Builder()
                .file(videoURL)
                .build();

                
                FrameLayout jwPlayerViewParent = new FrameLayout(getBridge().getContext());
                int jwPlayerViewParentId = View.generateViewId();
                jwPlayerViewParent.setId(jwPlayerViewParentId);

                JWPlayerView jwPlayerView = new JWPlayerView(getBridge().getContext());

                com.jwplayer.pub.api.JWPlayer mPlayer = jwPlayerView.getPlayer();

                //Set view layout. Player will take the whole screen if no parameters are set.
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width==0?FrameLayout.LayoutParams.WRAP_CONTENT:width, height==0?FrameLayout.LayoutParams.WRAP_CONTENT:height);
                lp.topMargin = getScaledPixels(x);
                lp.leftMargin = getScaledPixels(y);

                jwPlayerView.setLayoutParams(lp);

                jwPlayerViewParent.addView(jwPlayerView);

                ((ViewGroup) getBridge().getWebView().getParent()).addView(jwPlayerViewParent);

                List<PlaylistItem> playlist = new ArrayList<>();
                playlist.add(playlistItem);
                PlayerConfig config = new PlayerConfig.Builder()
                        .playlist(playlist)
                        .build();

                mPlayer.setup(config);
//                jwPlayerView.getPlayerAsync(ctx);
            }
        });
        call.resolve();
    }

    public int getScaledPixels(float pixels) {
        // Get the screen's density scale
        final float scale = getBridge().getActivity().getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }
}
