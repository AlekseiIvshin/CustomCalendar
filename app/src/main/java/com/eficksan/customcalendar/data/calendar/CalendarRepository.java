package com.eficksan.customcalendar.data.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

/**
 * Created by Aleksei Ivshin
 * on 22.09.2016.
 */

public class CalendarRepository {

    private final Context mContext;

    private final Uri uri = CalendarContract.Calendars.CONTENT_URI;
    private final String selection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
    private final String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME};

    public CalendarRepository(Context context) {
        this.mContext = context;
    }

    public static CalendarEntity mapToObject(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
        return new CalendarEntity(id, displayName);
    }

    public static Uri buildCalUri(String calendarName) {
        return CalendarContract.Calendars.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, calendarName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }

    public static ContentValues buildContentValues(String calendarName, Context context) {
        final ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, calendarName);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(CalendarContract.Calendars.NAME, calendarName);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarName);
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, ContextCompat.getColor(context, R.color.calendar_color));  //Calendar.getColor() returns int
        cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        cv.put(CalendarContract.Calendars.VISIBLE, 1);
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        return cv;
    }

    public CalendarEntity findCalendar(String calendarName) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            throw new PermissionRequiredException(new String[]{Manifest.permission.READ_CALENDAR});
        }
        String[] selectionArgs = new String[]{calendarName};
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
        if (cursor == null) {
            return null;
        }
        CalendarEntity entity = null;
        if (cursor.moveToFirst()) {
            entity = mapToObject(cursor);
        }
        cursor.close();
        return entity;
    }

    public long addCalendar(String name) {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri insertedCalendar = contentResolver.insert(buildCalUri(name), buildContentValues(name, mContext));
        if (insertedCalendar == null) {
            return -1L;
        } else {
            return Long.valueOf(uri.getLastPathSegment());
        }
    }
}
