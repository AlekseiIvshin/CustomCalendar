package com.eficksan.customcalendar.domain.calendar;

import android.util.Log;

import com.eficksan.customcalendar.data.calendar.CalendarRepository;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

public class AddCalendarUseCase extends BaseUseCase<String, Long> {

    private static final String TAG = AddCalendarUseCase.class.getSimpleName();

    private final CalendarRepository calendarRepository;

    public AddCalendarUseCase(
            CalendarRepository calendarRepository,
            Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        this.calendarRepository = calendarRepository;
    }

    @Override
    protected Observable<Long> buildObservable(final String calendarNameParam) {
        Log.v(TAG, "Search calendar by name: " + calendarNameParam);
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(calendarRepository.addCalendar(calendarNameParam));
                    subscriber.onCompleted();
                }
            }
        });
    }
}
