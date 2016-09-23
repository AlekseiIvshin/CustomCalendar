package com.eficksan.customcalendar.presentation.addingevent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.event.EventEntity;
import com.eficksan.customcalendar.domain.PermissionRequiredException;
import com.eficksan.customcalendar.domain.events.AddEventUseCase;
import com.eficksan.customcalendar.presentation.common.BasePresenter;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

public class AddEventPresenter extends BasePresenter<IAddEventView> implements PermissionResultListener {

    private static final String TAG = AddEventPresenter.class.getSimpleName();
    public static final int REQUEST_ADD_EVENT = 1;
    private final AddEventUseCase mAddEventUseCase;
    private DateTime mTargetDate;
    private long mCalendarId;
    private String mTitle;
    private String mDescription;
    private String mLocation;
    private LocalTime mEndTime;
    private LocalTime mStartTime;

    private final PermissionsRequestListener mPermissionsRequestListener;

    public AddEventPresenter(AddEventUseCase addEventUseCase, PermissionsRequestListener permissionsRequestListener) {
        this.mAddEventUseCase = addEventUseCase;
        this.mPermissionsRequestListener = permissionsRequestListener;
    }

    private CompositeSubscription mViewEventSubscription;

    public void initPresenter(DateTime targetDate, long calendarId) {
        mTargetDate = targetDate.withTime(0, 0, 0, 0);
        mCalendarId = calendarId;
        if (mTargetDate.isEqual(DateTime.now().withTime(0, 0, 0, 0))) {
            mStartTime = LocalTime.now();
        } else {
            mStartTime = new LocalTime(9, 0);
        }
        mEndTime = mStartTime.plusMinutes(30);
    }

    @Override
    public void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        mPermissionsRequestListener.addListener(this);
    }

    @Override
    public void onViewCreated(IAddEventView view) {
        super.onViewCreated(view);
        mView.init(mTargetDate, mStartTime, mEndTime);
        subscribeOnViewEvents();
    }

    @Override
    public void onViewDestroyed() {
        unsubscribeFromViewEvents();
        super.onViewDestroyed();
    }

    @Override
    public void onDestroy() {
        mAddEventUseCase.unsubscribe();
        mPermissionsRequestListener.removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_ADD_EVENT == requestCode) {
            if (grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == grantResults[0]) {
                addEvent(mCalendarId, mTargetDate);
            } else {
                mView.notifyUser(R.string.permission_not_granted);
            }
        }
    }

    private void subscribeOnViewEvents() {
        mViewEventSubscription = new CompositeSubscription();

        mViewEventSubscription.add(mView.titleChannel().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mTitle = s;
            }
        }));
        mViewEventSubscription.add(mView.descriptionChannel().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mDescription = s;
            }
        }));
        mViewEventSubscription.add(mView.locationChannel().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                mLocation = s;
            }
        }));
        mViewEventSubscription.add(mView.startTimeChannel().subscribe(new Action1<LocalTime>() {
            @Override
            public void call(LocalTime time) {
                mStartTime = time;
            }
        }));
        mViewEventSubscription.add(mView.endTimeChannel().subscribe(new Action1<LocalTime>() {
            @Override
            public void call(LocalTime time) {
                mEndTime = time;
            }
        }));
        mViewEventSubscription.add(mView.addEventChannel().subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                addEvent(mCalendarId, mTargetDate);
            }
        }));
    }

    public void addEvent(long calendarId, DateTime targetDate) {
        DateTime startTime = targetDate.withTime(mStartTime.getHourOfDay(), mStartTime.getMinuteOfHour(), 0, 0);
        DateTime endTime = targetDate.withTime(mEndTime.getHourOfDay(), mEndTime.getMinuteOfHour(), 0, 0);
        mAddEventUseCase.execute(
                new EventEntity(0L, calendarId, mTitle, mLocation, mDescription, startTime.getMillis(), endTime.getMillis()), new AddEventSubscriber());
    }

    private void unsubscribeFromViewEvents() {
        mViewEventSubscription.unsubscribe();
        mViewEventSubscription.clear();
        mViewEventSubscription = null;
    }

    private class AddEventSubscriber extends Subscriber<Long> {

        @Override
        public void onCompleted() {
            mAddEventUseCase.unsubscribe();
        }

        @Override
        public void onError(Throwable e) {
            Log.v(TAG, e.getMessage(), e);
            mAddEventUseCase.unsubscribe();
            if (e instanceof PermissionRequiredException) {
                String[] requiredPermissions = ((PermissionRequiredException) e).requiredPermissions;
                mPermissionsRequestListener.onPermissionsRequired(requiredPermissions, REQUEST_ADD_EVENT);
            }
        }

        @Override
        public void onNext(Long eventId) {
            if (mView != null) {
                if (eventId > 0) {
                    mView.notifyUser(R.string.message_event_adding_success);
                    if (mRouter != null) {
                        mRouter.goBack();
                    }
                } else {
                    mView.notifyUser(R.string.message_event_adding_fail);
                }
            }
        }
    }
}
