package com.proto.musicplayerproto1.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaControllerCompat;

public class CustomViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    private Application application;
    private MediaControllerCompat controller;

    public CustomViewModelFactory(Application application, MediaControllerCompat controller) {
        super(application);
        this.application = application;
        this.controller = controller;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T)(new MusicplayViewModel(application, controller));
    }
}
