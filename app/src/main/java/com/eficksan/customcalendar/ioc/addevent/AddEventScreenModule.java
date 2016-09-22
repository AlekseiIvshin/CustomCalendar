package com.eficksan.customcalendar.ioc.addevent;

import com.eficksan.customcalendar.domain.events.AddEventUseCase;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenScope;
import com.eficksan.customcalendar.presentation.addingevent.AddEventPresenter;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

@Module
public class AddEventScreenModule {

    private final PermissionsRequestListener permissionsRequestListener;

    public AddEventScreenModule(PermissionsRequestListener permissionsRequestListener) {
        this.permissionsRequestListener = permissionsRequestListener;
    }

    @Provides
    @CalendarScreenScope
    public AddEventPresenter providePresenter(AddEventUseCase addEventUseCase) {
        return new AddEventPresenter(addEventUseCase, permissionsRequestListener);
    }
}
