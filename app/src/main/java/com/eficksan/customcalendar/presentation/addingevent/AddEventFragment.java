package com.eficksan.customcalendar.presentation.addingevent;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.eficksan.customcalendar.App;
import com.eficksan.customcalendar.R;
import com.eficksan.customcalendar.domain.routing.Router;
import com.eficksan.customcalendar.ioc.addevent.AddEventScreenComponent;
import com.eficksan.customcalendar.ioc.addevent.AddEventScreenModule;
import com.eficksan.customcalendar.ioc.addevent.DaggerAddEventScreenComponent;
import com.eficksan.customcalendar.ioc.common.CalendarModule;
import com.eficksan.customcalendar.presentation.common.PermissionResultListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListener;
import com.eficksan.customcalendar.presentation.common.PermissionsRequestListenerDelegate;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public class AddEventFragment extends DialogFragment implements PermissionsRequestListener, IAddEventView {

    public static final String TAG = AddEventFragment.class.getSimpleName();
    private static final String ARGS_TARGET_DATE = "ARGS_TARGET_DATE";
    private static final String ARGS_CALENDAR_ID = "ARGS_CALENDAR_ID";

    PermissionsRequestListenerDelegate permissionsRequestListenerDelegate;

    AddEventScreenComponent mInjectorComponent;

    @Inject
    AddEventPresenter mPresenter;

    @Bind(R.id.event_title)
    EditText mTitle;
    @Bind(R.id.event_date)
    TextView mEventDate;
    @Bind(R.id.event_start_time)
    TextView mStartTime;
    @Bind(R.id.event_end_time)
    TextView mEndTime;
    @Bind(R.id.event_location)
    EditText mLocaiton;
    @Bind(R.id.event_description)
    EditText mDescription;
    @Bind(R.id.add_event)
    Button mAddEvent;

    private BehaviorSubject<LocalTime> mStartTimeChannel;
    private BehaviorSubject<LocalTime> mEndTimeChannel;
    private TimePickerDialog.OnTimeSetListener mStartTimeChangesCallback = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (mStartTimeChannel!=null) {
                mStartTimeChannel.onNext(new LocalTime(hourOfDay, minute));
            }
        }
    };
    private TimePickerDialog.OnTimeSetListener mEndTimeChangesCallback = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (mEndTimeChannel!=null) {
                mEndTimeChannel.onNext(new LocalTime(hourOfDay, minute));
            }
        }
    };

    public static AddEventFragment newInstance(long calendarId, DateTime targetDate) {
        AddEventFragment fragment = new AddEventFragment();
        Bundle args = new Bundle();
        args.putLong(ARGS_TARGET_DATE, targetDate.getMillis());
        args.putLong(ARGS_CALENDAR_ID, calendarId);
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsRequestListenerDelegate = new PermissionsRequestListenerDelegate();
        setUpInjectionComponent().inject(this);
        mPresenter.takeRouter((Router) getActivity());

        long targetDate = getArguments().getLong(ARGS_TARGET_DATE);
        long calendarId = getArguments().getLong(ARGS_CALENDAR_ID);

        mPresenter.initPresenter(new DateTime(targetDate),calendarId);
        mPresenter.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_event, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mPresenter.onViewCreated(this);
    }

    @Override
    public void onDestroyView() {
        mPresenter.onViewDestroyed();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsRequestListenerDelegate
                .onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public AddEventScreenComponent setUpInjectionComponent() {
        if (mInjectorComponent == null) {
            mInjectorComponent = DaggerAddEventScreenComponent.builder()
                    .appComponent(((App) getActivity().getApplication()).getAppComponent())
                    .calendarModule(new CalendarModule())
                    .addEventScreenModule(new AddEventScreenModule(this))
                    .build();
        }
        return mInjectorComponent;
    }


    public void removeInjectionComponent() {
        mInjectorComponent = null;
    }

    @Override
    public void init(DateTime eventDate, LocalTime startTime, LocalTime endTime) {
        mEventDate.setText(eventDate.toString(getString(R.string.event_date_pattern)));
        mStartTime.setText(startTime.toString(getString(R.string.event_time_pattern)));
        mEndTime.setText(endTime.toString(getString(R.string.event_time_pattern)));
        mStartTimeChannel = BehaviorSubject.create(startTime);
        mEndTimeChannel = BehaviorSubject.create(endTime);


        RxView.clicks(mStartTime).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                LocalTime time = mStartTimeChannel.getValue();
                new TimePickerDialog(getActivity(), mStartTimeChangesCallback, time.getHourOfDay(), time.getMinuteOfHour(), true);
            }
        });

        RxView.clicks(mEndTime).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                LocalTime time = mEndTimeChannel.getValue();
                new TimePickerDialog(getActivity(), mEndTimeChangesCallback, time.getHourOfDay(), time.getMinuteOfHour(), true);
            }
        });
    }

    @Override
    public Observable<String> titleChannel() {
        return RxTextView.textChanges(mTitle)
                .map(new Func1<CharSequence, String>() {
                    @Override
                    public String call(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                });
    }

    @Override
    public Observable<String> descriptionChannel() {
        return RxTextView.textChanges(mDescription)
                .map(new Func1<CharSequence, String>() {
                    @Override
                    public String call(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                });
    }

    @Override
    public Observable<String> locationChannel() {
        return RxTextView.textChanges(mLocaiton)
                .map(new Func1<CharSequence, String>() {
                    @Override
                    public String call(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                });
    }

    @Override
    public Observable<LocalTime> startTimeChannel() {
        return mStartTimeChannel;
    }

    @Override
    public Observable<LocalTime> endTimeChannel() {
        return mEndTimeChannel;
    }

    @Override
    public Observable<Void> addEventChannel() {
        return RxView.clicks(mAddEvent);
    }

    @Override
    public void notifyUser(@StringRes int messageResId) {
        Toast.makeText(getActivity(), messageResId, Toast.LENGTH_LONG).show();
    }
}
