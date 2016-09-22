package com.eficksan.customcalendar.presentation.splash;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.calendar.AddCalendarUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;
import com.eficksan.customcalendar.presentation.common.BasePresenter;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import rx.Subscriber;

/**
 * Created by Aleksei Ivshin
 * on 22.09.2016.
 */

public class SplashPresenter extends BasePresenter<ISplashView> implements PermissionResultListener {

    private static final int REQUEST_FIND_CALENDAR = 1;
    private static final int REQUEST_CREATE_CALENDAR = 2;

    private final FindCalendarUseCase findCalendarUseCase;
    private final AddCalendarUseCase addCalendarUseCase;
    private final String mCalendarName;
    private final PermissionsRequestListener mPermissionsRequestListener;

    private CalendarEntity mFoundCalendar;

    public SplashPresenter(
            FindCalendarUseCase findCalendarUseCase,
            AddCalendarUseCase addCalendarUseCase,
            String calendarName,
            PermissionsRequestListener mPermissionsRequestListener) {
        this.findCalendarUseCase = findCalendarUseCase;
        this.addCalendarUseCase = addCalendarUseCase;
        this.mCalendarName = calendarName;
        this.mPermissionsRequestListener = mPermissionsRequestListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        mPermissionsRequestListener.addListener(this);
        findCalendarUseCase.execute(mCalendarName, new FindCalendarSubscriber());
    }

    @Override
    public void onDestroy() {
        mPermissionsRequestListener.removeListener(this);
        addCalendarUseCase.unsubscribe();
        findCalendarUseCase.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CREATE_CALENDAR:
                if (grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    addCalendar(mCalendarName);
                } else {
                    mView.notifyUser(R.string.permission_not_granted);
                }
                break;
            case REQUEST_FIND_CALENDAR:
                if (grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    findCalendar(mCalendarName);
                } else {
                    mView.notifyUser(R.string.permission_not_granted);
                }
                break;
        }
    }

    private void findCalendar(String calendarName) {
        findCalendarUseCase.execute(calendarName, new FindCalendarSubscriber());
    }

    private void addCalendar(String calendarName) {
        addCalendarUseCase.execute(calendarName, new AddCalendaerSubscriber());
    }

    private class FindCalendarSubscriber extends Subscriber<CalendarEntity> {
        @Override
        public void onCompleted() {
            findCalendarUseCase.unsubscribe();
            if (mFoundCalendar == null) {
                addCalendar(mCalendarName);
            }
        }

        @Override
        public void onError(Throwable e) {
            findCalendarUseCase.unsubscribe();
            if (e instanceof PermissionRequiredException) {
                String[] requiredPermissions = ((PermissionRequiredException) e).requiredPermissions;
                mPermissionsRequestListener.onPermissionsRequired(requiredPermissions, REQUEST_FIND_CALENDAR);
            }
        }

        @Override
        public void onNext(CalendarEntity calendarEntity) {
            mFoundCalendar = calendarEntity;
            navigateToCalendar(mFoundCalendar.id);
        }
    }

    private class AddCalendaerSubscriber extends Subscriber<Long> {

        @Override
        public void onCompleted() {
            addCalendarUseCase.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            addCalendarUseCase.unsubscribe();
            if (e instanceof PermissionRequiredException) {
                String[] requiredPermissions = ((PermissionRequiredException) e).requiredPermissions;
                mPermissionsRequestListener.onPermissionsRequired(requiredPermissions, REQUEST_CREATE_CALENDAR);
            }
        }

        @Override
        public void onNext(Long calendarId) {
            navigateToCalendar(calendarId);
        }
    }

    private void navigateToCalendar(long calendarId) {
        if (mRouter!=null) {
            mRouter.updateCalendarIdAndShowCalendar(calendarId);
        }
    }
}
