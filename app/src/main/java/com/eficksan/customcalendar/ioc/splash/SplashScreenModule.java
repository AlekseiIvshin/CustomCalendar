package com.eficksan.customcalendar.ioc.splash;

import com.eficksan.customcalendar.domain.calendar.AddCalendarUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenScope;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;
import com.eficksan.customcalendar.presentation.splash.SplashPresenter;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

@Module
public class SplashScreenModule {

    private final PermissionsRequestListener permissionsRequestListener;

    public SplashScreenModule(PermissionsRequestListener permissionsRequestListener) {
        this.permissionsRequestListener = permissionsRequestListener;
    }

    @Provides
    @CalendarScreenScope
    public SplashPresenter provideSplashPresenter(
            FindCalendarUseCase findCalendarUseCase,
            AddCalendarUseCase addCalendarUseCase,
            @Named("calendarName") String calendarName) {
        return new SplashPresenter(findCalendarUseCase, addCalendarUseCase, calendarName, permissionsRequestListener);
    }
}
