package com.eficksan.customcalendar.ioc.calendar;

import com.eficksan.customcalendar.domain.calendar.FetchEventsUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUserCase;
import com.eficksan.customcalendar.presentation.calendar.CalendarPresenter;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import javax.inject.Named;

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
            FindCalendarUserCase findCalendarUserCase,
            FetchEventsUseCase fetchEventsUseCase,
            @Named("calendarName") String calendarName) {
        return new CalendarPresenter(findCalendarUserCase, fetchEventsUseCase, permissionsRequestListener, calendarName);
    }
}
