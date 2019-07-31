package com.proto.musicplayerproto1.model.player;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;

import java.util.ArrayList;
import java.util.List;

public class MusicPlaybackPreparer implements MediaSessionConnector.PlaybackPreparer {
    private ExoPlayer exoPlayer;
    private Context context;

    public MusicPlaybackPreparer(ExoPlayer exoPlayer, Context context) {
        this.exoPlayer = exoPlayer;
        this.context = context;
    }

    @Override
    public long getSupportedPrepareActions() {
        return (PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID | PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH | PlaybackStateCompat.ACTION_PREPARE);
    }

    @Override
    public void onPrepare() {
        // prepare all media sources
        ArrayList<MediaMetadataCompat> list = new MusicSourceHelper(context.getContentResolver()).getAllMusicList();
        MediaSource mediaSource = buildMediaSource(list);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.seekTo(0,0L);
        Log.d("++","onPrepare");
    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {
        // prepare only one sources
    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {
        // prepare media sources by query
        // query by
    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {

    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }

    private MediaSource buildMediaSource(List<MediaMetadataCompat> playSrcList) {
        List<MediaSource> uriList = new ArrayList<MediaSource>();
        for(MediaMetadataCompat m: playSrcList) {
            uriList.add(createExtractorMediaSource(Uri.parse(m.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)), m.getDescription()));
        }
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSources(uriList);
        return (new LoopingMediaSource(concatenatingMediaSource));
    }

    private MediaSource createExtractorMediaSource(Uri mediaUri, MediaDescriptionCompat description) {
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(context, "exoplayer-learning"))
                .setTag(description)
                .createMediaSource(mediaUri);
    }
}
