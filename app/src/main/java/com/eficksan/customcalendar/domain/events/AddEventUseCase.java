package com.eficksan.customcalendar.domain.events;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.common.BaseUseCase;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

public class AddEventUseCase extends BaseUseCase<EventEntity, Long> {

    private final EventsRepository mEventsRepository;

    public AddEventUseCase(EventsRepository eventsRepository, Scheduler jobScheduler, Scheduler uiScheduler) {
        super(jobScheduler, uiScheduler);
        this.mEventsRepository = eventsRepository;
    }

    @Override
    protected Observable<Long> buildObservable(final EventEntity parameterEventEntity) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    long addedEventId = mEventsRepository.addEvent(parameterEventEntity);
                    subscriber.onNext(addedEventId);
                    subscriber.onCompleted();
                }
            }
        });
    }
}
