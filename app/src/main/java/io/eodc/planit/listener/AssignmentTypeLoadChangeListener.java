package io.eodc.planit.listener;

/**
 * A callback for when the flag specifying whether to show complete or incomplete assignments
 * is changed
 *
 * @author 2n
 */
public interface AssignmentTypeLoadChangeListener {
    /**
     * Called when the specified flag is changed
     *
     * @param flag The new flag
     */
    void onTypeChanged(int flag);
}
