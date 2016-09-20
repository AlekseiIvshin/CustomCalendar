package com.eficksan.customcalendar.ioc.app;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

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
}
