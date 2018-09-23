package io.eodc.planit.helper;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import io.eodc.planit.adapter.AssignmentViewHolder;
import io.eodc.planit.listener.OnAssignmentDismissListener;

/**
 * Touch helper that deals with swiping right to "dismiss" assignments
 *
 * @author 2n
 */
public class AssignmentTouchHelper extends ItemTouchHelper.SimpleCallback {
    private OnAssignmentDismissListener mListener;

    public AssignmentTouchHelper(int dragDirs, int swipeDirs, OnAssignmentDismissListener listener) {
        super(dragDirs, swipeDirs);
        this.mListener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            mListener.onDismiss((AssignmentViewHolder) viewHolder);
        }
    }
}
