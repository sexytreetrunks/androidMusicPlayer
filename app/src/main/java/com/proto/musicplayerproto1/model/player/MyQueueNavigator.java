package com.proto.musicplayerproto1.model.player;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;

public class MyQueueNavigator extends TimelineQueueNavigator {
    public static final int MY_MAX_POSITION_FOR_SEEK_TO_PREVIOUS = 5000;
    public MyQueueNavigator(MediaSessionCompat mediaSession) {
        super(mediaSession);
    }

    public MyQueueNavigator(MediaSessionCompat mediaSession, int maxQueueSize) {
        super(mediaSession, maxQueueSize);
    }

    @Override
    public void onSkipToPrevious(Player player) {
        Timeline timeline = player.getCurrentTimeline();
        if (timeline.isEmpty()) {
            return;
        }
        int previousWindowIndex = player.getPreviousWindowIndex();
        if (player.getCurrentPosition() > MY_MAX_POSITION_FOR_SEEK_TO_PREVIOUS
                || previousWindowIndex == C.INDEX_UNSET) {
            player.seekTo(0);
        } else {
            player.seekTo(previousWindowIndex, C.TIME_UNSET);
        }
    }

    @Override
    public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
        Object obj = player.getCurrentTag();
        return (MediaDescriptionCompat)obj;
    }
}
