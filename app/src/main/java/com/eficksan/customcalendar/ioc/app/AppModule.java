package com.eficksan.customcalendar.ioc.app;

import android.content.Context;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@Module
public class AppModule {
    private final Context appContext;

    public AppModule(Context appContext) {
        this.appContext = appContext;
    }

    @Provides
    @AppScope
    public Context provideContext() {
        return appContext;
    }

    @Provides
    @Named("io")
    public Scheduler provideIoScheduler() {
        return Schedulers.io();
    }

    @Provides
    @Named("job")
    public Scheduler provideJobScheduler() {
        return Schedulers.computation();
    }

    @Provides
    @Named("ui")
    public Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
