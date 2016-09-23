package com.eficksan.customcalendar.presentation.calendar;

import android.support.annotation.StringRes;

import com.eficksan.customcalendar.data.event.EventEntity;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rx.Observable;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public interface ICalendarView {

    void showMonth(int monthNumber, ArrayList<EventEntity> events);

    Observable<DateTime> getSelectedDateTimeChanges();

    Observable<DateTime> getShownMonthChanges();

    void notifyUser(@StringRes int messageResId);
}
