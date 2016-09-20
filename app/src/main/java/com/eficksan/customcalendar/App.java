package com.eficksan.customcalendar;

import android.app.Application;

import com.eficksan.customcalendar.ioc.app.AppComponent;
import com.eficksan.customcalendar.ioc.app.AppModule;
import com.eficksan.customcalendar.ioc.app.DaggerAppComponent;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenModule;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenComponent;
import com.eficksan.customcalendar.ioc.calendar.DaggerCalendarScreenComponent;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class App extends Application {

    AppComponent appComponent;
    CalendarScreenComponent calendarScreenComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public CalendarScreenComponent plusCalendarScreenComponent() {
        if (calendarScreenComponent == null) {
            calendarScreenComponent = DaggerCalendarScreenComponent.builder()
                    .appComponent(appComponent)
                    .calendarModule(new CalendarScreenModule())
                    .build();
        }
        return calendarScreenComponent;
    }


    public void removeCalendarScreenComponent() {
        calendarScreenComponent = null;
    }
}
