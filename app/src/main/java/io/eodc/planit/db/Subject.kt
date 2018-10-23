package io.eodc.planit.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
