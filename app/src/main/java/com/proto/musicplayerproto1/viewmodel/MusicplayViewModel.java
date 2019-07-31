package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.MusicClient;
import com.proto.musicplayerproto1.MusicService;
import com.proto.musicplayerproto1.R;
import com.proto.musicplayerproto1.model.data.DisplayMetadata;
import com.proto.musicplayerproto1.model.data.DisplayPlaybackState;

import java.util.List;

public class MusicplayViewModel extends AndroidViewModel {
    private static MediaControllerCompat mController;
    private static MediaBrowserCompat mMediaBrowser;
    private MutableLiveData<DisplayMetadata> nowMediaMetadata;
    private MutableLiveData<DisplayPlaybackState> nowPlaybackState;
    private MutableLiveData<Long> progress;
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    //초기값지정시 player와 ViewModel의 nowPlayerbackState 값을 따로 지정해야함. player에 있는 초기 상태를 가져와서 여기서 초기화를 시키거나, 여기서 초기화시킨걸 player 초기값으로 지정하는 식으로 바꿔야함
    private static final DisplayPlaybackState DEFAULT_PLAYBACK_STATE = new DisplayPlaybackState(true, false, PlaybackStateCompat.REPEAT_MODE_ALL);

    public MusicplayViewModel(@NonNull Application application) {
        super(application);
        //일단 여기서 브라우저 생성 & 컨트롤러 생성
        Log.d("**","viewmodel 생성");
        nowMediaMetadata = new MutableLiveData<>();
        nowPlaybackState = new MutableLiveData<>();
        nowPlaybackState.postValue(DEFAULT_PLAYBACK_STATE);
        progress = new MutableLiveData<>();
        progress.postValue(0L);
        changePlaybackPosition();
        Log.d("**",(mMediaBrowser==null)? "browser is null":"browser 생성됨");
        if(mMediaBrowser==null) {
            mMediaBrowser = new MediaBrowserCompat(application.getApplicationContext(),
                    new ComponentName(application.getApplicationContext(), MusicService.class),
                    new MediaBrowserConnectionCallback(application.getApplicationContext()),
                    null);
            mMediaBrowser.connect(); //요걸해야 service create되는거
        } else {
            if(mController!=null) {
                MediaMetadataCompat metadata = mController.getMetadata();
                nowMediaMetadata.postValue(new DisplayMetadata(
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE),
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION),
                        metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                ));
                PlaybackStateCompat playbackState = mController.getPlaybackState();
                DisplayPlaybackState displayPlaybackState = new DisplayPlaybackState();
                if(playbackState.getState()==PlaybackStateCompat.STATE_PLAYING || playbackState.getState()==PlaybackStateCompat.STATE_BUFFERING)
                    displayPlaybackState.setPlaying(true);
                else
                    displayPlaybackState.setPlaying(false);
                boolean shufflemode = !(mController.getShuffleMode()==PlaybackStateCompat.SHUFFLE_MODE_NONE);
                displayPlaybackState.setShuffle(shufflemode);
                displayPlaybackState.setRepeatMode(mController.getRepeatMode());
                nowPlaybackState.postValue(displayPlaybackState);
            }
        }
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

    @Override
    protected void onCleared() {
        super.onCleared();
        updatePosition = false;
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
            Log.d("**", "browser connection 생성");
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
            super.onConnected();
        }

        @Override
        public void onConnectionSuspended() {
            Log.d("**","browser connection suspended");
            super.onConnectionSuspended();
        }

        @Override
        public void onConnectionFailed() {
            Log.d("**", "browser connection failed");
            super.onConnectionFailed();
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {//데이터 관련 mediacontroller callback
        public MediaControllerCallback() {
            Log.d("**","controller callback 생성");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("**","controller callback, onPlaybackStateChanged");
            super.onPlaybackStateChanged(state);
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            if(state.getState()==PlaybackStateCompat.STATE_PLAYING || state.getState()==PlaybackStateCompat.STATE_BUFFERING)
                displaystate.setPlaying(true);
            else
                displaystate.setPlaying(false);
            nowPlaybackState.postValue(displaystate);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            displaystate.setRepeatMode(repeatMode);
            nowPlaybackState.postValue(displaystate);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
            DisplayPlaybackState displaystate = nowPlaybackState.getValue();
            if(shuffleMode==PlaybackStateCompat.SHUFFLE_MODE_NONE)
                displaystate.setShuffle(false);
            else
                displaystate.setShuffle(true);
            nowPlaybackState.postValue(displaystate);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("**","controller callback, onMetadataChanged");
            super.onMetadataChanged(metadata);
            if(metadata!=null) {
                DisplayMetadata nowPlayingData = new DisplayMetadata(
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
                mController.getTransportControls().play();
            }
        }
    }


}
