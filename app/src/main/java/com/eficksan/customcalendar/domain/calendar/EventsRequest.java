package com.eficksan.customcalendar.domain.calendar;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public interface EventsRequest {

    long calendarId();
    long fromDate();
    long toDate();
}
