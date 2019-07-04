package com.proto.musicplayerproto1.Utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {
    private TimeUtils(){}

    static public String formatTimeTommss(long sec) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(sec),
                TimeUnit.MILLISECONDS.toSeconds(sec) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sec)));
    }
}
