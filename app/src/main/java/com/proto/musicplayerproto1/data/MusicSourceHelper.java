package com.proto.musicplayerproto1.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ShuffleOrder;

import java.util.ArrayList;

public class MusicSourceHelper {
    private ContentResolver contentResolver;
    private static final Uri contentRootUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public MusicSourceHelper(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public ArrayList<Music> getAllMusicList() {
        ArrayList<Music> musiclist = new ArrayList<>();
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
                Music music = new Music();
                music.setId(musicid);
                music.setTitle(title);
                music.setArtist(artist);
                music.setAlbumTitle(album);
                music.setAlbumPath(albumpath);
                music.setFilepath(musicpath);
                music.setUriStr(musicUri.toString());
                music.setDuration(duration);
                musiclist.add(music);
            }
        }
        return musiclist;
    }

    public String getAlbumArtPath(String album_id) {
        Uri album_uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Albums.ALBUM_ART};
        String selection = MediaStore.Audio.Albums._ID + "= ?";
        String[] selectionArgs = {album_id};
        String path = "";
        Cursor cursor = contentResolver.query(album_uri, projection, selection, selectionArgs, null);
        if(cursor!=null) {
            if(cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            }
        }
        return path;
    }
}
