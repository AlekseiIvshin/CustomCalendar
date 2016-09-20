package com.eficksan.customcalendar.data.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class CalendarRepository {

    private final Context mContext;

    public CalendarRepository(Context mContext) {
        this.mContext = mContext;
    }

    public Observable<CalendarEntity> fetchCalendarByName(final String calendarName) {
        return Observable.just(calendarName)
                .subscribeOn(Schedulers.io())
                .map(new Func1<String, Cursor>() {
                    @Override
                    public Cursor call(String s) {
                        ContentResolver cr = mContext.getContentResolver();
                        Uri uri = CalendarContract.Calendars.CONTENT_URI;
                        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
// Submit the query and get a Cursor object back.
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            throw new SecurityException("Needs permissions: " + Manifest.permission.READ_CALENDAR);
                        }
                        return cr.query(uri, new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME}, null, null, null);
                    }
                })
                .flatMap(new Func1<Cursor, Observable<CalendarEntity>>() {
                    @Override
                    public Observable<CalendarEntity> call(Cursor cursor) {
                        if (cursor == null) {
                            throw new IllegalArgumentException("There are not any calednars named "+calendarName);
                        }
                        if (cursor.moveToNext()) {
                            long id = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID));
                            String displayName = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
//                            return new CalendarEntity(id, displayName);
                        } else {

                        }
                        return null;
                    }
                });
    }
}
