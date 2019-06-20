package com.proto.musicplayerproto1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.proto.musicplayerproto1.data.Music;
import com.proto.musicplayerproto1.data.MusicSourceHelper;

public class MusicplayActivity extends AppCompatActivity {
    private ImageView iv_albumcover;
    private TextView tv_title;
    private TextView tv_artist;
    private TextView tv_albumtitle;
    private static final String PERMITION_LOG_TAG = "**AppPermission";
    private final int MY_PERMISSION_REQUEST_STORAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musicplay);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermissions();
        }
        iv_albumcover = (ImageView)findViewById(R.id.album_cover);
        tv_title = (TextView)findViewById(R.id.title);
        tv_artist = (TextView)findViewById(R.id.artist);
        tv_albumtitle = (TextView)findViewById(R.id.album_title);
        Music music = new MusicSourceHelper(getContentResolver()).getAllMusicList().get(13);
        if(music.getAlbumPath()==null)
            iv_albumcover.setImageResource(R.drawable.no_cover);
        else {
            Bitmap bitmap = BitmapFactory.decodeFile(music.getAlbumPath());
            iv_albumcover.setImageBitmap(bitmap);
        }
        tv_title.setText(music.getTitle());
        tv_title.setSelected(true);
        tv_artist.setText(music.getArtist());
        tv_artist.setSelected(true);
        tv_albumtitle.setText(music.getAlbumTitle());
        tv_artist.setSelected(true);
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to write the permission.
                Toast.makeText(this, "Read/Write external storage", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_STORAGE);

        } else {
            Log.e(PERMITION_LOG_TAG, "permission deny");
        }
    }
}
