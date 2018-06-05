package io.eodc.planit.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract defining all the columns, URIs, flags, types, etc used in data storage within the app.
 *
 * @author 2n
 */
public class PlannerContract {
    static final String AUTHORITY           = "io.eodc.planit.planner";
    static final Uri    BASE_CONTENT_URI    = Uri.parse("content://" + AUTHORITY);
    static final String PATH_ASSIGNMENT     = "assignment";
    static final String PATH_CLASS          = "class";


    public static final int FLAG_SHOW_INCOMPLETE    = 0;
    public static final int FLAG_SHOW_COMPLETE      = 1;

    public static final String TYPE_HOMEWORK    = "homework";
    public static final String TYPE_TEST        = "test";
    public static final String TYPE_PROJECT     = "project";

    private PlannerContract() {
    }

    public static class ClassColumns implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_CLASS)
                .build();
        public static final String TABLE_NAME = "classes";
        public static final String NAME = "name";
        public static final String TEACHER = "teacher";
        public static final String COLOR = "color";
    }

    public static class AssignmentColumns implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ASSIGNMENT)
                .build();
        public static final String TABLE_NAME = "assignments";
        public static final String TITLE = "title";
        public static final String CLASS_ID = "class";
        public static final String TYPE = "type";
        public static final String COMPLETED = "completed";
        public static final String DUE_DATE = "dueDate";
        public static final String NOTES = "notes";
    }
}
