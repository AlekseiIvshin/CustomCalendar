package com.eficksan.customcalendar.domain.events;

import android.util.Log;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class FetchEventsUseCase extends BaseUseCase<EventsRequest, EventEntity> {

    private static final String TAG = FetchEventsUseCase.class.getSimpleName();
    private final EventsRepository mEventsRepository;

    public FetchEventsUseCase(EventsRepository eventsRepository, Scheduler uiScheduler, Scheduler jobScheduler) {
        super(jobScheduler, uiScheduler);
        mEventsRepository = eventsRepository;
    }

    @Override
    protected Observable<EventEntity> buildObservable(EventsRequest paramEventsRequest) {
        Log.v(TAG, "Request events: " + paramEventsRequest);
        return Observable.just(paramEventsRequest)
                .flatMap(new Func1<EventsRequest, Observable<EventEntity>>() {
                    @Override
                    public Observable<EventEntity> call(EventsRequest eventsRequest) {
                        return Observable.from(mEventsRepository.fetchEvents(
                                eventsRequest.calendarId(),
                                eventsRequest.fromDate(),
                                eventsRequest.toDate()));
                    }
                });
    }
}
