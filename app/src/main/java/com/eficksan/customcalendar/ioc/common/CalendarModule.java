package com.eficksan.customcalendar.ioc.common;

import android.content.Context;

import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.domain.calendar.FetchEventsUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;

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
    public EventEntityMapper provideEntityMapper() {
        return new EventEntityMapper();
    }
    @Provides
    public CalendarEntityMapper provideCalendarMapper() {
        return new CalendarEntityMapper();
    }

    @Provides
    public FindCalendarUseCase provideFindCalendarUserCase(
            Context context,
            CalendarEntityMapper mapper,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FindCalendarUseCase(context, mapper, uiScheduler, ioScheduler);
    }

    @Provides
    public FetchEventsUseCase provideFetchEventsUserCase(
            Context context,
            EventEntityMapper mapper,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FetchEventsUseCase(context, mapper, uiScheduler, ioScheduler);
    }

}
