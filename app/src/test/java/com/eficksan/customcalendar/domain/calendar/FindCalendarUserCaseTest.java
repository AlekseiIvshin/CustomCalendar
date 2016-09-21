package com.eficksan.customcalendar.domain.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.CalendarEntityMapper;
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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public class FindCalendarUserCaseTest {

    FindCalendarUserCase useCase;
    private Context mockContext;
    Scheduler scheduler = Schedulers.immediate();
    CalendarEntityMapper mockMapper;
    public static final String STUB_CALENDAR_NAME = "stub calendar name";
    public static final CalendarEntity CALENDAR_ENTITY = new CalendarEntity(1, STUB_CALENDAR_NAME);

    @Before
    public void setUp() {
        mockMapper = mock(CalendarEntityMapper.class);
        when(mockMapper.mapToObject(any(Cursor.class)))
                .thenReturn(CALENDAR_ENTITY);
        mockContext = mock(Context.class);
        useCase = new FindCalendarUserCase(mockContext, mockMapper, scheduler, scheduler);
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
        when(mockCursor.moveToFirst()).thenReturn(true);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(mockCursor);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        final Uri expectedUri = CalendarContract.Calendars.CONTENT_URI;
        final String expectedSelection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
        final String[] expectedProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME};
        final String[] expectedSelectionArgs = new String[]{STUB_CALENDAR_NAME};

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        verify(mockContentResolver, times(1)).query(
                expectedUri,
                expectedProjection,
                expectedSelection,
                expectedSelectionArgs,
                null);
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().get(0), is(CALENDAR_ENTITY));
    }

    @Test
    public void shouldHandleNotFoundCalendar() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_GRANTED);

        ContentResolver mockContentResolver = mock(ContentResolver.class);
        Cursor mockCursor = mock(Cursor.class);
        when(mockCursor.moveToFirst()).thenReturn(false);
        when(mockContentResolver.query(
                any(Uri.class),
                any(String[].class),
                anyString(),
                any(String[].class),
                anyString()))
                .thenReturn(mockCursor);
        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);

        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        final Uri expectedUri = CalendarContract.Calendars.CONTENT_URI;
        final String expectedSelection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
        final String[] expectedProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME};
        final String[] expectedSelectionArgs = new String[]{STUB_CALENDAR_NAME};

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        verify(mockContentResolver, times(1)).query(
                expectedUri,
                expectedProjection,
                expectedSelection,
                expectedSelectionArgs,
                null);
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

        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        final Uri expectedUri = CalendarContract.Calendars.CONTENT_URI;
        final String expectedSelection = String.format("(%s = ?)", CalendarContract.Calendars.NAME);
        final String[] expectedProjection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME};
        final String[] expectedSelectionArgs = new String[]{STUB_CALENDAR_NAME};

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockContext, times(1))
                .checkPermission(anyString(), anyInt(), anyInt());
        verify(mockContentResolver, times(1)).query(
                expectedUri,
                expectedProjection,
                expectedSelection,
                expectedSelectionArgs,
                null);
        testSubscriber.assertCompleted();
        assertThat(testSubscriber.getOnNextEvents().size(), is(0));
    }

    @Test
    public void shouldReturnExceptionWhenPermissionWereNotGranted() {
        // Given
        when(mockContext.checkPermission(anyString(), anyInt(), anyInt()))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

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

        TestSubscriber<CalendarEntity> testSubscriber = new TestSubscriber<>();
        testSubscriber.unsubscribe();

        // When
        useCase.execute(STUB_CALENDAR_NAME, testSubscriber);

        // Then
        verify(mockContext, times(0)).checkPermission(anyString(), anyInt(), anyInt());
    }


}
