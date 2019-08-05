package com.proto.musicplayerproto1.Utils.BindingUtils;

import android.databinding.BindingAdapter;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
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
    public static void setDuration(DefaultTimeBar timeBar, long duration) {
        timeBar.setDuration(duration);
    }

    @BindingAdapter("app:progress")
    public static void setProgress(DefaultTimeBar timeBar, long progress) {
        timeBar.setPosition(progress);
    }

    @BindingAdapter("app:repeatModeIcon")
    public static void repeatModeIcon(ImageButton button, int repeatMode) {
        if(repeatMode== PlaybackStateCompat.REPEAT_MODE_ALL)
            button.setImageResource(R.drawable.exo_controls_repeat_all);
        else if(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE)
            button.setImageResource(R.drawable.exo_controls_repeat_one);
        else
            Log.d("**","repeat mode not setted: "+repeatMode);
    }

    @BindingAdapter("app:onScrub")
    public static void addOnScrubListener(DefaultTimeBar timeBar, TimeBar.OnScrubListener listener) {
        timeBar.addListener(listener);
    }

    @BindingAdapter("android:text")
    public static void setText(TextView textView, String text) {
        String oldText = textView.getText().toString();
        if(!oldText.equals(text)) {
            textView.setText(text);
            textView.setSelected(true);
        }
    }
}
