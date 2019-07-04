package com.proto.musicplayerproto1.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ShuffleOrder;

import java.io.File;
import java.util.ArrayList;

public class MusicSourceHelper {
    private ContentResolver contentResolver;
    private static final Uri contentRootUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final String albumArtUriString = "content://media/external/audio/albumart/";

    public MusicSourceHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ArrayList<MediaMetadataCompat> getAllMusicList() {
        ArrayList<MediaMetadataCompat> musiclist = new ArrayList<>();
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = contentResolver.query(contentRootUri, projection,null,null,null);
        if(cursor!=null) {
            while (cursor.moveToNext()) {
                String musicid = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String albumid = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String albumpath = getAlbumArtPath(albumid);
                String musicpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                Uri musicUri = ContentUris.withAppendedId(contentRootUri, Integer.parseInt(musicid));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                MediaMetadataCompat media = new MediaMetadataCompat.Builder()
                                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicid)
                                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,artist)
                                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                                                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, musicUri.toString())
                                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumpath)
                                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, albumpath)
                                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                                                .build();
                musiclist.add(media);
            }
        }
        return musiclist;
    }

    public String getAlbumArtPath(String album_id) {
        Uri album_uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String selection = MediaStore.Audio.Albums._ID + "= ?";
        String[] selectionArgs = {album_id};
        String path = null;
        Cursor cursor = contentResolver.query(album_uri, projection, selection, selectionArgs, null);
        if(cursor!=null) {
            if(cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }
        }
        if(path!=null)
            return albumArtUriString + album_id;
        else
            return null;
    }
}
