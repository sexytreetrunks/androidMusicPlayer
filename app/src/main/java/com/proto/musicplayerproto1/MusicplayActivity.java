package com.proto.musicplayerproto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.Utils.TimeUtils;
import com.proto.musicplayerproto1.data.MusicSourceHelper;
import com.proto.musicplayerproto1.player.PlayerHolder;
import com.proto.musicplayerproto1.player.PlayerState;

import java.util.List;

public class MusicplayActivity extends AppCompatActivity {
    private PlayerHolder player;
    //player를 UI화면을 통해 컨트롤하려면 아래 3개는 기본적으로 있어야함
    private MediaSessionCompat session;
    private MediaSessionConnector sessionConnector;
    private MediaControllerCompat controller;

    private ImageView iv_albumcover;
    private TextView tv_title;
    private TextView tv_artist;
    private TextView tv_albumtitle;
    private TextView tv_position;
    private TextView tv_duration;
    private DefaultTimeBar timeBar;
    private ImageButton btn_play;
    private ImageButton btn_pause;

    //progressbar ui 변경에 필요한 변수
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final String PERMITION_LOG_TAG = "**AppPermission";
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplay);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermissions();
        }
        iv_albumcover = (ImageView)findViewById(R.id.album_cover);
        tv_title = (TextView)findViewById(R.id.title);
        tv_artist = (TextView)findViewById(R.id.artist);
        tv_albumtitle = (TextView)findViewById(R.id.album_title);
        tv_position = (TextView)findViewById(R.id.exo_position);
        tv_duration = (TextView)findViewById(R.id.exo_duration);
        timeBar = (DefaultTimeBar)findViewById(R.id.exo_progress);
        btn_play = (ImageButton)findViewById(R.id.exo_play);
        btn_pause = (ImageButton)findViewById(R.id.exo_pause);

        List<MediaMetadataCompat> musicList = new MusicSourceHelper(getContentResolver()).getAllMusicList();

        player = new PlayerHolder(this, new PlayerState(), musicList);
        session = new MediaSessionCompat(this, getPackageName());
        sessionConnector = createMediaSessionConnector();
        sessionConnector.setPlayer(player.getPlayer(), null);//여따가 prepare해보자

        try {
            controller = new MediaControllerCompat(this, session.getSessionToken());
            controller.registerCallback(new MediaControllerCallback());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        MediaMetadataCompat music = musicList.get(0);
        if(music.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)==null)
            iv_albumcover.setImageResource(R.drawable.no_cover);
        else {
            iv_albumcover.setImageURI(Uri.parse(music.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)));
        }
        tv_title.setText(music.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        tv_title.setSelected(true);
        tv_artist.setText(music.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        tv_artist.setSelected(true);
        tv_albumtitle.setText(music.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
        tv_artist.setSelected(true);
        tv_duration.setText(TimeUtils.formatTimeTommss(music.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
        tv_position.setText("00:00");
        timeBar.setDuration(music.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        timeBar.setPosition(0L);
        changePlaybackPosition();
        timeBar.addListener(new TimeBar.OnScrubListener(){
            @Override
            public void onScrubStart(TimeBar timeBar, long position) { }
            @Override
            public void onScrubMove(TimeBar timeBar, long position) { }
            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if(controller!=null)
                    controller.getTransportControls().seekTo(position);
            }
        });
    }

    private void changePlaybackPosition() {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long curpos = controller.getPlaybackState().getPosition();
                if(controller.getPlaybackState().getState()==PlaybackStateCompat.STATE_PLAYING || controller.getPlaybackState().getState()==PlaybackStateCompat.STATE_BUFFERING) {
                    tv_position.setText(TimeUtils.formatTimeTommss(curpos));
                    timeBar.setPosition(curpos);
                }
                if(updatePosition)
                    changePlaybackPosition();
            }
        },100L);
    }

    private MediaSessionConnector createMediaSessionConnector() {
        MediaSessionConnector connector = new MediaSessionConnector(session);
        connector.setQueueNavigator((MediaSessionConnector.QueueNavigator) new TimelineQueueNavigator(session) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                Object obj = player.getCurrentTag();
                return (MediaDescriptionCompat)obj;
            }
        });
        return connector;
    }

    @Override
    protected void onStart() {
        super.onStart();
        player.start();
        sessionConnector.setPlayer(player.getPlayer(),null);
        session.setActive(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.stop();
        session.setActive(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updatePosition = false;
        player.release();
        session.setActive(false);
        session.release();
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_STORAGE);

        } else {
            Log.e(PERMITION_LOG_TAG, "permission deny");
        }
    }

    public void onPlaybackControlBtnClick(View v) {
        if(controller != null) {
            switch (v.getId()) {
                case R.id.exo_play:
                    controller.getTransportControls().play();
                    break;
                case R.id.exo_pause:
                    controller.getTransportControls().pause();
                    break;
                case R.id.exo_next:
                    controller.getTransportControls().skipToNext();
                    break;
                case R.id.exo_prev:
                    controller.getTransportControls().skipToPrevious();
                    break;
                case R.id.exo_shuffle:
                    if(controller.getShuffleMode()== PlaybackStateCompat.SHUFFLE_MODE_NONE)
                        controller.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    else
                        controller.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    break;
                case R.id.exo_repeat_toggle:
                    if(controller.getRepeatMode()== PlaybackStateCompat.REPEAT_MODE_ALL)
                        controller.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                    else
                        controller.getTransportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
            }
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if(state.getState()==PlaybackStateCompat.STATE_BUFFERING || state.getState()==PlaybackStateCompat.STATE_PLAYING) {
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
            } else {
                btn_play.setVisibility(View.VISIBLE);
                btn_pause.setVisibility(View.GONE);
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if(metadata!=null) {
                //TODO: 왜 Discription정보만 보존될까.
                tv_title.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                tv_title.setSelected(true);
                tv_artist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE));
                tv_artist.setSelected(true);
                tv_albumtitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION));
                tv_albumtitle.setSelected(true);
                tv_position.setText("00:00");
                tv_duration.setText(TimeUtils.formatTimeTommss(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)));
                timeBar.setDuration(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                timeBar.setPosition(0L);
                if(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)==null)
                    iv_albumcover.setImageResource(R.drawable.no_cover);
                else
                    iv_albumcover.setImageURI(Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)));
                // 플레이상태로 초기화
                if((controller.getPlaybackState().getState() & PlaybackStateCompat.STATE_PAUSED)!=0) {
                    controller.getTransportControls().play();
                    btn_pause.setVisibility(View.VISIBLE);
                    btn_play.setVisibility(View.GONE);
                }
            }
        }
    }
}


