package io.eodc.planit.helper

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import io.eodc.planit.adapter.AssignmentViewHolder

/**
 * Touch helper that deals with swiping right to "dismiss" assignments
 *
 * @author 2n
 */
class AssignmentTouchHelper(dragDirs: Int, swipeDirs: Int, private val mListener: OnAssignmentDismissListener) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.RIGHT) {
            mListener.onDismiss(viewHolder as AssignmentViewHolder)
        }
    }
}
