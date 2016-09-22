package com.eficksan.customcalendar.presentation.calendar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eficksan.customcalendar.App;
import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.data.calendar.EventEntity;
import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenComponent;
import com.eficksan.customcalendar.ioc.calendar.CalendarScreenModule;
import com.eficksan.customcalendar.ioc.calendar.DaggerCalendarScreenComponent;
import com.eficksan.customcalendar.ioc.common.CalendarModule;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListenerDelegate;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.entity.Event;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class CalendarFragment extends Fragment implements ICalendarView, PermissionsRequestListener {
    public static final String TAG = CalendarFragment.class.getSimpleName();

    PermissionsRequestListenerDelegate permissionsRequestListenerDelegate;

    @Bind(R.id.calendar)
    FlexibleCalendarView mCalendarView;

    @Inject
    CalendarPresenter mPresenter;

    private CalendarScreenComponent calendarScreenComponent;
    private BehaviorSubject<DateTime> mMonthChannel;
    private BehaviorSubject<DateTime> mDaysChannel;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsRequestListenerDelegate = new PermissionsRequestListenerDelegate();
        setUpInjectionComponent().inject(this);
        mPresenter.takeRouter((Router) getActivity());
        mPresenter.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        DateTime currentDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0);

        mMonthChannel = BehaviorSubject.create(currentDate);
        mDaysChannel = BehaviorSubject.create(currentDate);

        mCalendarView.setOnMonthChangeListener(new FlexibleCalendarView.OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month, int direction) {
                mMonthChannel.onNext(new DateTime(year, month + 1, 1, 0, 0));
            }
        });

        mCalendarView.setOnDateClickListener(new FlexibleCalendarView.OnDateClickListener() {
            @Override
            public void onDateClick(int year, int month, int day) {
                mDaysChannel.onNext(new DateTime(year, month + 1, day + 1, 0, 0));
            }
        });

        mPresenter.onViewCreated(this);
    }

    @Override
    public void onDestroyView() {
        mCalendarView.setOnMonthChangeListener(null);
        mCalendarView.setOnDateClickListener(null);
        mMonthChannel.onCompleted();
        mDaysChannel.onCompleted();
        mPresenter.onViewDestroyed();
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.releaseRouter();
        mPresenter.onDestroy();
        removeInjectionComponent();
        super.onDestroy();
    }

    @Override
    public void showMonth(int monthNumber, final ArrayList<EventEntity> events) {
        int calendarMonth = mCalendarView.getCurrentMonth() + 1;
        Log.v(TAG, String.format("Show month: calendar equals to %d, events received for %d, found events %d",
                calendarMonth,
                monthNumber,
                events.size()));
        if (calendarMonth == monthNumber) {
            mCalendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
                @Override
                public List<? extends Event> getEventsForTheDay(final int year, final int month, final int day) {
                    final DateTime start = new DateTime(year, month + 1, day, 0, 0);
                    final DateTime end = start.withHourOfDay(23).withMinuteOfHour(59);
                    return Observable.from(events)
                            .filter(new Func1<EventEntity, Boolean>() {
                                @Override
                                public Boolean call(EventEntity eventEntity) {
                                    return start.isBefore(eventEntity.startAt) && end.isAfter(eventEntity.endAt);
                                }
                            }).map(new Func1<EventEntity, MyEvent>() {
                                @Override
                                public MyEvent call(EventEntity eventEntity) {
                                    return new MyEvent();
                                }
                            })
                            .toList().toBlocking().first();
                }
            });
            mCalendarView.refresh();
        }
    }

    @Override
    public Observable<DateTime> getSelectedDateTimeChanges() {
        return mMonthChannel;
    }

    @Override
    public Observable<DateTime> getShownMonthChanges() {
        return mDaysChannel;
    }

    @Override
    public void notifyUser(@StringRes int messageResId) {
        Toast.makeText(getActivity(), messageResId, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsRequired(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void addListener(PermissionResultListener resultListener) {
        permissionsRequestListenerDelegate.addListener(resultListener);
    }

    @Override
    public void removeListener(PermissionResultListener resultListener) {
        permissionsRequestListenerDelegate.removeListener(resultListener);
    }

    private class MyEvent implements Event {

        @Override
        public int getColor() {
            return R.color.calendar_color;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestListenerDelegate
                .onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public CalendarScreenComponent setUpInjectionComponent() {
        if (calendarScreenComponent == null) {
            calendarScreenComponent = DaggerCalendarScreenComponent.builder()
                    .appComponent(((App) getActivity().getApplication()).getAppComponent())
                    .calendarModule(new CalendarModule())
                    .calendarScreenModule(new CalendarScreenModule(this))
                    .build();
        }
        return calendarScreenComponent;
    }


    public void removeInjectionComponent() {
        calendarScreenComponent = null;
    }
}
