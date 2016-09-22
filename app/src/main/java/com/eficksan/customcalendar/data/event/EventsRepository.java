package com.eficksan.customcalendar.data.event;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import java.util.LinkedList;
import java.util.List;

import static android.provider.CalendarContract.Events.CALENDAR_ID;
import static android.provider.CalendarContract.Events.CONTENT_URI;
import static android.provider.CalendarContract.Events.DESCRIPTION;
import static android.provider.CalendarContract.Events.DTEND;
import static android.provider.CalendarContract.Events.DTSTART;
import static android.provider.CalendarContract.Events.EVENT_LOCATION;
import static android.provider.CalendarContract.Events.TITLE;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

public class EventsRepository {

    private final Context mContext;

    public EventsRepository(Context mContext) {
        this.mContext = mContext;
    }

    public static EventEntity mapToObject(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
        long calendarId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_ID));
        String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
        String location = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION));
        String description = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION));
        long startAt = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
        long endAt = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
        return new EventEntity(id, calendarId, title, location, description, startAt, endAt);
    }

    public static ContentValues buildContentValues(EventEntity eventEntity) {
        ContentValues values = new ContentValues(6);
        values.put(TITLE, eventEntity.title);
        values.put(EVENT_LOCATION, eventEntity.location);
        values.put(DTSTART, eventEntity.startAt);
        values.put(DTEND, eventEntity.endAt);
        values.put(DESCRIPTION, eventEntity.description);
        values.put(CALENDAR_ID, eventEntity.calendarId);
        return values;
    }

    public List<EventEntity> fetchEvents(long calendarId, long fromTime, long toTime) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            throw new PermissionRequiredException(new String[]{Manifest.permission.READ_CALENDAR});
        }
        Uri uri = CalendarContract.Events.CONTENT_URI;
        String selection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND);
        String[] selectionArgs = new String[]{
                String.valueOf(calendarId),
                String.valueOf(fromTime),
                String.valueOf(toTime)
        };
        String[] projection = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };
        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        LinkedList<EventEntity> eventEntities = new LinkedList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                eventEntities.add(mapToObject(cursor));
            }
        }
        return eventEntities;
    }

    public Long addEvent(final EventEntity parameterEventEntity) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            throw new PermissionRequiredException(new String[]{Manifest.permission.WRITE_CALENDAR});
        }
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri insertedEventUri = contentResolver.insert(CONTENT_URI, buildContentValues(parameterEventEntity));

        if (insertedEventUri != null) {
            return Long.valueOf(insertedEventUri.getLastPathSegment());
        } else {
            return -1L;
        }
    }

}
