package io.eodc.planit.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * An extension of {@link ContentProvider} that interfaces with the tables defined in
 * {@link PlannerContract}. It is fully CRUD (Create, Read, Update, Delete) capable
 *
 * @author 2n
 */
public class PlannerProvider extends ContentProvider {
    private PlannerDbHelper dbHelper;

    public static final int ASSIGNMENT = 100;
    public static final int ASSIGNMENT_ID = 110;
    public static final int CLASS = 200;
    public static final int CLASS_ID = 210;

    public static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PlannerContract.AUTHORITY, PlannerContract.PATH_ASSIGNMENT, ASSIGNMENT);
        uriMatcher.addURI(PlannerContract.AUTHORITY, PlannerContract.PATH_ASSIGNMENT + "/#", ASSIGNMENT_ID);
        uriMatcher.addURI(PlannerContract.AUTHORITY, PlannerContract.PATH_CLASS, CLASS);
        uriMatcher.addURI(PlannerContract.AUTHORITY, PlannerContract.PATH_CLASS + "/#", CLASS_ID);
    }

    @Override
    public boolean onCreate() {
        Context mContext = getContext();
        if (mContext != null) {
            dbHelper = new PlannerDbHelper(mContext);
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        String id;
        String mSelection;
        String[] mSelectionArgs;

        switch (uriMatcher.match(uri)) {
            case ASSIGNMENT:
                return db.query(PlannerContract.AssignmentColumns.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case ASSIGNMENT_ID:
                mSelection = PlannerContract.AssignmentColumns._ID + "=?";
                id = uri.getPathSegments().get(1);
                mSelectionArgs = new String[] {id};
                return db.query(PlannerContract.AssignmentColumns.TABLE_NAME, projection,
                        mSelection, mSelectionArgs, null, null, sortOrder);
            case CLASS:
                return db.query(PlannerContract.ClassColumns.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case CLASS_ID:
                mSelection = PlannerContract.ClassColumns._ID + "=?";
                id = uri.getPathSegments().get(1);
                mSelectionArgs = new String[] {id};
                return db.query(PlannerContract.ClassColumns.TABLE_NAME, projection,
                        mSelection, mSelectionArgs, null, null, sortOrder);
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id;

        switch (uriMatcher.match(uri)) {
            case ASSIGNMENT:
                id = db.insert(PlannerContract.AssignmentColumns.TABLE_NAME, null, values);
                if (id > 0) return ContentUris.withAppendedId(PlannerContract.AssignmentColumns.CONTENT_URI, id);
                else throw new SQLException("Something went wrong when inserting into the Assignments table.");
            case CLASS:
                id = db.insert(PlannerContract.ClassColumns.TABLE_NAME, null, values);
                if (id > 0) return ContentUris.withAppendedId(PlannerContract.ClassColumns.CONTENT_URI, id);
                else throw new SQLException("Something went wrong when inserting into the Classes table.");
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case ASSIGNMENT:
                return db.delete(PlannerContract.AssignmentColumns.TABLE_NAME, selection, selectionArgs);
            case CLASS:
                return db.delete(PlannerContract.ClassColumns.TABLE_NAME, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case ASSIGNMENT:
                return db.update(PlannerContract.AssignmentColumns.TABLE_NAME, values, selection, selectionArgs);
            case CLASS:
                return db.update(PlannerContract.ClassColumns.TABLE_NAME, values, selection, selectionArgs);
        }

        return 0;
    }
}
