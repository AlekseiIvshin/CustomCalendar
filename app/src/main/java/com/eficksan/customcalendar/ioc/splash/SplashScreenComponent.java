package com.eficksan.customcalendar.ioc.splash;

import com.eficksan.customcalendar.ioc.app.AppComponent;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenScope;
import com.eficksan.customcalendar.ioc.common.CalendarModule;
import com.eficksan.customcalendar.presentation.splash.SplashFragment;

import dagger.Component;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
@CalendarScreenScope
@Component(dependencies = AppComponent.class,
        modules = {SplashScreenModule.class, CalendarModule.class})
public interface SplashScreenComponent {

    void inject(SplashFragment fragment);
}
