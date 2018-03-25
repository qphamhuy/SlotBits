package com.quang.slotbits.db;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.DateTime;

public class Converters {
    @TypeConverter
    public static DateTime fromTimestamp(Long value) {
        return value == null ? null : new DateTime(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(DateTime date) {
        return date == null ? null : date.getMillis();
    }
}
