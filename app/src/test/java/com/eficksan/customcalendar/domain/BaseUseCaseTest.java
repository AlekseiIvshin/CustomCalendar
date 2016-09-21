package com.eficksan.customcalendar.domain;

import com.eficksan.customcalendar.domain.common.BaseUseCase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */
@RunWith(JUnit4.class)
public class BaseUseCaseTest {

    private BaseUseCase<String, String> useCase;

    @Before
    public void setUp() {
        TestScheduler testScheduler = new TestScheduler();
        useCase = new BaseUseCaseImpl(testScheduler, testScheduler);
    }

    @Test
    public void shouldBuildObservableAndReturnCorrectResult() {
        // Given
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute("Test", testSubscriber);

        // Then
        assertThat(testSubscriber.getOnNextEvents().size(), is(0));
    }

    @Test
    public void shouldUnsubscribeWhenItCalled() {
        // Given
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute("Test", testSubscriber);
        useCase.unsubscribe();

        // Then
        assertThat(testSubscriber.isUnsubscribed(), is(true));

    }

    private static class BaseUseCaseImpl extends BaseUseCase<String, String> {

        BaseUseCaseImpl(Scheduler jobScheduler, Scheduler uiScheduler) {
            super(jobScheduler, uiScheduler);
        }

        @Override
        protected Observable<String> buildObservable(String parameter) {
            return Observable.empty();
        }

        @Override
        public void execute(String parameter, Subscriber<String> subscriber) {
            super.execute(parameter, subscriber);
        }
    }

}