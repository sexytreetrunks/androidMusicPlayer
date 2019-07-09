package com.proto.musicplayerproto1;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.proto.musicplayerproto1.data.Music;
import com.proto.musicplayerproto1.model.data.MusicSourceHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.proto.musicplayerproto1", appContext.getPackageName());
    }

    @Test
    public void dbtest() {
        Context context = InstrumentationRegistry.getTargetContext();
        ArrayList<MediaMetadataCompat> list = new MusicSourceHelper(context.getContentResolver()).getAllMusicList();
        Log.d("**","list size: "+list.size());
        list.forEach(m->{
            if(m.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)!=null) {
                Uri uri = Uri.parse(m.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI));
                Log.d("**", uri.toString());
            } else {
                Log.d("**", "no album art");
            }
            MediaDescriptionCompat description = m.getDescription();
            Log.d("**", description.getMediaId() +", " + description.getTitle() + ", "+ description.getMediaUri().toString());
        });
    }
}
