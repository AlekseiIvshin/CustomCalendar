package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.data.calendar.EventEntityMapper;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import rx.Scheduler;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public class FetchEventsUserCaseTest {

    public static final EventEntity EVENT_ENTITY = new EventEntity(1, 1, "", "", "", 1, 1);
    FetchEventsUseCase useCase;
    private Context mockContext;
    Scheduler scheduler = Schedulers.immediate();
    EventEntityMapper mockMapper;
    public static final int STUB_CALENDAR_ID = 1;
    EventsRequest mockEventRequest;

    @Before
    public void setUp() {
        mockEventRequest = mock(EventsRequest.class);
        mockMapper = mock(EventEntityMapper.class);
        mockContext = mock(Context.class);
        when(mockMapper.mapToObject(any(Cursor.class)))
                .thenReturn(EVENT_ENTITY);
        useCase = new FetchEventsUseCase(mockContext, mockMapper, scheduler, scheduler);
    }

    @After
    public void tearDown() {
        useCase.unsubscribe();
    }

    @Test
    public void shouldRequestPermissionsAndCallContentProvider() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        ContentResolver mockContentResolver = mock(ContentResolver.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToNext())
                .thenReturn(true)
                .thenReturn(false);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(mockCursor);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        Uri expectedUri = CalendarContract.Events.CONTENT_URI;
        String expectedSelection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND);
        String[] expectedProjection = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };

        when(mockEventRequest.calendarId())
                .thenReturn(1L);
        when(mockEventRequest.fromDate())
                .thenReturn(1L);
        when(mockEventRequest.toDate())
                .thenReturn(1L);

        String[] expectedSelectionArgs = new String[]{
                String.valueOf(1L),
                String.valueOf(1L),
                String.valueOf(1L)
        };

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockCursor, times(2)).moveToNext();
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        verify(mockContentResolver, times(1)).query(
                expectedUri,
                expectedProjection,
                expectedSelection,
                expectedSelectionArgs,
                null);
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().get(0), is(EVENT_ENTITY));
    }

    @Test
    public void wshouldHandleMultiplyEvents() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        ContentResolver mockContentResolver = mock(ContentResolver.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToNext())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(mockCursor);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        Uri expectedUri = CalendarContract.Events.CONTENT_URI;
        String expectedSelection = String.format("(%s = ?) AND (%s >= ?) AND (%s < ?)",
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND);
        String[] expectedProjection = new String[]{
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
        };

        when(mockEventRequest.calendarId())
                .thenReturn(1L);
        when(mockEventRequest.fromDate())
                .thenReturn(1L);
        when(mockEventRequest.toDate())
                .thenReturn(1L);

        String[] expectedSelectionArgs = new String[]{
                String.valueOf(1L),
                String.valueOf(1L),
                String.valueOf(1L)
        };

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        verify(mockContentResolver, times(1)).query(
                expectedUri,
                expectedProjection,
                expectedSelection,
                expectedSelectionArgs,
                null);
        verify(mockCursor, times(4)).moveToNext();
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().size(), is(3));
    }

    @Test
    public void shouldHandleNotFoundCalendar() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        ContentResolver mockContentResolver = mock(ContentResolver.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToNext()).thenReturn(false);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(mockCursor);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().size(), is(0));
    }

    @Test
    public void shouldHandleNullableCursor() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        ContentResolver mockContentResolver = mock(ContentResolver.class);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(null);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().size(), is(0));
    }

    @Test
    public void shouldReturnExceptionWhenPermissionWereNotGranted() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockContext, times(1)).checkPermission(anyString(), anyInt(), anyInt());
        testSubscriber.assertError(PermissionRequiredException.class);
        List<Throwable> errorEvents = testSubscriber.getOnErrorEvents();
        assertThat(errorEvents.size(), is(1));
        String[] permissions = ((PermissionRequiredException) errorEvents.get(0)).requiredPermissions;
        assertThat(permissions.length, is(1));
        assertThat((permissions[0]), is(Manifest.permission.READ_CALENDAR));
    }

    @Test
    public void shouldDoNothingWhenObserverWasUnsubscribed() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        TestSubscriber<EventEntity> testSubscriber = new TestSubscriber<>();
        testSubscriber.unsubscribe();

        // When
        useCase.execute(mockEventRequest, testSubscriber);

        // Then
        verify(mockContext, times(0)).checkPermission(anyString(), anyInt(), anyInt());
    }


}
