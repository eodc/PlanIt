package io.eodc.planit.fragment

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView
import io.eodc.planit.R

import io.eodc.planit.activity.MainActivity
import io.eodc.planit.db.Subject
import io.eodc.planit.helper.RecyclerViewSwipeHelper
import io.eodc.planit.model.AssignmentListViewModel

open class NavigableFragment : Fragment() {
    lateinit var mAssignmentListViewModel: AssignmentListViewModel

    internal val subjects: List<Subject>?
        get() {
            if (activity != null && activity is MainActivity) {
                val subjects = (activity as MainActivity).classes
                return subjects ?: subjects
            }
            return null
        }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mAssignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel::class.java)
    }

    fun setupRecyclerViews(vararg recyclerViews: DragDropSwipeRecyclerView) {
        for (recyclerView in recyclerViews) {
            recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            recyclerView.orientation = DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING
            (recyclerView.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            val swipeHelper = RecyclerViewSwipeHelper(mAssignmentListViewModel, recyclerView, activity as AppCompatActivity)
            recyclerView.swipeListener = swipeHelper

            recyclerView.behindSwipedItemIconMargin = 16f

            recyclerView.behindSwipedItemBackgroundSecondaryColor = ContextCompat.getColor(context!!, R.color.bg_swipe_complete)
            recyclerView.behindSwipedItemIconSecondaryDrawableId = R.drawable.ic_check_white_24dp

            recyclerView.behindSwipedItemBackgroundColor = ContextCompat.getColor(context!!, R.color.bg_swipe_edit)
            recyclerView.behindSwipedItemIconDrawableId = R.drawable.ic_edit_white_24dp
        }
    }
}
