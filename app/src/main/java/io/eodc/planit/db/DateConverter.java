package io.eodc.planit.db;

import android.arch.persistence.room.TypeConverter;

import org.joda.time.DateTime;

class DateConverter {
    @TypeConverter
    public static DateTime fromTimestamp(Long time) {
        return time == null ? null : new DateTime(time);
    }

    @TypeConverter
    public static Long toTimestamp(DateTime time) {
        return time == null ? null : time.getMillis();
    }
}
