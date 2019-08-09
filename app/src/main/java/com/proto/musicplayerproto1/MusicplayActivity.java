package com.proto.musicplayerproto1;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.proto.musicplayerproto1.databinding.ActivityMusicplayBinding;
import com.proto.musicplayerproto1.model.player.PlayerHolder;
import com.proto.musicplayerproto1.ui.carousel.CarouselPagerAdapter;
import com.proto.musicplayerproto1.ui.coverflow.CoverTransformer;
import com.proto.musicplayerproto1.viewmodel.MusicplayViewModel;

import java.util.List;
import java.util.stream.Collectors;

public class MusicplayActivity extends AppCompatActivity {
    private CarouselPagerAdapter adapter;
    private MusicplayViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            checkPermissions();
        }

        Log.d("**","onCreated");
        viewModel = ViewModelProviders.of(this).get(MusicplayViewModel.class);
        ActivityMusicplayBinding binding = DataBindingUtil.setContentView(MusicplayActivity.this, R.layout.activity_musicplay);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(MusicplayActivity.this);

        adapter = new CarouselPagerAdapter(viewModel.getDataList().getValue()
                                            .stream()
                                            .map(m->m.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                                            .collect(Collectors.toList()));
        binding.overlapPager.setAdapter(adapter);
        binding.overlapPager.setPageTransformer(false, new CoverTransformer(0.3f, -70, 0f, 0.5f));

        viewModel.getCurrentDataPosition().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {//coverflow -> player
                MediaControllerCompat controller = viewModel.getController();
                if(controller!=null)
                    controller.getTransportControls().skipToQueueItem(integer);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("**","onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("**","onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("**","activity destroyed");
    }

    private void checkPermissions() {
        String PERMITION_LOG_TAG = "**AppPermission";
        int MY_PERMISSION_REQUEST_STORAGE = 100;
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


