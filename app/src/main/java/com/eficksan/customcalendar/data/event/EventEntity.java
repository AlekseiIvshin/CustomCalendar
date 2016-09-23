package com.eficksan.customcalendar.data.event;

/**
 * Created by Aleksei_Ivshin on 9/20/16.
 */

public class EventEntity {
    public final long id;
    public final long calendarId;
    public final String title;
    public final String location;
    public final String description;
    public final long startAt;
    public final long endAt;

    public EventEntity(long id, long calendarId, String title, String location, String description, long startAt, long endAt) {
        this.id = id;
        this.calendarId = calendarId;
        this.title = title;
        this.location = location;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @Override
    public String toString() {
        return "EventEntity{" +
                "id=" + id +
                ", calendarId=" + calendarId +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }
}
