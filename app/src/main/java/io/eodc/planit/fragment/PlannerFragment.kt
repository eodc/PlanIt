package io.eodc.planit.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.helper.AssignmentTouchHelper
import io.eodc.planit.model.AssignmentListViewModel

/**
 * Fragment showing all of the user's inputted assignments
 *
 * @author 2n
 */
class PlannerFragment : NavigableFragment() {

    @BindView(R.id.recycle_assignment)
    internal var mRvContent: RecyclerView? = null
    @BindView(R.id.text_done)
    internal var mTvAllDone: TextView? = null

    private var assignmentListViewModel: AssignmentListViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        assignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel::class.java!!)

        assignmentListViewModel!!.allAssignments.observe(this, { assignments ->
            if (assignments != null) {
                if (assignments!!.size == 0) {
                    mTvAllDone!!.visibility = View.VISIBLE
                    mRvContent!!.visibility = View.GONE
                } else {
                    mTvAllDone!!.visibility = View.GONE
                    mRvContent!!.visibility = View.VISIBLE
                    if (mRvContent!!.adapter == null) {
                        val adapter = AssignmentAdapter(context, assignments, subjects)
                        mRvContent!!.swapAdapter(adapter, false)
                    } else {
                        val adapter = mRvContent!!.adapter as AssignmentAdapter?
                        adapter!!.swapAssignmentsList(assignments)
                    }
                }
            }
        })
    }

    private fun onDismiss(holder: AssignmentViewHolder) {
        val adapter = mRvContent!!.adapter
        adapter?.notifyItemRemoved(holder.adapterPosition)
        Thread { assignmentListViewModel!!.removeAssignments(holder.assignment) }.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_planner, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback = AssignmentTouchHelper(
                0,
                ItemTouchHelper.RIGHT,
                OnAssignmentDismissListener { this.onDismiss(it) })
        val touchHelper = ItemTouchHelper(callback)

        touchHelper.attachToRecyclerView(mRvContent)
        mRvContent!!.layoutManager = LinearLayoutManager(context)
    }
}
