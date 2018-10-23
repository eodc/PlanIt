package io.eodc.planit.db

import android.arch.persistence.room.TypeConverter

import org.joda.time.DateTime

class DateConverter {
    @TypeConverter
    fun fromTimestamp(time: Long?): DateTime? {
        return if (time == null) null else DateTime(time)
    }

    @TypeConverter
    fun toTimestamp(time: DateTime?): Long? {
        return time?.millis
    }
}
