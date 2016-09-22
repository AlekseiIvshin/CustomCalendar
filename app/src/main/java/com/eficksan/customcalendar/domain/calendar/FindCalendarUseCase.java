package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.eficksan.customcalendar.R;
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

    private static Uri buildCalUri(String calendarName) {
        return CalendarContract.Calendars.CONTENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, calendarName)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }


    private static ContentValues buildContentValues(String calendarName, Context context) {
        final ContentValues cv = new ContentValues();
        cv.put(Calendars.ACCOUNT_NAME, calendarName);
        cv.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(Calendars.NAME, calendarName);
        cv.put(Calendars.CALENDAR_DISPLAY_NAME, calendarName);
        cv.put(Calendars.CALENDAR_COLOR, ContextCompat.getColor(context, R.color.calendar_color));  //Calendar.getColor() returns int
        cv.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
//        cv.put(Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        cv.put(Calendars.VISIBLE, 1);
        cv.put(Calendars.SYNC_EVENTS, 1);
        return cv;
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
                    ContentResolver contentResolver = mContext.getContentResolver();
                    Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        Log.v(TAG, "Calendar found: " + calendarNameParam);
                        subscriber.onNext(mapper.mapToObject(cursor));
                        cursor.close();
                    } else {
                        Log.v(TAG, "Calendar not found: " + calendarNameParam);
                        Uri insertedCalendar = contentResolver.insert(buildCalUri(calendarNameParam), buildContentValues(calendarNameParam, mContext));
                        Log.v(TAG, "Calendar added: " + insertedCalendar);
                        cursor = contentResolver.query(insertedCalendar, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            subscriber.onNext(mapper.mapToObject(cursor));
                            cursor.close();
                        }
                    }

                    subscriber.onCompleted();
                }
            }
        });
    }
}
