package com.proto.musicplayerproto1.model.data;

import android.content.ContentResolver;
import android.support.v4.media.MediaMetadataCompat;

import java.util.List;

public class MusicSourceService {
    // activity생성 -> viewmodel생성 ->
    // -> 데이터 캐싱
    //                -> coverflow 초기화
    //                -> player 초기화
    private MusicSourceHelper musicSourceHelper;
    private List<MediaMetadataCompat> musicList;

    public MusicSourceService(ContentResolver contentResolver) {
        musicSourceHelper = new MusicSourceHelper(contentResolver);
    }


}
