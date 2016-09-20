package com.eficksan.customcalendar.domain.calendar;

import org.joda.time.DateTime;

/**
 * Created by Aleksei Ivshin
 * on 20.09.2016.
 */
public class MonthEventsRequest {

    public final long calendarId;
    public final long fromDate;
    public final long toDate;

    private MonthEventsRequest(long calendarId, long fromDate, long toDate) {
        this.calendarId = calendarId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static MonthEventsRequest createNew(long calendarId, DateTime date) {
        DateTime startDateTime = date.withDayOfMonth(1);
        DateTime endDateTime = date.dayOfMonth().withMaximumValue();
        long fromDate = startDateTime.getMillis();
        long toDate = endDateTime.getMillis();
        return new MonthEventsRequest(calendarId, fromDate, toDate);
    }
}
