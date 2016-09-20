package com.eficksan.customcalendar.presentation.calendar;

import android.os.Bundle;
import android.util.Log;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUserCase;
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
    private CompositeSubscription mViewEventsSubscription;

    private final FindCalendarUserCase mFindCalendarUserCase;
    private String mTargetCalendarName;

    public CalendarPresenter(FindCalendarUserCase findCalendarUserCase) {
        this.mFindCalendarUserCase = findCalendarUserCase;
    }

    public void setTargetCalendarName(String targetCalendarName) {
        this.mTargetCalendarName = targetCalendarName;
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        if (savedInstanceStates == null) {
            DateTime now = DateTime.now();
            mTargetDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0);
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
                        // TODO: show day events
                    }
                }));

        mViewEventsSubscription.add(mView.getSelectedDateTimeChanges()
                .subscribe(new Action1<DateTime>() {
                    @Override
                    public void call(DateTime dateTime) {
                        if (mView != null) {
                            mTargetDate = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0);
                            // TODO: fetch events
                            ArrayList<EventEntity> monthEvents = new ArrayList<>();
                            mView.showMonth(mTargetDate.getMonthOfYear(), monthEvents);
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
        super.onDestroy();
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
            Log.v(TAG, "Found calendar: " + calendarEntity);
        }
    }
}
