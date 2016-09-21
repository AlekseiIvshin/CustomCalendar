package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

public class FindCalendarUseCase extends BaseUseCase<String, CalendarEntity> {

    private static final String TAG = FindCalendarUseCase.class.getSimpleName();

    private final Context mContext;
    private final CalendarEntityMapper mapper;

    private final Uri uri = Calendars.CONTENT_URI;
    private final String selection = String.format("(%s = ?)", Calendars.NAME);
    private final String[] projection = new String[]{Calendars._ID, Calendars.NAME};

    public FindCalendarUseCase(
            Context context,
            CalendarEntityMapper mapper,
            Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mContext = context;
        this.mapper = mapper;
    }

    @Override
    protected Observable<CalendarEntity> buildObservable(final String calendarNameParam) {
        Log.v(TAG, "Search calendar by name: " + calendarNameParam);
        return Observable.create(new Observable.OnSubscribe<CalendarEntity>() {
            @Override
            public void call(Subscriber<? super CalendarEntity> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "Need more permissions: " + Manifest.permission.READ_CALENDAR);
                        subscriber.onError(new PermissionRequiredException(new String[]{Manifest.permission.READ_CALENDAR}));
                        return;
                    }
                    String[] selectionArgs = new String[]{calendarNameParam};
                    Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        subscriber.onNext(mapper.mapToObject(cursor));
                    }
                    subscriber.onCompleted();
                }
            }
        });
    }
}
