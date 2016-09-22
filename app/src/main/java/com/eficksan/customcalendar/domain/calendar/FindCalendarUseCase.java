package com.eficksan.customcalendar.domain.calendar;

import android.util.Log;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarRepository;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

public class FindCalendarUseCase extends BaseUseCase<String, CalendarEntity> {

    private final CalendarRepository calendarRepository;

    public FindCalendarUseCase(
            CalendarRepository calendarRepository,
            Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        this.calendarRepository = calendarRepository;
    }

    @Override
    protected Observable<CalendarEntity> buildObservable(final String calendarNameParam) {
        return Observable.create(new Observable.OnSubscribe<CalendarEntity>() {
            @Override
            public void call(Subscriber<? super CalendarEntity> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(calendarRepository.findCalendar(calendarNameParam));
                    subscriber.onCompleted();
                }
            }
        });
    }
}
