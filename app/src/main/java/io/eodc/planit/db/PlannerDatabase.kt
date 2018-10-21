package io.eodc.planit.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.migration.Migration
import android.content.Context

@Database(entities = [Assignment::class, Subject::class], version = 4)
@TypeConverters(value = [DateConverter::class])
abstract class PlannerDatabase : RoomDatabase() {

    abstract fun assignmentDao(): AssignmentDao

    abstract fun classDao(): SubjectDao

    companion object {
        private var instance: PlannerDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE classes RENAME TO subjects")
            }
        }

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
                    .addMigrations(MIGRATION_3_4)
                    .build()
        }
    }
}

