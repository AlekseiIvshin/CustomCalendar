package com.eficksan.customcalendar.presentation.calendar;

import com.eficksan.customcalendar.data.calendar.EventEntity;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rx.Observable;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public interface ICalendarView extends IView {

    void showMonth(int monthNumber, ArrayList<EventEntity> events);

    Observable<DateTime> getSelectedDateTimeChanges();

    Observable<DateTime> getShownMonthChanges();
}
