package com.proto.musicplayerproto1.Utils.BindingUtils;

import android.databinding.BindingAdapter;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.proto.musicplayerproto1.R;

public class BindingAdapters {
    @BindingAdapter("app:albumArt")
    public static void setAlbumArt(ImageView view, String albumArtUri) {
        if(albumArtUri==null)
            view.setImageResource(R.drawable.no_cover);
        else
            view.setImageURI(Uri.parse(albumArtUri));
    }

    @BindingAdapter("app:playIcon")
    public static void playIcon(ImageButton button, boolean isPlaying) {
        if(isPlaying)
            button.setImageResource(R.drawable.exo_controls_pause);
        else
            button.setImageResource(R.drawable.exo_controls_play);
    }

    @BindingAdapter("app:duration")
    public static void setDuration(DefaultTimeBar timeBar, Long duration) {
        timeBar.setDuration(duration);
    }

    @BindingAdapter("app:progress")
    public static void setProgress(DefaultTimeBar timeBar, Long position) {
        timeBar.setPosition(position);
    }
}
