package com.eficksan.customcalendar.ioc.calendar;

import com.eficksan.customcalendar.presentation.calendar.CalendarPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

@Module
public class CalendarModule {

    @Provides
    @CalendarScreenScope
    public CalendarPresenter provideCalendarPresenter() {
        return new CalendarPresenter();
    }
}
