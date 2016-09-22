package com.eficksan.customcalendar.ioc.addevent;

import com.eficksan.customcalendar.ioc.app.AppComponent;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenScope;
import com.eficksan.customcalendar.ioc.common.CalendarModule;
import com.eficksan.customcalendar.presentation.addingevent.AddEventFragment;

import dagger.Component;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@CalendarScreenScope
@Component(dependencies = AppComponent.class,
        modules = {AddEventScreenModule.class, CalendarModule.class})
public interface AddEventScreenComponent {

    void inject(AddEventFragment fragment);
}
