package io.eodc.planit.listener;

/**
 * A callback for when the class list is changed
 *
 * @author 2n
 */
public interface OnClassListChangeListener {
    /**
     * Called when the class list changes
     *
     * @param count The new count of classes
     */
    void onClassListChange(int count);
}
