package io.eodc.planit.helper

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.listener.OnAssignmentDismissListener

/**
 * Touch helper that deals with swiping right to "dismiss" assignments
 *
 * @author 2n
 */
class AssignmentTouchHelper(dragDirs: Int, swipeDirs: Int, private val mListener: OnAssignmentDismissListener) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView,
                        viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                        target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.RIGHT) {
            mListener.onDismiss(viewHolder as AssignmentViewHolder)
        }
    }
}
