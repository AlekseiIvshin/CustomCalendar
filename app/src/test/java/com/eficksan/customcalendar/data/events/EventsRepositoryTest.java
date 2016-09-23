package com.eficksan.customcalendar.data.events;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.eficksan.customcalendar.BuildConfig;
import com.eficksan.customcalendar.data.event.EventEntity;
import com.eficksan.customcalendar.data.event.EventsRepository;
import com.eficksan.customcalendar.domain.PermissionRequiredException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import rx.observers.TestSubscriber;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
@Ignore
public class EventsRepositoryTest {


    private EventsRepository eventsRepository;
    public static final EventEntity EVENT_ENTITY = new EventEntity(1, 1, "", "", "", 2, 3);
    private Context mockContext;

    @Before
    public void setUp() {
        mockContext = mock(Context.class);
    }

    @After
    public void tearDown() {
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

        String[] expectedSelectionArgs = new String[]{
                String.valueOf(1L),
                String.valueOf(1L),
                String.valueOf(1L)
        };

        // When
        eventsRepository.fetchEvents(1L, 2L, 3L);

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
    public void shouldHandleMultiplyEvents() {
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

        String[] expectedSelectionArgs = new String[]{
                String.valueOf(1L),
                String.valueOf(1L),
                String.valueOf(1L)
        };

        // When
        eventsRepository.fetchEvents(1L, 2L, 3L);

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
    public void shouldHandleNotFoundEvents() {
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
        eventsRepository.fetchEvents(1L, 2L, 3L);

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
        eventsRepository.fetchEvents(1L, 2L, 3L);

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
        eventsRepository.fetchEvents(1L, 2L, 3L);

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
        eventsRepository.fetchEvents(1L, 2L, 3L);

        // Then
        verify(mockContext, times(0)).checkPermission(anyString(), anyInt(), anyInt());
    }
}
