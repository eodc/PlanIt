package io.eodc.planit.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {Assignment.class, Subject.class}, version = 4)
@TypeConverters(value = DateConverter.class)
public abstract class PlannerDatabase extends RoomDatabase {
    private static PlannerDatabase instance;

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE classes RENAME TO subjects");
        }
    };

    public static PlannerDatabase getInstance(Context context) {
        if (instance == null) {
            createInstance(context.getApplicationContext());
        }
        return instance;
    }

    private static void createInstance(Context context) {
        instance = Room.databaseBuilder(context,
                PlannerDatabase.class,
                "assignments.db")
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    public abstract AssignmentDao assignmentDao();

    public abstract SubjectDao classDao();
}

