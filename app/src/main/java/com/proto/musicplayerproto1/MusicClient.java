package com.proto.musicplayerproto1;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import com.proto.musicplayerproto1.viewmodel.MusicplayViewModel;

public class MusicClient {
    private static MusicClient musicClientInstance;

    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;

    private MusicClient(Context applicationContext) {
        mMediaBrowser = new MediaBrowserCompat(applicationContext,
                                                new ComponentName(applicationContext, MusicService.class),
                                                new MediaBrowserConnectionCallback(applicationContext),
                                                null);
        mMediaBrowser.connect();
    }

    public MediaControllerCompat getMediaController() {
        return mMediaController;
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
                mMediaController = new MediaControllerCompat(context, mMediaBrowser.getSessionToken());
                //TODO: register callback
                // 여기서 등록할 callback은 viewModel데이터를 갱신하는 코드가 담겨있음
                // 따라서 해당 콜백 코드(MediaControllerCallback)를 여기로 가져올수없음(viewmodel과 결합도가 높기때문에)
                // viewModel에서 MusicClient의 controller를 get하여 콜백을 등록하는 방법이 있으나
                // 이는 browser의 connect가 된후 이루어져야하기 때문에 onCreate에서 초기화시
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

    public static MusicClient getInstance(Context applicationContext) {
        if(musicClientInstance == null) {
            musicClientInstance = new MusicClient(applicationContext);
        }
        return musicClientInstance;
    }
}
