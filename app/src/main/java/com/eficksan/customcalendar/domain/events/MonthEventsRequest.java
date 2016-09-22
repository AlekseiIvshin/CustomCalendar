package com.eficksan.customcalendar.domain.events;

import org.joda.time.DateTime;

/**
 * Created by Aleksei Ivshin
 * on 20.09.2016.
 */
public class MonthEventsRequest implements EventsRequest {

    private final long calendarId;
    private final long fromDate;
    private final long toDate;

    private MonthEventsRequest(long calendarId, long fromDate, long toDate) {
        this.calendarId = calendarId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static MonthEventsRequest createNew(long calendarId, DateTime date) {
        DateTime startDateTime = date.withDayOfMonth(1).withTime(0, 0, 0, 0);
        DateTime endDateTime = date.dayOfMonth().withMaximumValue().withTime(23, 59, 0, 0);
        long fromDate = startDateTime.getMillis();
        long toDate = endDateTime.getMillis();
        return new MonthEventsRequest(calendarId, fromDate, toDate);
    }

    @Override
    public long calendarId() {
        return calendarId;
    }

    @Override
    public long fromDate() {
        return fromDate;
    }

    @Override
    public long toDate() {
        return toDate;
    }

    @Override
    public String toString() {
        return "MonthEventsRequest{" +
                "calendarId=" + calendarId +
                ", fromDate=" + new DateTime(fromDate) +
                ", toDate=" + new DateTime(toDate) +
                '}';
    }
}
