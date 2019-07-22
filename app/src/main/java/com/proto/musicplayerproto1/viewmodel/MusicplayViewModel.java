package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.R;
import com.proto.musicplayerproto1.model.data.DisplayMetadata;
import com.proto.musicplayerproto1.model.data.DisplayPlaybackState;

public class MusicplayViewModel extends AndroidViewModel {
    private MediaControllerCompat mController;
    private MutableLiveData<DisplayMetadata> nowMediaMetadata;
    private MutableLiveData<DisplayPlaybackState> nowPlaybackState;
    private MutableLiveData<Long> position;
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final DisplayPlaybackState DEFAULT_PLAYBACK_STATE = new DisplayPlaybackState(true, false, PlaybackStateCompat.REPEAT_MODE_ALL);

    public MusicplayViewModel(@NonNull Application application, MediaControllerCompat mController) {
        super(application);
        this.mController = mController;
        this.mController.registerCallback(new MediaControllerCallback());
        nowMediaMetadata = new MutableLiveData<>();
        nowPlaybackState = new MutableLiveData<>();
        nowPlaybackState.postValue(DEFAULT_PLAYBACK_STATE);
        position = new MutableLiveData<>();
        position.postValue(0L);
        changePlaybackPosition();
    }

    public MutableLiveData<DisplayMetadata> getNowMediaMetadata() {
        return nowMediaMetadata;
    }

    public MutableLiveData<Long> getPosition() {
        return position;
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
                long curpos = mController.getPlaybackState().getPosition();
                if(nowPlaybackState.getValue().isPlaying()) {
                    position.postValue(curpos);
                }
                if(updatePosition)
                    changePlaybackPosition();
            }
        },100L);
    }

    public void onPlaybackControlBtnClick(View v) {//onClickListener
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

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
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
                position.postValue(0L);
                mController.getTransportControls().play();
                DisplayPlaybackState displaystate = nowPlaybackState.getValue();
                displaystate.setPlaying(true);
                nowPlaybackState.postValue(displaystate);
            }
        }
    }
}
