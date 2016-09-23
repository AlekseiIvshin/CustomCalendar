package com.eficksan.customcalendar.presentation.addingevent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.RemoteException;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.event.EventEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.events.AddEventUseCase;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import rx.Subscriber;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.eficksan.customcalendar.presentation.addingevent.AddEventPresenter.REQUEST_ADD_EVENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */
@RunWith(JUnit4.class)
public class AddEventPresenterTest {

    private static final long CALENDAR_ID = 1;
    private static final DateTime TARGET_DATE = new DateTime();

    AddEventPresenter presenter;
    private AddEventUseCase addEventUseCase;
    private PermissionsRequestListener permissionRequestListener;

    IAddEventView mockView;

    @Before
    public void setUp() {
        addEventUseCase = mock(AddEventUseCase.class);
        permissionRequestListener = mock(PermissionsRequestListener.class);

        mockView = mock(IAddEventView.class);
        when(mockView.titleChannel())
                .thenReturn(BehaviorSubject.create("title"));
        when(mockView.descriptionChannel())
                .thenReturn(BehaviorSubject.create("description"));
        when(mockView.locationChannel())
                .thenReturn(BehaviorSubject.create("location"));
        when(mockView.startTimeChannel())
                .thenReturn(BehaviorSubject.create(new LocalTime()));
        when(mockView.endTimeChannel())
                .thenReturn(BehaviorSubject.create(new LocalTime()));
        when(mockView.addEventChannel())
                .thenReturn(PublishSubject.<Void>create());

        presenter = new AddEventPresenter(addEventUseCase, permissionRequestListener);
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

    //===================== on VIEW CREATED =======================//

    @Test
    public void shouldSubscribeOnViewEventsWhenViewCreated() {
        // When
        presenter.onCreate(null);
        presenter.onViewCreated(mockView);

        // Then
        verify(mockView, times(1))
                .titleChannel();
        verify(mockView, times(1))
                .descriptionChannel();
        verify(mockView, times(1))
                .locationChannel();
        verify(mockView, times(1))
                .startTimeChannel();
        verify(mockView, times(1))
                .endTimeChannel();
        verify(mockView, times(1))
                .addEventChannel();
        verify(mockView, times(1))
                .init(any(DateTime.class), any(LocalTime.class), any(LocalTime.class));
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
        verify(addEventUseCase, times(1))
                .unsubscribe();
    }

    //===================== FIND calendar =======================//

    @Test
    public void shouldAddEventWhenViewDispatchActionForAdding() {
        // Given
        doNothing().when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        PublishSubject<Void> addEventClickChannel = PublishSubject.create();
        when(mockView.addEventChannel())
                .thenReturn(addEventClickChannel);

        presenter.onCreate(null);
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.onViewCreated(mockView);

        // When
        addEventClickChannel.onNext(null);


        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

    }

    @Test
    public void shouldUnsubscribeWhenEventAddedSuccessfully() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(1L);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        // When
        presenter.onViewCreated(mockView);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);


        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(1))
                .unsubscribe();
        verify(mockView, times(1))
                .notifyUser(R.string.message_event_adding_success);
    }

    @Test
    public void shouldUnsubscribeWhenEventAddedWithFails() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(-1L);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        // When
        presenter.onViewCreated(mockView);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);


        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(1))
                .unsubscribe();
        verify(mockView, times(1))
                .notifyUser(R.string.message_event_adding_fail);
    }

    @Test
    public void shouldNotFallWhenViewIsNull() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(-1L);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        // When
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);


        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(1))
                .unsubscribe();
    }

    @Test
    public void shouldHandlePermissionError() {
        // Given
        final String[] requiredPermissions = new String[]{Manifest.permission.WRITE_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new PermissionRequiredException(requiredPermissions));
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        // When
        presenter.onCreate(null);
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);

        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(1))
                .unsubscribe();
        verify(permissionRequestListener, times(1))
                .onPermissionsRequired(requiredPermissions, REQUEST_ADD_EVENT);
    }

    @Test
    public void shouldHandleNonPermissionError() {
        // Given
        final String[] requiredPermissions = new String[]{Manifest.permission.WRITE_CALENDAR};
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onError(new RemoteException());
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());

        // When
        presenter.onCreate(null);
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.onViewCreated(mockView);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);

        // Then
        verify(permissionRequestListener, times(0))
                .onPermissionsRequired(requiredPermissions, REQUEST_ADD_EVENT);
    }

    //==================== Permissions ================//

    @Test
    public void shouldAddEventWhenPermissionsGranted() {
        // Given
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                ((Subscriber) arguments[1]).onNext(1L);
                ((Subscriber) arguments[1]).onCompleted();
                return null;
            }
        }).when(addEventUseCase).execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        String[] permissions = new String[]{Manifest.permission.READ_CALENDAR};
        int[] results = new int[]{PackageManager.PERMISSION_GRANTED};

        // When
        presenter.onCreate(null);
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.onViewCreated(mockView);
        presenter.addEvent(CALENDAR_ID, TARGET_DATE);

        // Then
        verify(addEventUseCase, times(1))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(1))
                .unsubscribe();

        // When
        presenter.onRequestPermissionsResult(REQUEST_ADD_EVENT, permissions, results);

        // Then
        verify(addEventUseCase, times(2))
                .execute(any(EventEntity.class), Matchers.<Subscriber<Long>>any());
        verify(addEventUseCase, times(2))
                .unsubscribe();
    }

    @Test
    public void shouldNotifyUserWhenPermissionsDenied() {
        // Given
        String[] permissions = new String[]{Manifest.permission.READ_CALENDAR};
        int[] results = new int[]{PackageManager.PERMISSION_DENIED};

        // When
        presenter.onCreate(null);
        presenter.initPresenter(DateTime.now(), CALENDAR_ID);
        presenter.onViewCreated(mockView);
        presenter.onRequestPermissionsResult(REQUEST_ADD_EVENT, permissions, results);

        // Then
        verify(mockView, times(1))
                .notifyUser(R.string.permission_not_granted);
    }

}
