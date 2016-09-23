package com.eficksan.customcalendar.ioc.common;

import android.content.Context;

import com.eficksan.customcalendar.data.calendar.CalendarRepository;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.calendar.AddCalendarUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;
import com.eficksan.customcalendar.domain.events.AddEventUseCase;
import com.eficksan.customcalendar.domain.events.EventsChangesUseCase;
import com.eficksan.customcalendar.domain.events.FetchEventsUseCase;

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
    public EventsRepository provideEventsRepository(Context context) {
        return new EventsRepository(context);
    }

    @Provides
    public CalendarRepository provideCalendarRepository(Context context) {
        return new CalendarRepository(context);
    }

    @Provides
    public FindCalendarUseCase provideFindCalendarUserCase(
            CalendarRepository calendarRepository,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new FindCalendarUseCase(calendarRepository, uiScheduler, ioScheduler);
    }

    @Provides
    public AddCalendarUseCase provideAddCalendarUseCase(
            CalendarRepository calendarRepository,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new AddCalendarUseCase(calendarRepository, uiScheduler, ioScheduler);
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

    @Provides
    public EventsChangesUseCase provideEventsChangesUseCase(
            Context context,
            @Named("io") Scheduler ioScheduler, @Named("ui") Scheduler uiScheduler) {
        return new EventsChangesUseCase(context, ioScheduler, uiScheduler);
    }
}
