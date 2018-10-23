package io.eodc.planit.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.helper.AssignmentTouchHelper
import io.eodc.planit.listener.OnAssignmentDismissListener
import io.eodc.planit.model.AssignmentListViewModel
import kotlinx.android.synthetic.main.fragment_planner.*

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
class PlannerFragment : NavigableFragment() {

    private var assignmentListViewModel: AssignmentListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        assignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel::class.java)

        assignmentListViewModel!!.allAssignments.observe(this, Observer {
            if (it != null) {
                if (it.isEmpty()) {
                    textPlannerDone.visibility = View.VISIBLE
                    recyclePlanner.visibility = View.GONE
                } else {
                    textPlannerDone.visibility = View.GONE
                    recyclePlanner.visibility = View.VISIBLE
                    if (recyclePlanner.adapter == null) {
                        val adapter = AssignmentAdapter(context!!, it, subjects!!)
                        recyclePlanner.swapAdapter(adapter, false)
                    } else {
                        val adapter = recyclePlanner.adapter as AssignmentAdapter?
                        adapter!!.swapAssignmentsList(it)
                    }
                }
            }
        })
    }

    private fun onDismiss(holder: AssignmentViewHolder) {
        val adapter = recyclePlanner.adapter
        adapter?.notifyItemRemoved(holder.adapterPosition)
        Thread { assignmentListViewModel!!.removeAssignments(holder.assignment!!) }.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = AssignmentTouchHelper(
                0,
                ItemTouchHelper.RIGHT,
                OnAssignmentDismissListener { this.onDismiss(it) })
        val touchHelper = ItemTouchHelper(callback)

        touchHelper.attachToRecyclerView(recyclePlanner)
        recyclePlanner.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
    }
}
