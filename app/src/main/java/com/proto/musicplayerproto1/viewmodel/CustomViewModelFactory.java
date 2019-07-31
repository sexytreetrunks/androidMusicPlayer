package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;

public class CustomViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    private Application application;

    public CustomViewModelFactory(Application application) {
        super(application);
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T)(new MusicplayViewModel(application));
    }
}
