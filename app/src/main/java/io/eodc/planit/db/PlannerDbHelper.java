package io.eodc.planit.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helps link the DB to the Content Provider
 *
 * @author 2n
 * @see PlannerProvider
 */
public class PlannerDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "assignments.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of this DB Helper
     *
     * @param context The context to use
     */
    PlannerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String ASSIGNMENTS_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + PlannerContract.AssignmentColumns.TABLE_NAME + "(" +
                        PlannerContract.AssignmentColumns._ID + " INTEGER PRIMARY KEY, " +
                        PlannerContract.AssignmentColumns.TITLE + " TEXT NOT NULL, " +
                        PlannerContract.AssignmentColumns.CLASS_ID + " INTEGER NOT NULL, " +
                        PlannerContract.AssignmentColumns.TYPE + " TEXT NOT NULL, " +
                        PlannerContract.AssignmentColumns.COMPLETED + " BOOLEAN NOT NULL, " +
                        PlannerContract.AssignmentColumns.DUE_DATE + " DATE NOT NULL, " +
                        PlannerContract.AssignmentColumns.NOTES + " TEXT)";
        final String CLASSES_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + PlannerContract.ClassColumns.TABLE_NAME + "(" +
                        PlannerContract.ClassColumns._ID + " INTEGER PRIMARY KEY, " +
                        PlannerContract.ClassColumns.NAME + " TEXT NOT NULL, " +
                        PlannerContract.ClassColumns.TEACHER + " TEXT NOT NULL, " +
                        PlannerContract.ClassColumns.COLOR + " TEXT NOT NULL)";

        db.execSQL(CLASSES_CREATE_ENTRIES);
        db.execSQL(ASSIGNMENTS_CREATE_ENTRIES);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
