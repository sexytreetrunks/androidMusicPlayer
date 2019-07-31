package com.proto.musicplayerproto1;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;
import com.proto.musicplayerproto1.model.player.MusicPlaybackPreparer;
import com.proto.musicplayerproto1.model.player.PlayerHolder;
import com.proto.musicplayerproto1.model.player.PlayerState;

import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {
    private PlayerHolder player;
    private MediaSessionCompat session;
    private MediaSessionConnector sessionConnector;

    private MediaControllerCompat mController;

    private boolean isForegroundService = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("**","service 생성");
        player = new PlayerHolder(this, new PlayerState());
        session = new MediaSessionCompat(this, getPackageName());
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setActive(true);
        sessionConnector = createMediaSessionConnector();
        setSessionToken(session.getSessionToken());
        // 이거 안넣으면 service는 생성되지만 browser에선 connect시 sessionToken을 못받아서 계속 connecting 상태가됨(즉 browser가있는 메인스레드에서 session이있는 service스레드한테 "session토큰을 받고 connect하는 작업"을 요청하지만 service스레드는 sessiontoken이 없으니 계속 작업을 미러둠)
        try {
            mController = new MediaControllerCompat(this, session.getSessionToken());
            mController.registerCallback(new MediaControllerNotificationCallback());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //session.setSessionActivity();
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
        MusicPlaybackPreparer playbackPreparer = new MusicPlaybackPreparer(player.getPlayer(), this);
        connector.setPlayer(player.getPlayer(), playbackPreparer);
        return connector;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        // 존나 뭐하는 놈인지 모르겠음. null반환하면 연결 거부된다길래 일단 아무거나 만들어서 리턴함
        return (new BrowserRoot("root",null));
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //SEND DATA(playlist) to browser
        //RECIEVE DATA(playlist) from browser.onChildrenLoad
    }

    //이것때문인지 아닌지 확인필요
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("**","service binded. intent action is "+intent.getAction()); //-- manifest에 설정한 service의 action과 동일한지 확인 ㄱ
        mController.getTransportControls().prepare();
        mController.getTransportControls().play();
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) { // 이게 필요할지 안필요할지 모르겠음
        Log.d("**","service is unbinded");
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        player.stop();
        session.setActive(false);
    }

    @Override
    public void onDestroy() {
        player.release();
        session.release();
        Log.d("**","service is destroyed");
    }

    private class MediaControllerNotificationCallback extends MediaControllerCompat.Callback {//Notification 관련 mediacontroller callback
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            updateNotification(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            updateNotification(mController.getPlaybackState());
        }

        private void updateNotification(PlaybackStateCompat state) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    Notification notification = new NotificationBuilder(MusicService.this)
                                                .buildNotification(session.getSessionToken());
                    if(!isForegroundService) {//이미 동작중인 foreground서비스가 없을경우 새로 생성(stop한뒤 플레이하는경우) 아니면 새로 생성하지않음(pause한뒤 플레이하는경우). stop한뒤 플레이가 되면 service가 아예 새로 생성되기 때문에 isForegroundService는 false상태가됨. pause한뒤 플레이하면 start했을때 이미 isForegroundService를 true로 해논상태이므로 true가됨
                        ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(),MusicService.this.getClass()));
                        isForegroundService = true;
                    }
                    startForeground(NotificationBuilder.NOTIFICATION_ID, notification); //startForeground를 하게되면 notificationManager로 notify하지않아도 notification이 실행됨
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    stopForeground(false);
                    Notification notification1 = new NotificationBuilder(MusicService.this)
                                                .buildNotification(session.getSessionToken());
                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NotificationBuilder.NOTIFICATION_ID, notification1);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    stopForeground(true);// foreground사용중지하고 notification도 지움. 근데 어짜피 stopSelf로 서비스 죽일거라서 이 명령어는 주석처리해도됨
                    stopSelf();
                    break;
            }
        }
    }
}
