package com.eficksan.customcalendar.ioc.common;

import android.content.Context;

import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.domain.calendar.FetchEventsUseCase;
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
    public CalendarEntityMapper provideMapper() {
        return new CalendarEntityMapper();
    }

    @Provides
    public FindCalendarUserCase provideFindCalendarUserCase(
            Context context,
            CalendarEntityMapper mapper,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FindCalendarUserCase(context, mapper, uiScheduler, ioScheduler);
    }

    @Provides
    public FetchEventsUseCase provideFetchEventsUserCase(
            Context context,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FetchEventsUseCase(context, uiScheduler, ioScheduler);
    }

}
