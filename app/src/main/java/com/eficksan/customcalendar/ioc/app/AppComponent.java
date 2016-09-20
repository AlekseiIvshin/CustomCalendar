package com.eficksan.customcalendar.ioc.app;

import javax.inject.Named;

import dagger.Component;
import rx.Scheduler;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@AppScope
@Component(modules = {AppModule.class})
public interface AppComponent {

    @Named("io")
    Scheduler ioScheduler();

    @Named("job")
    Scheduler jobScheduler();

    @Named("ui")
    Scheduler uiScheduler();
}
