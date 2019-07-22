package com.proto.musicplayerproto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
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
import android.widget.Toast;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.databinding.ActivityMusicplayBinding;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;
import com.proto.musicplayerproto1.model.player.PlayerHolder;
import com.proto.musicplayerproto1.model.player.PlayerState;
import com.proto.musicplayerproto1.viewmodel.CustomViewModelFactory;
import com.proto.musicplayerproto1.viewmodel.MusicplayViewModel;

import java.util.List;

public class MusicplayActivity extends AppCompatActivity {
    private MusicplayViewModel viewModel;

    private PlayerHolder player;
    //player를 UI화면을 통해 컨트롤하려면 아래 3개는 기본적으로 있어야함
    private MediaSessionCompat session;
    private MediaSessionConnector sessionConnector;
    private MediaControllerCompat mController;

    private DefaultTimeBar timeBar;

    private static final String PERMITION_LOG_TAG = "**AppPermission";
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplay);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermissions();
        }

        List<MediaMetadataCompat> musicList = new MusicSourceHelper(getContentResolver()).getAllMusicList();

        player = new PlayerHolder(this, new PlayerState(), musicList);
        session = new MediaSessionCompat(this, getPackageName());
        sessionConnector = createMediaSessionConnector();
        sessionConnector.setPlayer(player.getPlayer(), null);//여따가 prepare해보자

        try {
            mController = new MediaControllerCompat(this, session.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        viewModel = new CustomViewModelFactory(getApplication(), mController).create(MusicplayViewModel.class);
        final ActivityMusicplayBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_musicplay);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);
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
}


