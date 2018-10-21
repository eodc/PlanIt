package io.eodc.planit.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import org.joda.time.DateTime

@Entity(tableName = "assignments")
class Assignment(@field:ColumnInfo
                 var title: String?, @field:ColumnInfo
                 var classId: Int, @field:ColumnInfo
                 var dueDate: DateTime?, @field:ColumnInfo
                 var notes: String?) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @ColumnInfo
    var type: Int = 0

    override fun toString(): String? {
        return title
    }

    companion object {
        val TYPE_TEST = 1 shl 1
        val TYPE_PROJECT = 1 shl 2
        val TYPE_HOMEWORK = 1 shl 3
    }
}
