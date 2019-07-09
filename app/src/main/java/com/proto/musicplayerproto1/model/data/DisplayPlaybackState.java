package com.proto.musicplayerproto1.model.data;

public class DisplayPlaybackState {
    private boolean isPlaying;
    private boolean isShuffle;
    private int repeatMode;

    public DisplayPlaybackState(boolean isPlaying, boolean isShuffle, int repeatMode) {
        this.isPlaying = isPlaying;
        this.isShuffle = isShuffle;
        this.repeatMode = repeatMode;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }
}
