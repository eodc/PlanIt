package io.eodc.planit.helper

import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener.SwipeDirection.LEFT_TO_RIGHT
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener.SwipeDirection.RIGHT_TO_LEFT
import com.google.android.material.snackbar.Snackbar
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.db.Assignment
import io.eodc.planit.model.AssignmentListViewModel
import kotlinx.android.synthetic.main.activity_main.view.*

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
                val type: String = when (item.type) {
                    Assignment.TYPE_TEST -> "Quiz/Test"
                    Assignment.TYPE_PROJECT -> "Project"
                    Assignment.TYPE_HOMEWORK -> "Homework"
                    else -> ""
                }

                val snackbar = Snackbar.make(mRecyclerView.rootView.root,
                        mActivity.getString(R.string.snckbr_complete_label, type),
                        Snackbar.LENGTH_SHORT)
                        .setAction(mActivity.getString(R.string.snckbr_complete_action_label)) {
                            Thread { mAssignmentListViewModel.insertAssignments(item) }.start()
                        }
                val snackParams = snackbar.view.layoutParams as CoordinatorLayout.LayoutParams
                snackParams.anchorId = R.id.bottomNavigation
                snackParams.anchorGravity = Gravity.TOP
                snackParams.gravity = Gravity.TOP
                snackbar.view.layoutParams = snackParams

                snackbar.show()

                Thread { mAssignmentListViewModel.removeAssignments(item) }.start()
            }
            else -> {
                return
            }
        }
    }
}