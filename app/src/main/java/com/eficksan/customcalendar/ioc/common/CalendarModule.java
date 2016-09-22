package com.eficksan.customcalendar.ioc.common;

import android.content.Context;

import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.events.AddEventUseCase;
import com.eficksan.customcalendar.domain.events.FetchEventsUseCase;
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
    public EventsRepository prodiveEventsRepository(Context context) {
        return new EventsRepository(context);
    }

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
            EventsRepository eventsRepository,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FetchEventsUseCase(eventsRepository, uiScheduler, ioScheduler);
    }

    @Provides
    public AddEventUseCase provideAddEventUseCase(
            EventsRepository eventsRepository,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new AddEventUseCase(eventsRepository, uiScheduler, ioScheduler);
    }

}
