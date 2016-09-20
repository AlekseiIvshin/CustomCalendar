package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class FetchEventsUserCase extends BaseUseCase<MonthEventsRequest, EventEntity> {

    private final Context mContext;

    public FetchEventsUserCase(Context context, Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mContext = context;
    }

    @Override
    protected Observable<EventEntity> buildObservable(MonthEventsRequest monthEventsRequest) {
        return Observable.just(monthEventsRequest)
                .map(new Func1<MonthEventsRequest, Cursor>() {
                    @Override
                    public Cursor call(MonthEventsRequest monthEventsRequest) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            throw new SecurityException("Permissions required: " + Manifest.permission.READ_CALENDAR);
                        }
                        Uri uri = Events.CONTENT_URI;
                        String selection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                                Events.CALENDAR_ID,
                                Events.DTSTART,
                                Events.DTEND);
                        String[] selectionArgs = new String[]{
                                String.valueOf(monthEventsRequest.calendarId),
                                String.valueOf(monthEventsRequest.fromDate),
                                String.valueOf(monthEventsRequest.toDate)
                        };
                        String[] projection = new String[]{
                                Events._ID,
                                Events.CALENDAR_ID,
                                Events.TITLE,
                                Events.DESCRIPTION,
                                Events.EVENT_LOCATION,
                                Events.DTSTART,
                                Events.DTEND
                                };
                        return mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                    }
                })
                .takeUntil(new Func1<Cursor, Boolean>() {
                    @Override
                    public Boolean call(Cursor cursor) {
                        return cursor != null && cursor.moveToNext();
                    }
                })
                .map(new Func1<Cursor, EventEntity>() {
                    @Override
                    public EventEntity call(Cursor cursor) {
                        return EventEntityMapper.mapToObject(cursor);
                    }
                });
    }
}
