package com.eficksan.customcalendar.presentation.calendar;

import android.Manifest;
import android.content.pm.PackageManager;

import com.eficksan.customcalendar.data.calendar.CalendarEntity;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.calendar.EventsRequest;
import com.eficksan.customcalendar.domain.calendar.FetchEventsUseCase;
import com.eficksan.customcalendar.domain.calendar.FindCalendarUseCase;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import rx.Subscriber;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */
@RunWith(JUnit4.class)
public class CalendarPresenterTest {

    private static final String STUB_CALENDAR_NAME = "stub calendar name";
    public static final CalendarEntity CALENDAR_ENTITY = new CalendarEntity(1, STUB_CALENDAR_NAME);
    public static final EventEntity EVENT_ENTITY = new EventEntity(1, 1, "", "", "", 1, 1);
    private static final int REQUEST_FETCH_EVENTS = 1;
    private static final int REQUEST_FIND_CALENDAR = 2;

    CalendarPresenter presenter;
    private FindCalendarUseCase findCalendarUseCase;
    private FetchEventsUseCase fetchEventsUseCase;
    private PermissionsRequestListener permissionRequestListener;

    @Before
    public void setUp() {
        findCalendarUseCase = mock(FindCalendarUseCase.class);
        fetchEventsUseCase = mock(FetchEventsUseCase.class);
        permissionRequestListener = mock(PermissionsRequestListener.class);

        presenter = new CalendarPresenter(
                findCalendarUseCase,
                fetchEventsUseCase,
                permissionRequestListener,
                STUB_CALENDAR_NAME);
    }

    //===================== on CREATE presenter =======================//

    @Test
    public void shouldSubscribeOnPermissionsGranterWhenPresenterCreated() {
        // When
        presenter.onCreate(null);

        // Then
        verify(permissionRequestListener, times(1))
                .addListener(presenter);
    }

    @Test
    public void shouldFindCalendarWhenPresenterCreated() {
        // Given
        doNothing()
                .when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
    }

    //===================== on VIEW CREATED =======================//

    @Test
    public void shouldSubscribeOnViewEventsWhenViewCreated() {
        // Given
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);

