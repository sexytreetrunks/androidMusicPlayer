package com.proto.musicplayerproto1;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.proto.musicplayerproto1.databinding.ActivityMusicplayBinding;
import com.proto.musicplayerproto1.model.player.PlayerHolder;
import com.proto.musicplayerproto1.viewmodel.MusicplayViewModel;

import java.util.List;

public class MusicplayActivity extends AppCompatActivity {
    private MusicplayViewModel viewModel;

    private PlayerHolder player;
    //player를 UI화면을 통해 컨트롤하려면 아래 3개는 기본적으로 있어야함
    private MediaSessionCompat session;
    private MediaSessionConnector sessionConnector;
    private MediaControllerCompat mController;
    private MediaBrowserCompat mBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_musicplay);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermissions();
        }

        /*player = new PlayerHolder(this, new PlayerState());
        session = new MediaSessionCompat(this, getPackageName());
        sessionConnector = createMediaSessionConnector();

        try {
            mController = new MediaControllerCompat(this, session.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        */
        /*MediaBrowserCompat mediaBrowser;
        MediaBrowserCompat.ConnectionCallback callback;
        callback.onConnected();//여기서 mController 초기화
        MediaBrowserCompat.SubscriptionCallback subscriptionCallback;
        subscriptionCallback.onChildrenLoaded();//여기서 MusicService.onLoadedChildren에서 던진 데이터 받아서 viewModel에 있는 데이타 갱신*/

        // 이제 서비스 불러와서 시작하면됨
        //mBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class), new MediaBrowserConnectionCallback(),null);

        //viewModel = new CustomViewModelFactory(getApplication()).create(MusicplayViewModel.class);//이렇게 하면 onCleared안불러짐. 왜그런지는 모르겠음
        //viewModel = CustomViewModelFactory.getInstance(getApplication()).create(MusicplayViewModel.class);//얘도 안불러와짐
        //예상원인. 그냥 ViewModelFactory로 생성한 viewmodel은 ViewModelStore가 포함되지않음.
        //ViewModelStore은 엑티비티/프래그먼트의 viewmodel들을 갖고있는 클래스로써 viewmodelstore의 오너인 엑티비티/프래그먼트가 소멸시 clear()를 호출하여 viewmodel의 oncleared함수가 호출되도록함
        //Q. 그럼 우리가 커스텀한 ViewModelFactory에서 ViewModelStore를 생성해서 사용하면 되지않나여?
        // 근데 ViewModelStore에서 생명주기관리할 viewmodel을 저장하고불러오는 함수인 get,put함수는 접근제어자가 default로 지정되어있음. 즉 같은 패키지내에서만 사용이 가능한 함수
        // 그래서 우리가 사용할수있는건 public으로 지정된 clear함수뿐임(근데 무용지물. activity/fragment 패키지에서 사용하기위해 내놓은거지 개발자가 사용하라고 만들어놓은게 아님)
        viewModel = ViewModelProviders.of(this).get(MusicplayViewModel.class);
        ActivityMusicplayBinding binding = DataBindingUtil.setContentView(MusicplayActivity.this, R.layout.activity_musicplay);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(MusicplayActivity.this);
    }

    /*private MediaSessionConnector createMediaSessionConnector() {
        MediaSessionConnector connector = new MediaSessionConnector(session);
        connector.setQueueNavigator((MediaSessionConnector.QueueNavigator) new TimelineQueueNavigator(session) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                Object obj = player.getCurrentTag();
                return (MediaDescriptionCompat)obj;
            }
        });
        MusicPlaybackPreparer playbackPreparer = new MusicPlaybackPreparer(player.getPlayer(), this);
        connector.setPlayer(player.getPlayer(), playbackPreparer);
        return connector;
    }*/

    /*@Override
    protected void onStart() {
        super.onStart();
        mController.getTransportControls().prepare();
        player.start();
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
        session.release();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("**","activity destroyed");
    }

    private void checkPermissions() {
        String PERMITION_LOG_TAG = "**AppPermission";
        int MY_PERMISSION_REQUEST_STORAGE = 100;
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


