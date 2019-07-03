package com.proto.musicplayerproto1.player;

public class PlayerState {
    private int window;
    private long position;
    private boolean whenReady;

    public PlayerState() {
        window = 0;
        position = 0L;
        whenReady = true;
    }

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public boolean getWhenReady() {
        return whenReady;
    }

    public void setWhenReady(boolean whenReady) {
        this.whenReady = whenReady;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "window=" + window +
                ", position=" + position +
                ", whenReady=" + whenReady +
                '}';
    }
}
