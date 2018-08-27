package io.eodc.planit.fragment;

import android.support.v4.app.Fragment;

/**
 * An abstract fragment containing method implementation all freely navigable (that is, all fragments
 * that can be accessed from the bottom navigation) fragments use.
 *
 * @author 2n
 */
public abstract class BaseFragment extends Fragment {

    public static final int FLAG_SHOW_COMPLETE = 1;
    public static final int FLAG_SHOW_INCOMPLETE = 0;

    /* @Override
    public void onAssignmentComplete(Cursor cursor) {
        cursor.moveToFirst();
        final int id = cursor.getInt(cursor.getColumnIndex(PlannerContract.AssignmentColumns._ID));
        int typeIndex = cursor.getColumnIndex(PlannerContract.AssignmentColumns.TYPE);
        String snackbarLabel = "";

        switch (cursor.getString(typeIndex)) {
            case PlannerContract.TYPE_HOMEWORK:
                snackbarLabel = getString(R.string.snckbr_complete_label, "Homework");
                break;
            case PlannerContract.TYPE_PROJECT:
                snackbarLabel = getString(R.string.snckbr_complete_label, "Project");
                break;
            case PlannerContract.TYPE_TEST:
                snackbarLabel = getString(R.string.snckbr_complete_label, "Project/Test");
                break;
        }
        if (getActivity() != null) {
            Snackbar.make(((MainActivity) getActivity()).getBottomNav(),
                    snackbarLabel,
                    Snackbar.LENGTH_SHORT)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ContentValues values = new ContentValues();
                            values.put(PlannerContract.AssignmentColumns.COMPLETED, false);
                            getActivity().getContentResolver().update(PlannerContract.AssignmentColumns.CONTENT_URI,
                                    values, PlannerContract.AssignmentColumns._ID + "=?",
                                    new String[]{String.valueOf(id)});
                            onAssignmentEdit();
                        }
                    }).show();
        }
        cursor.close();
    } */
}
