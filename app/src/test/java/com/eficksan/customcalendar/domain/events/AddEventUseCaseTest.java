package com.eficksan.customcalendar.domain.events;

import android.Manifest;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */
@RunWith(JUnit4.class)
public class AddEventUseCaseTest {
    static final EventEntity EVENT_ENTITY = new EventEntity(1L, 1L, "Title", "Location", "Description", 1L, 2L);
    AddEventUseCase useCase;
    Scheduler scheduler = Schedulers.immediate();


    private EventsRepository mockEventsRepository;

    @Before
    public void setUp() throws Exception {
        mockEventsRepository = mock(EventsRepository.class);
        useCase = new AddEventUseCase(mockEventsRepository, scheduler, scheduler);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldCallEventsRepositoryForAddEvent() {
        // Given
        TestSubscriber<Long> testSubscriber = new TestSubscriber<>();

        when(mockEventsRepository.addEvent(any(EventEntity.class)))
                .thenReturn(-1L);

        // When
        useCase.execute(EVENT_ENTITY, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .addEvent(EVENT_ENTITY);
        assertThat(testSubscriber.getOnNextEvents().size(), equalTo(1));
        assertThat(testSubscriber.getOnNextEvents().get(0), equalTo(-1L));
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldCallEventsRepositoryAndProvideResults() {
        // Given
        TestSubscriber<Long> testSubscriber = new TestSubscriber<>();

        when(mockEventsRepository.addEvent(any(EventEntity.class)))
                .thenReturn(1L);

        // When
        useCase.execute(EVENT_ENTITY, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .addEvent(EVENT_ENTITY);
        assertThat(testSubscriber.getOnNextEvents().size(), equalTo(1));
        assertThat(testSubscriber.getOnNextEvents().get(0), equalTo(1L));
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldProvidePermissionException() {
        // Given
        TestSubscriber<Long> testSubscriber = new TestSubscriber<>();

        when(mockEventsRepository.addEvent(any(EventEntity.class)))
                .thenThrow(new PermissionRequiredException(
                        new String[]{
                                Manifest.permission.WRITE_CALENDAR
                        }));

        // When
        useCase.execute(EVENT_ENTITY, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .addEvent(EVENT_ENTITY);
        testSubscriber.assertError(PermissionRequiredException.class);
    }

}