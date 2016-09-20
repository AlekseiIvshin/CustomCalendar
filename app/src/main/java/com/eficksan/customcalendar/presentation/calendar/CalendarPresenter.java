package com.eficksan.customcalendar.presentation.calendar;

import android.os.Bundle;
import android.util.Log;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.calendar.FetchEventsUserCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUserCase;
import com.eficksan.customcalendar.domain.calendar.MonthEventsRequest;
import com.eficksan.customcalendar.presentation.common.BasePresenter;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
public class CalendarPresenter extends BasePresenter<ICalendarView> {
    private static final String TAG = CalendarPresenter.class.getSimpleName();

    private static final String EXTRA_LAST_SHOW_DATE = "EXTRA_LAST_SHOW_DATE";
    private DateTime mTargetDate;
    private long mCalendarId = -1;
    private CompositeSubscription mViewEventsSubscription;

    private final FindCalendarUserCase mFindCalendarUserCase;
    private final FetchEventsUserCase mFetchEventsUserCase;

    private String mTargetCalendarName;

    public CalendarPresenter(FindCalendarUserCase findCalendarUserCase, FetchEventsUserCase mFetchEventsUserCase) {
        this.mFindCalendarUserCase = findCalendarUserCase;
        this.mFetchEventsUserCase = mFetchEventsUserCase;
    }

    public void setTargetCalendarName(String targetCalendarName) {
        this.mTargetCalendarName = targetCalendarName;
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        if (savedInstanceStates == null) {
            mTargetDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0);
        } else {
            long savedLastTime = savedInstanceStates.getLong(EXTRA_LAST_SHOW_DATE);
            mTargetDate = new DateTime(savedLastTime);
        }
    }

    @Override
    public void onViewCreated(ICalendarView view) {
        super.onViewCreated(view);

        mFindCalendarUserCase.execute(mTargetCalendarName, new FoundCalendarSubscriber());

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
        mFindCalendarUserCase.unsubscribe();
        mFetchEventsUserCase.unsubscribe();
        super.onDestroy();
    }

    public void fetchEventsForMonth() {
        MonthEventsRequest eventsRequest = MonthEventsRequest
                .createNew(mCalendarId, mTargetDate);

        mFetchEventsUserCase.execute(eventsRequest, new FetchEventsSubscriber());
    }

    private class FoundCalendarSubscriber extends Subscriber<CalendarEntity> {

        @Override
        public void onCompleted() {
            Log.v(TAG, "There are not any calendars");
            mFindCalendarUserCase.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            // TODO: handle permission required
            mFindCalendarUserCase.unsubscribe();
        }

        @Override
        public void onNext(CalendarEntity calendarEntity) {
            if (calendarEntity == null) {
                mCalendarId = -1;
                return;
            }
            Log.v(TAG, "Found calendar: " + calendarEntity.toString());
            mCalendarId = calendarEntity.id;
            fetchEventsForMonth();
        }
    }

    private class FetchEventsSubscriber extends Subscriber<EventEntity> {

        ArrayList<EventEntity> eventEntities;

        public FetchEventsSubscriber() {
            eventEntities = new ArrayList<>();
        }

        @Override
        public void onCompleted() {
            Log.v(TAG, "There are not any calendars");
            mFetchEventsUserCase.unsubscribe();
            if (mView != null) {
                mView.showMonth(mTargetDate.getMonthOfYear(), eventEntities);
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage(), e);
            // TODO: handle permission required
            mFetchEventsUserCase.unsubscribe();
        }

        @Override
        public void onNext(EventEntity eventEntity) {
            Log.v(TAG, "Found event: " + eventEntity.toString());
            eventEntities.add(eventEntity);
        }
    }
}
