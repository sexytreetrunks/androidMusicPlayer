package com.proto.musicplayerproto1.model.player;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/*
 * 비디오 플레이어 컨트롤 담당(플레이어와 UI간 통신 담당)
 */
public class PlayerHolder {
    private PlayerState playerState;
    private SimpleExoPlayer player;

    private static final String UI_LOG_TAG = "**PlayerHolder Func call";
    private static final String PLAYER_LOG_TAG = "++Player status";

    public PlayerHolder(Context context, PlayerState playerState) {
        this.playerState = playerState;

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                                    .setUsage(C.USAGE_MEDIA)
                                                    .setContentType(C.CONTENT_TYPE_MOVIE)
                                                    .build();
        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        player.setAudioAttributes(audioAttributes, true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        attachLogging(player);
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void start() {
        player.setPlayWhenReady(playerState.getWhenReady());
        player.seekTo(playerState.getWindow(), playerState.getPosition());
        Log.i(UI_LOG_TAG,"start func call");
    }

    public void stop() {
        player.stop(true); // reset 시키든 안시키든 상관없음.
        Log.i(UI_LOG_TAG, "stop func call");
    }

    public void release() {
        player.release();
        Log.i(UI_LOG_TAG, "release func call");
    }

    private void attachLogging(ExoPlayer exoPlayer) {

        exoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.d(PLAYER_LOG_TAG,"window position changed : " +reason);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(PLAYER_LOG_TAG, getStateString(playbackState)+","+((playWhenReady)? "playReady":"playNotReady"));
            }

            @Override
            public void onPlayerError(ExoPlaybackException err) {
                Log.d(PLAYER_LOG_TAG, "ERROR:\t" +err);
            }

            private String getStateString(int state) {
                String str = "";
                switch (state) {
                    case Player.STATE_BUFFERING:
                        str = "STATE_BUFFERING";
                        break;
                    case Player.STATE_ENDED:
                        str = "STATE_ENDED";
                        break;
                    case Player.STATE_IDLE:
                        str = "STATE_IDLE";
                        break;
                    case Player.STATE_READY:
                        str = "STATE_READY";
                        break;
                    default:
                        str = "?";
                        break;
                }
                return str;
            }
        });
    }

}
