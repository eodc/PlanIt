package io.eodc.planit.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "subjects")
class Subject(@field:ColumnInfo
              var name: String,
              @field:ColumnInfo
              var teacher: String,
              @field:ColumnInfo
              var color: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    override fun toString(): String {
        return name
    }
}
