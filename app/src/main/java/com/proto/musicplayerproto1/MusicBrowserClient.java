package com.proto.musicplayerproto1;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

import com.proto.musicplayerproto1.MusicService;

public class MusicBrowserClient {
    private Context mContext;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private MediaControllerCompat.TransportControls transportControls;

    public MusicBrowserClient(Context context) {
        mContext = context;
        mMediaBrowser = new MediaBrowserCompat(context,
                                                new ComponentName(context, MusicService.class),
                                                new MediaBrowserConnectionCallback(),
                                                null);

    }
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            try {
                mMediaController = new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());

            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }
    }
}
