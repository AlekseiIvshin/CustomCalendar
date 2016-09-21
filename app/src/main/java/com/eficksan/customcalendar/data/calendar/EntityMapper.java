package com.eficksan.customcalendar.data.calendar;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Aleksei Ivshin
 * on 21.09.2016.
 */

public interface EntityMapper<ENTITY_CLASS> {

    ENTITY_CLASS mapToObject(Cursor cursor);
    ContentValues mapToContentValues(ENTITY_CLASS entity);

}
