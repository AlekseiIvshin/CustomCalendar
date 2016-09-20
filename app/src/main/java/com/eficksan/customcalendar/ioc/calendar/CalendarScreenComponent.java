package com.eficksan.customcalendar.ioc.calendar;

import com.eficksan.customcalendar.ioc.app.AppComponent;
import com.eficksan.customcalendar.presentation.calendar.CalendarFragment;

import dagger.Component;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@CalendarScreenScope
@Component(dependencies = AppComponent.class,
modules = {CalendarScreenModule.class})
public interface CalendarScreenComponent {

    void inject(CalendarFragment calendarFragment);
}
