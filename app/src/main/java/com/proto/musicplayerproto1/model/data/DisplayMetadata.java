package com.proto.musicplayerproto1.model.data;

public class DisplayMetadata {
    private String mediaId;
    private String albumArtUri;
    private String title;
    private String artist;
    private String album;
    private long duration;

    public DisplayMetadata(String mediaId, String albumArtUri, String title, String artist, String album, long duration) {
        this.mediaId = mediaId;
        this.albumArtUri = albumArtUri;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
    }

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "DisplayMetadata{" +
                "mediaId='" + mediaId + '\'' +
                ", albumArtUri='" + albumArtUri + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                '}';
    }
}
