package io.eodc.planit.listener;

import android.database.Cursor;

/**
 * A callback for when an assignment's information is changed
 *
 * @author 2n
 */
public interface OnAssignmentChangeListener {
    /**
     * Called when the assignment is completed
     *
     * @param cursor A cursor containing the updated assignment
     */
    void onAssignmentComplete(Cursor cursor);

    /**
     * Called when an assignment is created
     */
    void onAssignmentCreation();

    /**
     * Called when an assignment is edited
     */
    void onAssignmentEdit();

}
