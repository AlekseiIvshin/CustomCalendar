package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class FetchEventsUserCase extends BaseUseCase<EventsRequest, EventEntity> {

    private static final String TAG = FetchEventsUserCase.class.getSimpleName();
    private final Context mContext;

    public FetchEventsUserCase(Context context, Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mContext = context;
    }

    @Override
    protected Observable<EventEntity> buildObservable(final EventsRequest paramEventsRequest) {
        Log.v(TAG, "Request events: " + paramEventsRequest);
        return Observable.create(new Observable.OnSubscribe<EventEntity>() {
            @Override
            public void call(Subscriber<? super EventEntity> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        throw new PermissionRequiredException(new String[]{Manifest.permission.READ_CALENDAR});
                    }
                    Uri uri = Events.CONTENT_URI;
                    String selection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                            Events.CALENDAR_ID,
                            Events.DTSTART,
                            Events.DTEND);
                    String[] selectionArgs = new String[]{
                            String.valueOf(paramEventsRequest.calendarId()),
                            String.valueOf(paramEventsRequest.fromDate()),
                            String.valueOf(paramEventsRequest.toDate())
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
                    Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);

                    if (cursor != null) {
                        EventEntity eventEntity;
                        while (cursor.moveToNext()) {
                            eventEntity = EventEntityMapper.mapToObject(cursor);
                            Log.v(TAG, "Found entity: " + eventEntity.toString());
                            subscriber.onNext(eventEntity);
                        }
                    }
                    subscriber.onCompleted();
                }

            }
        });
    }
}
