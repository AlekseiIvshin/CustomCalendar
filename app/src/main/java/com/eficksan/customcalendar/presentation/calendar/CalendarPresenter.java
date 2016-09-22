package com.eficksan.customcalendar.presentation.calendar;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.events.FetchEventsUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;
import com.eficksan.customcalendar.domain.events.MonthEventsRequest;
import com.eficksan.customcalendar.presentation.common.BasePresenter;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
public class CalendarPresenter extends BasePresenter<ICalendarView> implements PermissionResultListener {
    private static final String TAG = CalendarPresenter.class.getSimpleName();

    private static final String EXTRA_LAST_SHOW_DATE = "EXTRA_LAST_SHOW_DATE";
    private static final int REQUEST_FETCH_EVENTS = 1;
    private static final int REQUEST_FIND_CALENDAR = 2;
    private DateTime mTargetDate;
    private long mCalendarId = -1;
    private CompositeSubscription mViewEventsSubscription;

    private final FindCalendarUseCase mFindCalendarUseCase;
    private final FetchEventsUseCase mFetchEventsUseCase;
    private final PermissionsRequestListener mPermissionsRequestListener;
    private final String mTargetCalendarName;

    public CalendarPresenter(
            FindCalendarUseCase findCalendarUseCase,
            FetchEventsUseCase fetchEventsUseCase,
            PermissionsRequestListener permissionsRequestListener,
            String targetCalendarName) {
        this.mFindCalendarUseCase = findCalendarUseCase;
        this.mFetchEventsUseCase = fetchEventsUseCase;
        this.mPermissionsRequestListener = permissionsRequestListener;
        mTargetCalendarName = targetCalendarName;
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        mPermissionsRequestListener.addListener(this);
        if (savedInstanceStates == null) {
            mTargetDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0);
        } else {
            long savedLastTime = savedInstanceStates.getLong(EXTRA_LAST_SHOW_DATE);
            mTargetDate = new DateTime(savedLastTime);
        }
        findCalendar();
    }

    @Override
    public void onViewCreated(ICalendarView view) {
        super.onViewCreated(view);
        subscribeOnViewEvents();
    }

    @Override
    public void onSaveInstanceState(Bundle states) {
        super.onSaveInstanceState(states);
        states.putLong(EXTRA_LAST_SHOW_DATE, mTargetDate.getMillis());
    }

    @Override
    public void onViewDestroyed() {
        mViewEventsSubscription.unsubscribe();
        mViewEventsSubscription.clear();
        mViewEventsSubscription = null;
        super.onViewDestroyed();
    }

    @Override
    public void onDestroy() {
        mPermissionsRequestListener.removeListener(this);
        mFindCalendarUseCase.unsubscribe();
        mFetchEventsUseCase.unsubscribe();
        super.onDestroy();
    }

    private void subscribeOnViewEvents() {
        mViewEventsSubscription = new CompositeSubscription();

        mViewEventsSubscription.add(mView.getShownMonthChanges()
                .subscribe(new Action1<DateTime>() {
                    @Override
                    public void call(DateTime dateTime) {
                        mTargetDate = dateTime;
                        if (mCalendarId > 0) {
                            fetchEventsForMonth();
                        }
                    }
                }));

        mViewEventsSubscription.add(mView.getSelectedDateTimeChanges()
                .subscribe(new Action1<DateTime>() {
                    @Override
                    public void call(DateTime dateTime) {
                        if (mView != null) {
                            mTargetDate = dateTime;
                        }
                    }
                }));
    }

    private void findCalendar() {
        mFindCalendarUseCase.execute(mTargetCalendarName, new FoundCalendarSubscriber());
    }

    private void fetchEventsForMonth() {
        if (mCalendarId > 0) {
            MonthEventsRequest eventsRequest = MonthEventsRequest
                    .createNew(mCalendarId, mTargetDate);

            mFetchEventsUseCase.execute(eventsRequest, new FetchEventsSubscriber());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_FETCH_EVENTS:
                if (grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    fetchEventsForMonth();
                } else {
                    mView.notifyUser(R.string.permission_not_granted);
                }
                break;
            case REQUEST_FIND_CALENDAR:
                if (grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                    findCalendar();
                } else {
                    mView.notifyUser(R.string.permission_not_granted);
                }
                break;
        }
    }

    private class FoundCalendarSubscriber extends Subscriber<CalendarEntity> {

        @Override
        public void onCompleted() {
            Log.v(TAG, "There are not more calendars");
            mFindCalendarUseCase.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            mFindCalendarUseCase.unsubscribe();
            if (e instanceof PermissionRequiredException) {
                String[] requiredPermissions = ((PermissionRequiredException) e).requiredPermissions;
                mPermissionsRequestListener.onPermissionsRequired(requiredPermissions, REQUEST_FIND_CALENDAR);
            }
        }

        @Override
        public void onNext(CalendarEntity calendarEntity) {
            if (calendarEntity == null) {
                mCalendarId = -1;
                return;
            }
            Log.v(TAG, "Found calendar: " + calendarEntity.toString());
            mCalendarId = calendarEntity.id;
        }
    }

    private class FetchEventsSubscriber extends Subscriber<EventEntity> {

        ArrayList<EventEntity> eventEntities;

        FetchEventsSubscriber() {
            eventEntities = new ArrayList<>();
        }

        @Override
        public void onCompleted() {
            Log.v(TAG, "There are not more events");
            mFetchEventsUseCase.unsubscribe();
            if (mView != null) {
                mView.showMonth(mTargetDate.getMonthOfYear(), eventEntities);
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            // TODO: handle permission required
            mFetchEventsUseCase.unsubscribe();
            if (e instanceof PermissionRequiredException) {
                String[] requiredPermissions = ((PermissionRequiredException) e).requiredPermissions;
                mPermissionsRequestListener.onPermissionsRequired(requiredPermissions, REQUEST_FETCH_EVENTS);
            }
        }

        @Override
        public void onNext(EventEntity eventEntity) {
            Log.v(TAG, "Found event: " + eventEntity.toString());
            eventEntities.add(eventEntity);
        }
    }
}
