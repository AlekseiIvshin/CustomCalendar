package com.eficksan.customcalendar.presentation.calendar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eficksan.customcalendar.R;
import com.p_v.flexiblecalendar.FlexibleCalendarView;
import com.p_v.flexiblecalendar.entity.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CalendarFragment extends Fragment {
    public static final String TAG = CalendarFragment.class.getSimpleName();

    @Bind(R.id.calendar)
    FlexibleCalendarView mCalendarView;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
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

        final List<Event> eventColorList = getEventColorList();


        mCalendarView.setEventDataProvider(new FlexibleCalendarView.EventDataProvider() {
            @Override
            public List<? extends Event> getEventsForTheDay(int year, int month, int day) {
                return eventColorList;
            }
        });
    }

    private List<Event> getEventColorList()  {
        Random random = new Random();
        int eventCount = random.nextInt(4) + 1;

        ArrayList<Event> events = new ArrayList<>();
        for (int i = 0; i < eventCount; i++) {
            events.add(new MyEvent());
        }
        return events;
    }

    private class MyEvent implements Event {

        @Override
        public int getColor() {
            return R.color.calendar_event;
        }
    }

}
