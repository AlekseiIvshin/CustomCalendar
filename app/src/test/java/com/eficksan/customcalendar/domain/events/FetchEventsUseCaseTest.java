package com.eficksan.customcalendar.domain.events;

import android.Manifest;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public class FetchEventsUseCaseTest {

    private static final EventEntity EVENT_ENTITY = new EventEntity(1, 1, "", "", "", 2, 3);
    private FetchEventsUseCase useCase;
    private EventsRequest stubEventRequest = new EventsRequest() {
        @Override
        public long calendarId() {
            return 1L;
        }

        @Override
        public long fromDate() {
            return 2L;
        }

        @Override
        public long toDate() {
            return 3L;
        }
    };

    private EventsRepository mockEventsRepository;

    @Before
    public void setUp() {
        Scheduler scheduler = Schedulers.immediate();
        mockEventsRepository = mock(EventsRepository.class);
        useCase = new FetchEventsUseCase(mockEventsRepository, scheduler, scheduler);
    }

    @After
    public void tearDown() {
        useCase.unsubscribe();
    }

    @Test
    public void shouldCallEventsRepository() {
        // Given
        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        when(mockEventsRepository.fetchEvents(anyLong(), anyLong(), anyLong()))
                .thenReturn(Collections.EMPTY_LIST);

        // When
        useCase.execute(stubEventRequest, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .fetchEvents(1L, 2L, 3L);
        assertThat(testSubscriber.getOnNextEvents().size(), equalTo(0));
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldCallEventsRepositoryAndProvideResults() {
        // Given
        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();
        LinkedList<EventEntity> stubResults = new LinkedList<>();
        stubResults.add(EVENT_ENTITY);
        stubResults.add(EVENT_ENTITY);
        stubResults.add(EVENT_ENTITY);

        when(mockEventsRepository.fetchEvents(anyLong(), anyLong(), anyLong()))
                .thenReturn(stubResults);

        // When
        useCase.execute(stubEventRequest, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .fetchEvents(1L, 2L, 3L);
        assertThat(testSubscriber.getOnNextEvents().size(), equalTo(3));
        for (int i = 0; i < 3; i++) {
            assertThat(testSubscriber.getOnNextEvents().get(i), equalTo(EVENT_ENTITY));
        }
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldProvidePermissionException() {
        // Given
        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        when(mockEventsRepository.fetchEvents(anyLong(), anyLong(), anyLong()))
                .thenThrow(new PermissionRequiredException(
                        new String[]{
                                Manifest.permission.READ_CALENDAR
                        }));

        // When
        useCase.execute(stubEventRequest, testSubscriber);

        // Then
        verify(mockEventsRepository, times(1))
                .fetchEvents(1L, 2L, 3L);
        testSubscriber.assertError(PermissionRequiredException.class);
    }

}
