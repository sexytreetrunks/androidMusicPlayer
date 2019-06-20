package com.proto.musicplayerproto1;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.proto.musicplayerproto1.data.Music;
import com.proto.musicplayerproto1.data.MusicSourceHelper;

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
        ArrayList<Music> list = new MusicSourceHelper(context.getContentResolver()).getAllMusicList();
        Log.d("**","list size: "+list.size());
    }
}
