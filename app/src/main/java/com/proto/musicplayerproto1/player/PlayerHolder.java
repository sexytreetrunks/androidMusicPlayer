package com.proto.musicplayerproto1.player;

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
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.List;

/*
 * 비디오 플레이어 컨트롤 담당(플레이어와 UI간 통신 담당)
 */
public class PlayerHolder {
    private Context context;
    private PlayerState playerState;
    private SimpleExoPlayer player;
    private List<MediaMetadataCompat> playSrc;// MediaMetadataCompat or Arraylist<MediaMetadataCompat>

    private static final String UI_LOG_TAG = "**PlayerHolder Func call";
    private static final String PLAYER_LOG_TAG = "**Player status";

    public PlayerHolder(Context context, PlayerState playerState, List<MediaMetadataCompat> playSrc) {
        this.context = context;
        this.playerState = playerState;
        this.playSrc = playSrc;

        AudioManager audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                                    .setUsage(C.USAGE_MEDIA)
                                                    .setContentType(C.CONTENT_TYPE_MOVIE)
                                                    .build();
        player = ExoPlayerFactory.newSimpleInstance(context, new DefaultTrackSelector());
        player.setAudioAttributes(audioAttributes, true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
    }

    public Player getPlayer() {
        return player;
    }

    public void start() {
        MediaSource mediaSource = buildMediaSource(playSrc);
        player.prepare(mediaSource);
        player.setPlayWhenReady(playerState.getWhenReady());

        player.seekTo(playerState.getWindow(), playerState.getPosition());
        attachLogging(player);
        Log.i(UI_LOG_TAG,"start func call");
    }

    private MediaSource buildMediaSource(List<MediaMetadataCompat> playSrcList) {
        List<MediaSource> uriList = new ArrayList<MediaSource>();
        for(MediaMetadataCompat m: playSrcList) {
            uriList.add(createExtractorMediaSource(Uri.parse(m.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)), m.getDescription()));
        }
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(uriList);
        return (new LoopingMediaSource(concatenatingMediaSource));
    }

    private MediaSource createExtractorMediaSource(Uri mediaUri, MediaDescriptionCompat description) {
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(context, "exoplayer-learning"))
                .setTag(description)
                .createMediaSource(mediaUri);
    }

    public void stop() {
        playerState.setPosition(player.getCurrentPosition());
        playerState.setWindow(player.getCurrentWindowIndex());
        playerState.setWhenReady(player.getPlayWhenReady());
        player.stop(true);
        Log.i(UI_LOG_TAG, "stop func call");
    }

    public void release() {
        player.release();
        Log.i(UI_LOG_TAG, "release func call");
    }

    private void attachLogging(ExoPlayer exoPlayer) {

        exoPlayer.addListener(new Player.EventListener() {
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
