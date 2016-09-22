package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarRepository;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public class FindCalendarUseCaseTest {

    private FindCalendarUseCase useCase;

    private static final String STUB_CALENDAR_NAME = "stub calendar name";
    private static final CalendarEntity CALENDAR_ENTITY = new CalendarEntity(1, STUB_CALENDAR_NAME);

    private CalendarRepository mockRepository;

    @Before
    public void setUp() {
        Scheduler scheduler = Schedulers.immediate();
        mockRepository = mock(CalendarRepository.class);
        useCase = new FindCalendarUseCase(mockRepository, scheduler, scheduler);
    }

    @After
    public void tearDown() {
        useCase.unsubscribe();
    }

    @Test
    public void shouldCallCalendarRepository() {
        // Given
        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        when(mockRepository.findCalendar(anyString()))
                .thenReturn(CALENDAR_ENTITY);

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockRepository, times(1))
                .findCalendar(STUB_CALENDAR_NAME);
        assertThat(testSubscriber.getOnNextEvents().size(), equalTo(1));
        testSubscriber.assertCompleted();
    }

    @Test
    public void shouldProvidePermissionException() {
        // Given
        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        when(mockRepository.findCalendar(anyString()))
                .thenThrow(new PermissionRequiredException(
                        new String[]{
                                Manifest.permission.READ_CALENDAR
                        }));

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockRepository, times(1))
                .findCalendar(STUB_CALENDAR_NAME);
        testSubscriber.assertError(PermissionRequiredException.class);
    }


}
