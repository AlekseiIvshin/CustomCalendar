package com.eficksan.customcalendar.ioc.calendar;

import com.eficksan.customcalendar.domain.calendar.FindCalendarUserCase;
import com.eficksan.customcalendar.presentation.calendar.CalendarPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

@Module
public class CalendarScreenModule {

    @Provides
    @CalendarScreenScope
    public CalendarPresenter provideCalendarPresenter(FindCalendarUserCase findCalendarUserCase) {
        return new CalendarPresenter(findCalendarUserCase, mFetchEventsUserCase);
    }
}
