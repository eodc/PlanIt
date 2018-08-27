package io.eodc.planit.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {Assignment.class, Class.class}, version = 2)
@TypeConverters(value = DateConverter.class)
public abstract class PlannerDatabase extends RoomDatabase {
    private static PlannerDatabase instance;

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
                .build();
    }

    public abstract AssignmentDao assignmentDao();
    public abstract ClassDao classDao();
}

