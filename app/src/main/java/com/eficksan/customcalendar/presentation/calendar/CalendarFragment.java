package com.eficksan.customcalendar.presentation.calendar;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenComponent;
import com.eficksan.customcalendar.presentation.common.ComponentLifeCycleDelegate;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.entity.Event;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.Manifest.permission.WRITE_CALENDAR;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CalendarFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,ComponentLifeCycleDelegate.ComponentProvider<CalendarScreenComponent> {
    public static final String TAG = CalendarFragment.class.getSimpleName();
    private static final int CALENDAR_PERMISSION = 42;

    private static final int CALENDAR_FETCHING_LOADER_ID = 1;
    private static final int EVENTS_FETCHING_LOADER_ID = 2;
    private static final String EXTRA_CALENDAR_ID = "EXTRA_CALENDAR_ID";

    @Bind(R.id.calendar)
    FlexibleCalendarView mCalendarView;

    private ComponentLifeCycleDelegate<ICalendarView, CalendarPresenter, CalendarScreenComponent> lifeCycleDelegate;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lifeCycleDelegate = new ComponentLifeCycleDelegate<>(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        fetchCalendars();

        final List<Event> eventColorList = getEventColorList();

        mCalendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<? extends Event> getEventsForTheDay(int year, int month, int day) {
                return eventColorList;
            }
        });
    }

    private void fetchCalendars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && PERMISSION_GRANTED != getActivity().checkSelfPermission(WRITE_CALENDAR)) {
            requestPermissions(new String[]{WRITE_CALENDAR}, CALENDAR_PERMISSION);
        } else {
            getLoaderManager().initLoader(CALENDAR_FETCHING_LOADER_ID, null, this);
        }
    }

    private List<Event> getEventColorList() {
        Random random = new Random();
        int eventCount = random.nextInt(4) + 1;

        ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            events.add(new MyEvent());
        }
        return events;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CALENDAR_FETCHING_LOADER_ID: {
                Uri uri = CalendarContract.Calendars.CONTENT_URI;
                String selection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
                String[] selectionArgs = new String[]{"eficksan@gmail.com"};
                String[] projection = new String[]{CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME};
                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
            }
            case EVENTS_FETCHING_LOADER_ID: {
                DateTime fromDate = DateTime.now();
                DateTime toDate = fromDate.plusMonths(1);
                long calendarId = args.getLong(EXTRA_CALENDAR_ID);
                Log.v(TAG, String.format("Request events for calendar (id = %d) from %s to %s", calendarId, fromDate, toDate));

                Uri uri = CalendarContract.Events.CONTENT_URI;
                String selection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                        CalendarContract.Events.CALENDAR_ID,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND);
                String[] selectionArgs = new String[]{
                        String.valueOf(calendarId),
                        String.valueOf(fromDate.getMillis()),
                        String.valueOf(toDate.getMillis())
                };
                String[] projection = new String[]{
                        CalendarContract.Events._ID,
                        CalendarContract.Events.CALENDAR_ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DESCRIPTION,
                        CalendarContract.Events.EVENT_LOCATION,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND};
                return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, null);
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cur) {
        switch (loader.getId()) {
            case CALENDAR_FETCHING_LOADER_ID:
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        CalendarEntity calendarEntity = CalendarEntityMapper.mapToObject(cur);
                        Log.v(TAG, String.format("Found calendar: id=%d, display name = '%s'", calendarEntity.id, calendarEntity.displayName));
                        Bundle args = new Bundle();
                        args.putLong(EXTRA_CALENDAR_ID, calendarEntity.id);
                        getLoaderManager().initLoader(EVENTS_FETCHING_LOADER_ID, args, this);
                    }
                }
                break;
            case EVENTS_FETCHING_LOADER_ID:
                if (cur != null) {
                    EventEntity eventEntity;
                    while (cur.moveToNext()) {
                        eventEntity = EventEntityMapper.mapToObject(cur);
                        Log.v(TAG, eventEntity.toString());
                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public CalendarScreenComponent onSetUpComponent() {
        return null;
    }

    @Override
    public void onKillComponent() {

    }

    private class MyEvent implements Event {

        @Override
        public int getColor() {
            return R.color.calendar_event;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (CALENDAR_PERMISSION == requestCode) {
            fetchCalendars();
        }
    }
}
