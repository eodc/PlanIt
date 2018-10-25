package io.eodc.planit.helper

import androidx.appcompat.app.AppCompatActivity
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.db.Assignment
import io.eodc.planit.model.AssignmentListViewModel

class RecyclerViewSwipeHelper(private val mAssignmentListViewModel: AssignmentListViewModel,
                              private val mRecyclerView: DragDropSwipeRecyclerView,
                              private val mActivity: AppCompatActivity) : OnItemSwipeListener<Assignment> {
    override fun onItemSwiped(position: Int, direction: OnItemSwipeListener.SwipeDirection, item: Assignment) {
        when (direction) {
            RIGHT_TO_LEFT -> {
                val holder = mRecyclerView.findViewHolderForLayoutPosition(position)
                if (holder != null) {
                    (holder as AssignmentViewHolder).editAssignment(mActivity)
                }
            }
            LEFT_TO_RIGHT -> {
                Thread { mAssignmentListViewModel.removeAssignments(item) }.start()
            }
            else -> {
                return
            }
        }
    }
}