package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class FindCalendarUserCase extends BaseUseCase<String, CalendarEntity> {

    private final Context mContext;

    public FindCalendarUserCase(Context context, Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mContext = context;
    }

    @Override
    protected Observable<CalendarEntity> buildObservable(String calendarName) {
        return Observable.just(calendarName)
                .map(new Func1<String, Cursor>() {
                    @Override
                    public Cursor call(String s) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            throw new SecurityException("Permissions required: " + Manifest.permission.READ_CALENDAR);
                        }
                        Uri uri = CalendarContract.Calendars.CONTENT_URI;
                        String selection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
                        String[] selectionArgs = new String[]{"eficksan@gmail.com"};
                        String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME};
                        return mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                    }
                })
                .takeUntil(new Func1<Cursor, Boolean>() {
                    @Override
                    public Boolean call(Cursor cursor) {
                        return cursor != null && cursor.moveToNext();
                    }
                })
                .map(new Func1<Cursor, CalendarEntity>() {
                    @Override
                    public CalendarEntity call(Cursor cursor) {
                        return CalendarEntityMapper.mapToObject(cursor);
                    }
                });
    }
}
