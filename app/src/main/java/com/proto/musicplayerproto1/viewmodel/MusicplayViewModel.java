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
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ui.TimeBar;
import com.proto.musicplayerproto1.MusicService;
import com.proto.musicplayerproto1.MusicplayActivity;
import com.proto.musicplayerproto1.R;
import com.proto.musicplayerproto1.model.data.DisplayMetadata;
import com.proto.musicplayerproto1.model.data.DisplayPlaybackState;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MusicplayViewModel extends AndroidViewModel {
    private static final DisplayPlaybackState DEFAULT_PLAYBACK_STATE = new DisplayPlaybackState(true, false, PlaybackStateCompat.REPEAT_MODE_ALL);
    private static final long DELAY_TIME = 100L;
    //service 관련
    private static MediaControllerCompat mController;
    private static MediaBrowserCompat mMediaBrowser;
    //ui관련
    private MutableLiveData<DisplayMetadata> nowMediaMetadata = new MutableLiveData<>();
    private MutableLiveData<DisplayPlaybackState> nowPlaybackState = new MutableLiveData<>();
    private MutableLiveData<Long> progress = new MutableLiveData<>();
    private boolean updatePosition = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    //rewind, fastforward 구현 관련
    private Handler handler = new Handler();
    private boolean forwardRewindFlag = false;
    private final static long timerTaskDelayTime = 800L;
    private final static long longPressDuration = timerTaskDelayTime;
    private Timer timer;
    private long pressTime;
    //coverflow 관련
    private MutableLiveData<Integer> currentDataPosition = new MutableLiveData<>();
    private MutableLiveData<List<MediaMetadataCompat>> dataList = new MutableLiveData<>();

    public MusicplayViewModel(@NonNull Application application) {
        super(application);
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
        },DELAY_TIME);
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

    public View.OnTouchListener getOnPressedListener() {
        return (new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressTime = System.currentTimeMillis();
                        timer = new Timer(); //timer와 timertask는 재사용이 불가능. 걍 다시쓸때마다 새로 만들어줘야함 개빡침.
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                forwardRewindFlag = true;
                                if(v.getId() == R.id.exo_next)
                                    fastForward();
                                else if(v.getId() == R.id.exo_prev)
                                    rewind();
                            }
                        }, DELAY_TIME);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        pressTime = System.currentTimeMillis() - pressTime;
                        forwardRewindFlag = false;
                        timer.cancel();
                        timer.purge();
                        if(pressTime >= longPressDuration)// longClick일경우 true를 리턴하여 onClick이 실행안되도록 한다
                            return true;
                        break;
                }
                return false;
            }
        });
    }

    private void fastForward() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int fastforwardMs = getApplication().getResources().getInteger(R.integer.exoplayer_playback_fastforward_increment_ms);
                long duration = mController.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                long seekPosition = mController.getPlaybackState().getPosition() + fastforwardMs;
                if(duration != C.TIME_UNSET)
                    seekPosition = Math.min(seekPosition, duration);
                mController.getTransportControls().seekTo(seekPosition);
                if(forwardRewindFlag)
                    fastForward();
            }
        }, DELAY_TIME);
    }

    private void rewind() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int rewindMs = getApplication().getResources().getInteger(R.integer.exoplayer_playback_fastforward_increment_ms);
                long seekPosition = mController.getPlaybackState().getPosition() - rewindMs;
                mController.getTransportControls().seekTo(Math.max(seekPosition, 0));
                if(forwardRewindFlag)
                    rewind();
            }
        },DELAY_TIME);
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
