package com.eficksan.customcalendar.data.calendar;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.CalendarContract;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

@Deprecated
public class EventEntityMapper implements EntityMapper<EventEntity>{

    public EventEntity mapToObject(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
        long calendarId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
        String location = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
        String description = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
        long startAt = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
        long endAt = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
        return new EventEntity(id, calendarId, title, location, description, startAt, endAt);
    }

    @Override
    public ContentValues mapToContentValues(EventEntity entity) {
        return null;
    }
}
