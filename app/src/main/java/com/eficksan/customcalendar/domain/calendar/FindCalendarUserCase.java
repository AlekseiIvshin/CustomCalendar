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
import rx.functions.Func1;

public class FindCalendarUserCase extends BaseUseCase<String, CalendarEntity> {

    private static final String TAG = FindCalendarUserCase.class.getSimpleName();
    private final Context mContext;

    private final Uri uri = Calendars.CONTENT_URI;
    private final String selection = String.format("(%s = ?)", Calendars.NAME);
    private final String[] projection = new String[]{Calendars._ID, Calendars.NAME};

    public FindCalendarUserCase(Context context, Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mContext = context;
    }

    @Override
    protected Observable<CalendarEntity> buildObservable(final String calendarNameParam) {
        Log.v(TAG, "Search calendar by name: " + calendarNameParam);
        return Observable.just(calendarNameParam)
                .map(new Func1<String, Cursor>() {
                    @Override
                    public Cursor call(String calendarName) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                            Log.e(TAG, "Need more permissions: " + Manifest.permission.READ_CALENDAR);
                            throw new PermissionRequiredException(new String[]{Manifest.permission.READ_CALENDAR});
                        }
                        String[] selectionArgs = new String[]{calendarName};
                        return mContext.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                    }
                })
                .map(new Func1<Cursor, CalendarEntity>() {
                    @Override
                    public CalendarEntity call(Cursor cursor) {
                        if (cursor != null && cursor.moveToFirst()) {
                            CalendarEntity calendarEntity = CalendarEntityMapper.mapToObject(cursor);
                            Log.v(TAG, "Found calendar: " + calendarEntity.toString());
                            return calendarEntity;
                        }
                        Log.v(TAG, "Not found calendar by name = " + calendarNameParam);
                        return null;
                    }
                });
    }
}