        // Then
        verify(mockView, times(1))
                .getSelectedDateTimeChanges();
        verify(mockView, times(1))
                .getShownMonthChanges();
    }

    //===================== on DESTROY presenter =======================//

    @Test
    public void shouldUnsubscribeFromPermissionsGranterWhenPresenterDestroyed() {
        // When
        presenter.onDestroy();

        // Then
        verify(permissionRequestListener, times(1))
                .removeListener(presenter);
    }

    @Test
    public void shouldUnsubscribeUseCasesWhenPresenterDestroyed() {
        // When
        presenter.onDestroy();

        // Then
        verify(findCalendarUseCase, times(1))
                .unsubscribe();
        verify(fetchEventsUseCase, times(1))
                .unsubscribe();
    }

    //===================== FIND calendar =======================//

    @Test
    public void shouldUnsubscribeWhenCalendarFound() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        verify(findCalendarUseCase, times(1))
                .unsubscribe();
    }

    @Test
    public void shouldHandlePermissionErrorWhenSearchCalendar() {
        // Given
        final String[] requiredPermissions = new String[]{Manifest.permission.READ_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new PermissionRequiredException(requiredPermissions));
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        verify(findCalendarUseCase, times(1))
                .unsubscribe();
        verify(permissionRequestListener, times(1))
                .onPermissionsRequired(requiredPermissions, REQUEST_FIND_CALENDAR);
    }

    @Test
    public void shouldHandleNonPermissionErrorWhenSearchCalendar() {
        // Given
        final String[] requiredPermissions = new String[]{Manifest.permission.READ_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new RuntimeException());
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        verify(findCalendarUseCase, times(1))
                .unsubscribe();
        verify(permissionRequestListener, times(0))
                .onPermissionsRequired(requiredPermissions, REQUEST_FIND_CALENDAR);
        verify(fetchEventsUseCase, times(0))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());
    }

    @Test
    public void shouldFetchEventsWhenCalendarFound() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        verify(findCalendarUseCase, times(1))
                .unsubscribe();
    }

    //===================== Fetch events =======================//

    @Test
    public void shouldShowEventsWhenEventsFetched() {
        // Given
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(EVENT_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(fetchEventsUseCase).execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);

        // Then
        verify(fetchEventsUseCase, times(1))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());
        verify(fetchEventsUseCase, times(1))
                .unsubscribe();
    }

    @Test
    public void shouldHandlePermissionError() {
        // Given
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        final String[] requiredPermissions = new String[]{Manifest.permission.READ_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new PermissionRequiredException(requiredPermissions));
                return null;
            }
        }).when(fetchEventsUseCase).execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);

        // Then
        verify(fetchEventsUseCase, times(1))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());
        verify(fetchEventsUseCase, times(1))
                .unsubscribe();
        verify(permissionRequestListener, times(1))
                .onPermissionsRequired(requiredPermissions, REQUEST_FETCH_EVENTS);
    }

    @Test
    public void shouldHandleNonPermissionError() {
        // Given
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        final String[] requiredPermissions = new String[]{Manifest.permission.READ_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new RuntimeException());
                return null;
            }
        }).when(fetchEventsUseCase).execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);

        // Then
        verify(fetchEventsUseCase, times(1))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());
        verify(fetchEventsUseCase, times(1))
                .unsubscribe();
        verify(permissionRequestListener, times(0))
                .onPermissionsRequired(requiredPermissions, REQUEST_FETCH_EVENTS);
    }

    //==================== Permissions ================//

    @Test
    public void shouldSearchCalendarWhenPermissionsGranted() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        String[] permissions = new String[] {Manifest.permission.READ_CALENDAR};
        int[] results = new int[] {PackageManager.PERMISSION_GRANTED};

        // When
        presenter.onCreate(null);

        // Then
        verify(findCalendarUseCase, times(1))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());

        // When
        presenter.onRequestPermissionsResult(REQUEST_FIND_CALENDAR, permissions, results);

        // Then
        verify(findCalendarUseCase, times(2))
                .execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
    }

    @Test
    public void shouldSearchEventsWhenPermissionsGranted() {
        // Given
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(CALENDAR_ENTITY);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(findCalendarUseCase).execute(anyString(), Matchers.<Subscriber<CalendarEntity>>any());
        String[] permissions = new String[] {Manifest.permission.READ_CALENDAR};
        int[] results = new int[] {PackageManager.PERMISSION_GRANTED};

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);
        // Then
        verify(fetchEventsUseCase, times(1))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());

        // When
        presenter.onRequestPermissionsResult(REQUEST_FETCH_EVENTS, permissions, results);

        // Then
        verify(fetchEventsUseCase, times(2))
                .execute(any(EventsRequest.class), Matchers.<Subscriber<EventEntity>>any());
    }

    @Test
    public void shouldNotifyUserWhenPermissionsDeniedForCalendarRequest() {
        // Given
        String[] permissions = new String[] {Manifest.permission.READ_CALENDAR};
        int[] results = new int[] {PackageManager.PERMISSION_DENIED};
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);
        presenter.onRequestPermissionsResult(REQUEST_FIND_CALENDAR, permissions, results);

        // Then
        verify(mockView, times(1))
                .notifyUser(anyInt());
    }

    @Test
    public void shouldNotifyUserWhenPermissionsDeniedForEventsRequest() {
        // Given
        String[] permissions = new String[] {Manifest.permission.READ_CALENDAR};
        int[] results = new int[] {PackageManager.PERMISSION_DENIED};
        ICalendarView mockView = mock(ICalendarView.class);
        BehaviorSubject<DateTime> dayChangesChannel = BehaviorSubject.create(new DateTime());
        BehaviorSubject<DateTime> monthChangesChannel = BehaviorSubject.create(new DateTime());
        when(mockView.getSelectedDateTimeChanges())
                .thenReturn(dayChangesChannel);
        when(mockView.getShownMonthChanges())
                .thenReturn(monthChangesChannel);

        // When
        presenter.onViewCreated(mockView);
        presenter.onRequestPermissionsResult(REQUEST_FETCH_EVENTS, permissions, results);

        // Then
        verify(mockView, times(1))
                .notifyUser(anyInt());
    }
}
