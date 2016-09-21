package com.eficksan.customcalendar.ioc.common;

import android.content.Context;

import com.eficksan.customcalendar.domain.calendar.FetchEventsUserCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUserCase;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@Module
public class CalendarModule {

    @Provides
    public FindCalendarUserCase provideFindCalendarUserCase(
            Context context,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FindCalendarUserCase(context, uiScheduler, ioScheduler);
    }

    @Provides
    public FetchEventsUserCase provideFetchEventsUserCase(
            Context context,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FetchEventsUserCase(context, uiScheduler, ioScheduler);
    }

}
