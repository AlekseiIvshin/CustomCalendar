package com.eficksan.customcalendar.data.calendar;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class CalendarEntity {

    public final long id;
    public final String displayName;

    public CalendarEntity(long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "CalendarEntity{" +
                "id=" + id +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
