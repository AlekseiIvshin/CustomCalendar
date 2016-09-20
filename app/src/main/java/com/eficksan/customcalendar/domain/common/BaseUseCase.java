package com.eficksan.customcalendar.domain.common;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */
public abstract class BaseUseCase<ParameterType, ResultType> {

    private Subscription subscription;
    protected final Scheduler jobScheduler;
    private final Scheduler uiScheduler;

    protected BaseUseCase(Scheduler jobScheduler, Scheduler uiScheduler) {
        this.jobScheduler = jobScheduler;
        this.uiScheduler = uiScheduler;
    }

    protected abstract Observable<ResultType> buildObservable(ParameterType parameter);

    public void execute(ParameterType parameter, Subscriber<ResultType> subscriber) {
        subscription = buildObservable(parameter)
                .subscribeOn(jobScheduler)
                .observeOn(uiScheduler)
                .subscribe(subscriber);
    }

    public void unsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
