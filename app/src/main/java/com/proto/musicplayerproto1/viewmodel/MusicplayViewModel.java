package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.MusicService;
import com.proto.musicplayerproto1.MusicplayActivity;
import com.proto.musicplayerproto1.R;
import com.proto.musicplayerproto1.model.data.DisplayMetadata;
import com.proto.musicplayerproto1.model.data.DisplayPlaybackState;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MusicplayViewModel extends AndroidViewModel {
    //service 관련
    private static MediaControllerCompat mController;
    private static MediaBrowserCompat mMediaBrowser;
    //ui관련
    private MutableLiveData<DisplayMetadata> nowMediaMetadata = new MutableLiveData<>();
    private MutableLiveData<DisplayPlaybackState> nowPlaybackState = new MutableLiveData<>();
    private MutableLiveData<Long> progress = new MutableLiveData<>();
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    //coverflow 관련
    private MutableLiveData<Integer> currentDataPosition = new MutableLiveData<>();
    private MutableLiveData<List<MediaMetadataCompat>> dataList = new MutableLiveData<>();

    //초기값지정시 player와 ViewModel의 nowPlayerbackState 값을 따로 지정해야함. player에 있는 초기 상태를 가져와서 여기서 초기화를 시키거나, 여기서 초기화시킨걸 player 초기값으로 지정하는 식으로 바꿔야함
    private static final DisplayPlaybackState DEFAULT_PLAYBACK_STATE = new DisplayPlaybackState(true, false, PlaybackStateCompat.REPEAT_MODE_ALL);

    public MusicplayViewModel(@NonNull Application application) {
        super(application);
        //일단 여기서 브라우저 생성 & 컨트롤러 생성
        Log.d("**","viewmodel 생성");
        currentDataPosition.setValue(0);
        dataList.setValue(new MusicSourceHelper(application.getContentResolver()).getAllMusicList());
        nowPlaybackState.setValue(DEFAULT_PLAYBACK_STATE);
        progress.setValue(0L);
        changePlaybackPosition();
        if(mMediaBrowser==null) {
            mMediaBrowser = new MediaBrowserCompat(application.getApplicationContext(),
                    new ComponentName(application.getApplicationContext(), MusicService.class),
                    new MediaBrowserConnectionCallback(application.getApplicationContext()),
                    null);
            Log.d("**","browser 생성");
        }
        mMediaBrowser.connect();
        Log.d("**","browser connection 요청");
    }

    public MutableLiveData<DisplayMetadata> getNowMediaMetadata() {
        return nowMediaMetadata;
    }

    public MutableLiveData<Long> getProgress() {
        return progress;
    }

    public MutableLiveData<DisplayPlaybackState> getNowPlaybackState() {
        return nowPlaybackState;
    }

    public MutableLiveData<Integer> getCurrentDataPosition() {
        return currentDataPosition;
    }

    public void setCurrentDataPosition(MutableLiveData<Integer> currentDataPosition) {
        this.currentDataPosition = currentDataPosition;
    }

    public int getUriListSize() {
        return dataList.getValue().size();
    }

    public MutableLiveData<List<MediaMetadataCompat>> getDataList() {
        return dataList;
    }

    public void setDataList(MutableLiveData<List<MediaMetadataCompat>> dataList) {
        this.dataList = dataList;
    }

    public MediaControllerCompat getController() {
        return mController;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        updatePosition = false;
        /*mMediaBrowser.unsubscribe("root",subscriptionCallback);*/
        mMediaBrowser.disconnect();
        Log.d("**","viewmodel onCleared(browser connection 종료)");
    }

    private void changePlaybackPosition() {
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mController!=null) {
                    if(nowPlaybackState.getValue().isPlaying()) {
                        long curpos = mController.getPlaybackState().getPosition();
                        progress.postValue(curpos);
                    }
                }
                if(updatePosition)
                    changePlaybackPosition();
            }
        },100L);
    }

    //원래 정석대로라면 viewModel에서 view객체를 사용하면안됨. viewModel은 view객체가 사용할 데이터만 가져와야하는거. 이건 어떻게 리펙토링해야할지 고민필요
    public void onPlaybackControlBtnClick(View v) {
        if(mController != null) {
            switch (v.getId()) {
                case R.id.exo_playpause:
                    if(nowPlaybackState.getValue().isPlaying())
                        mController.getTransportControls().pause();
                    else
                        mController.getTransportControls().play();
                    break;
                case R.id.exo_next:
                    mController.getTransportControls().skipToNext();
                    break;
                case R.id.exo_prev:
                    mController.getTransportControls().skipToPrevious();
                    break;
                case R.id.exo_shuffle:
                    if(mController.getShuffleMode()== PlaybackStateCompat.SHUFFLE_MODE_NONE)
                        mController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                    else
                        mController.getTransportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                    break;
                case R.id.exo_repeat_toggle:
                    mController.getTransportControls().setRepeatMode((mController.getRepeatMode()+1)%3);
            }
        }
    }

    //Could not find accessor 에러: getter와 연결된 variable의 이름과 xml에서 불러오는 variable의 이름이 일치해야함. 또한 getter는 parameter가지면 안됨.
    public TimeBar.OnScrubListener getTimebarScrubListener() {
        return (new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                if(mController!=null)
                    mController.getTransportControls().seekTo(position);
            }
        });
    }

    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        private Context context;
        public MediaBrowserConnectionCallback(Context context) {
            this.context = context;
            Log.d("**", "browser callback 생성(connection callback)");
        }

        @Override
        public void onConnected() {
            try {
                Log.d("**","browser onconnected, controller 생성");
                mController = new MediaControllerCompat(context, mMediaBrowser.getSessionToken());
                mController.registerCallback(new MediaControllerCallback());
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("**","browser connection suspended");
        }

        @Override
        public void onConnectionFailed() {
            Log.d("**", "browser connection failed");
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {//데이터 관련 mediacontroller callback
        public MediaControllerCallback() {
            Log.d("**","controller callback 생성");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            if(state.getState()==PlaybackStateCompat.STATE_PLAYING || state.getState()==PlaybackStateCompat.STATE_BUFFERING)
                displaystate.setPlaying(true);
            else
                displaystate.setPlaying(false);
            nowPlaybackState.setValue(displaystate);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            displaystate.setRepeatMode(repeatMode);
            nowPlaybackState.setValue(displaystate);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            if(shuffleMode==PlaybackStateCompat.SHUFFLE_MODE_NONE)
                displaystate.setShuffle(false);
            else
                displaystate.setShuffle(true);
            nowPlaybackState.setValue(displaystate);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if(metadata!=null) {
                Log.d("==","on metadata changed called: "+metadata.getDescription().toString());
                DisplayMetadata nowPlayingData = new DisplayMetadata(
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION),
                        metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                );
                nowMediaMetadata.postValue(nowPlayingData);
                progress.postValue(0L);
                DisplayPlaybackState displaystate = nowPlaybackState.getValue();
                displaystate.setPlaying(true);
                nowPlaybackState.postValue(displaystate);
                /*SharedPreferences sp = getApplication().getSharedPreferences("pref",Context.MODE_PRIVATE);
                int windowPosition = sp.getInt(getApplication().getString(R.string.pref_key_windowPosition),0);
                currentDataPosition.postValue(windowPosition);*/
                //dataList.getValue().stream().map();
                nowPlayingData.getMediaId();
                int i=0;
                for(MediaMetadataCompat m: dataList.getValue()) {
                    if(m.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).equals(nowPlayingData.getMediaId()))
                        currentDataPosition.setValue(i);
                    i++;
                }
                mController.getTransportControls().play();
            }
        }
    }
}
