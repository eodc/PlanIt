package io.eodc.planit.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Assignment::class, Subject::class], version = 1)
@TypeConverters(value = [DateConverter::class])
abstract class PlannerDatabase : RoomDatabase() {

    abstract fun assignmentDao(): AssignmentDao

    abstract fun classDao(): SubjectDao

    companion object {
        private var instance: PlannerDatabase? = null

        fun getInstance(context: Context): PlannerDatabase? {
            if (instance == null) {
                createInstance(context.applicationContext)
            }
            return instance
        }

        private fun createInstance(context: Context) {
            instance = Room.databaseBuilder<PlannerDatabase>(context,
                    PlannerDatabase::class.java,
                    "assignments.db")
                    .build()
        }
    }
}

