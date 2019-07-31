package com.proto.musicplayerproto1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

public class NotificationBuilder {
    private Context context;
    public static final int NOTIFICATION_ID = 444;
    public final String CHANNEL_ID;
    private static final int REQUEST_CODE = 501;

    private Action playAction;
    private Action pauseAction;
    private Action nextAction;
    private Action prevAction;
    private PendingIntent stopPendingIntent;

    private NotificationManager notificationManager;

    public NotificationBuilder(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        CHANNEL_ID = context.getString(R.string.channel_id);
        playAction = new Action(
                R.drawable.exo_notification_play,
                "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_PLAY));
        pauseAction = new Action(
                R.drawable.exo_notification_pause,
                "pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_PAUSE));
        nextAction = new Action(
                R.drawable.exo_notification_next,
                "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        prevAction = new Action(
                R.drawable.exo_notification_previous,
                "prev",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        stopPendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
                context, PlaybackStateCompat.ACTION_STOP);
    }

    public Notification buildNotification(MediaSessionCompat.Token sessionToken) {
        if (!isChannelExists())
            createChannel();
        MediaControllerCompat mediaController = null;
        try {
            mediaController = new MediaControllerCompat(context, sessionToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        MediaDescriptionCompat description = mediaController.getMetadata().getDescription();
        PlaybackStateCompat playbackState = mediaController.getPlaybackState();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        //playbackstate의 action값은 22bit 길이의 flag값임(따라서 action값은 long type으로 표시)
        //22bit에서 해당 flag만 1이되는 형태로 action값 지정(따라서 int를 반환하는 getState랑 & 연산했을떄 해당 플래그가 다르면 0이나오는거임ㅇㅇ)
        if((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0)
            builder.addAction(prevAction);
        if((playbackState.getState() == PlaybackStateCompat.STATE_PLAYING)
                || (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING))
            builder.addAction(pauseAction);
        else
            builder.addAction(playAction);

        if((playbackState.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0)
            builder.addAction(nextAction);

        MediaStyle musicNotiStyle = new MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(1) // **반드시 addAction으로 builder에 액션버튼을 추가한뒤 showActionsInCompactView값을 지정해야함. 안그러면 안뜸.
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopPendingIntent);

        builder.setStyle(musicNotiStyle)
                .setSmallIcon(R.drawable.ic_stat_image_audiotrack)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(description.getIconBitmap())
                .setDeleteIntent(stopPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        return builder.build();
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(context.getString(R.string.channel_description));
        channel.enableLights(true);
        notificationManager.createNotificationChannel(channel);
        notificationManager.getNotificationChannel(CHANNEL_ID);
    }

    private boolean isChannelExists() {
        return (notificationManager.getNotificationChannel(CHANNEL_ID) !=null);
    }

    private PendingIntent createContentIntent() {
        Intent intent = new Intent(context.getApplicationContext(), MusicplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
