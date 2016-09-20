package com.eficksan.customcalendar.data.calendar;

import android.database.Cursor;
import android.provider.CalendarContract;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class CalendarEntityMapper {

    public static CalendarEntity mapToObject(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Calendars._ID));
        String displayName = cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME));
        return new CalendarEntity(id, displayName);
    }
}
