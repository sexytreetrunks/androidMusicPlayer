package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.proto.musicplayerproto1.Utils.TimeUtils;
import com.proto.musicplayerproto1.model.data.DisplayMetadata;


//여기 UI관련 코드들 다 들어가있어야함
public class MusicplayViewModel extends AndroidViewModel {
    private MediaControllerCompat mController;
    private MutableLiveData<DisplayMetadata> nowMediaMetadata;
    private MutableLiveData<Boolean> isPlaying;
    private MutableLiveData<Long> position;
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public MusicplayViewModel(@NonNull Application application, MediaControllerCompat mController) {
        super(application);
        this.mController = mController;
        this.mController.registerCallback(new MediaControllerCallback());
        nowMediaMetadata = new MutableLiveData<>();
        isPlaying = new MutableLiveData<>();
        isPlaying.postValue(true);
        position = new MutableLiveData<>();
        position.postValue(0L);
        changePlaybackPosition();
    }

    public MutableLiveData<DisplayMetadata> getNowMediaMetadata() {
        return nowMediaMetadata;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public MutableLiveData<Long> getPosition() {
        return position;
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
                if(isPlaying.getValue()) {
                    position.postValue(curpos);
                }
                if(updatePosition)
                    changePlaybackPosition();
            }
        },100L);
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if(state.getState()==PlaybackStateCompat.STATE_PLAYING || state.getState()==PlaybackStateCompat.STATE_BUFFERING)
                isPlaying.postValue(true);
            else
                isPlaying.postValue(false);
            Log.d("**","change isPlaying: "+isPlaying.getValue());
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
                if((mController.getPlaybackState().getState() & PlaybackStateCompat.STATE_PAUSED)!=0) {
                    mController.getTransportControls().play();
                    isPlaying.postValue(true);
                }
            }
        }
    }
}
