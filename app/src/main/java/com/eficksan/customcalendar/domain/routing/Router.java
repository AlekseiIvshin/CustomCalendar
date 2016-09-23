package com.eficksan.customcalendar.domain.routing;

import org.joda.time.DateTime;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public interface Router {

    void goBack();

    void updateCalendarIdAndShowCalendar(long calendarId);

    void setSelectedDate(DateTime dateTime);
}
