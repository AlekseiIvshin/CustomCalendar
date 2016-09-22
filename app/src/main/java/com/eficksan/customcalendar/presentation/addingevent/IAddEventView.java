package com.eficksan.customcalendar.presentation.addingevent;

import android.support.annotation.StringRes;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import rx.Observable;

/**
 * Created by Aleksei_Ivshin on 9/22/16.
 */

public interface IAddEventView {

    void init(DateTime eventDate, LocalTime startTime, LocalTime endTime);

    Observable<String> titleChannel();

    Observable<String> descriptionChannel();

    Observable<String> locationChannel();

    Observable<LocalTime> startTimeChannel();

    Observable<LocalTime> endTimeChannel();

    Observable<Void> addEventChannel();

    void notifyUser(@StringRes int messageResId);
}
