package com.eficksan.customcalendar.ioc.calendar;

import com.eficksan.customcalendar.domain.events.EventsChangesUseCase;
import com.eficksan.customcalendar.domain.events.FetchEventsUseCase;
import com.eficksan.customcalendar.presentation.calendar.CalendarPresenter;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

@Module
public class CalendarScreenModule {

    private final PermissionsRequestListener permissionsRequestListener;

    public CalendarScreenModule(PermissionsRequestListener permissionsRequestListener) {
        this.permissionsRequestListener = permissionsRequestListener;
    }

    @Provides
    @CalendarScreenScope
    public CalendarPresenter provideCalendarPresenter(
            FetchEventsUseCase fetchEventsUseCase, EventsChangesUseCase eventsChangesUseCase) {
        return new CalendarPresenter(fetchEventsUseCase, permissionsRequestListener, eventsChangesUseCase);
    }
}
