package io.eodc.planit.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import io.eodc.planit.adapter.AssignmentViewHolder;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.PlannerDatabase;

/**
 * Touch helper that deals with swiping right to "dismiss" assignments
 *
 * @author 2n
 */
public class AssignmentTouchHelper extends ItemTouchHelper.SimpleCallback {
    private Context                     mContext;

    public AssignmentTouchHelper(Context context, int dragDirs, int swipeDirs) {
        super(dragDirs, swipeDirs);
        this.mContext = context;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            AssignmentViewHolder holder = (AssignmentViewHolder) viewHolder;
            Assignment currentAssignment = holder.getAssignment();

            if (currentAssignment.isCompleted()) {
                PlannerDatabase.getInstance(mContext)
                        .assignmentDao().removeAssignment(currentAssignment);
            } else {
                currentAssignment.setCompleted(true);
                PlannerDatabase.getInstance(mContext)
                        .assignmentDao().updateAssignment(currentAssignment);
            }
        }
    }
}
