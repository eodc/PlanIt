package io.eodc.planit.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentAdapter
import kotlinx.android.synthetic.main.fragment_planner.*

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
class PlannerFragment : NavigableFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAssignmentListViewModel.allAssignments.observe(this, Observer {
            if (it != null) {
                if (it.isEmpty()) {
                    textPlannerDone.visibility = View.VISIBLE
                } else {
                    textPlannerDone.visibility = View.GONE
                    if (recyclePlanner.adapter == null) {
                        val adapter = AssignmentAdapter(context!!, it, subjects!!)
                        recyclePlanner.adapter = adapter
                    } else {
                        val adapter = recyclePlanner.adapter as AssignmentAdapter?
                        adapter!!.swapAssignmentsList(it)
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_planner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(recyclePlanner)
    }
}
