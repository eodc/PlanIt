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
import com.google.common.collect.Iterables
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.db.Assignment
import io.eodc.planit.helper.AssignmentTouchHelper
import io.eodc.planit.model.AssignmentListViewModel
import org.joda.time.DateTime
import java.util.ArrayList
import java.util.Date

/**
 * Fragment that displays a month's assignments, as well as the information on those assignments.
 *
 * @author 2n
 */
class CalendarFragment : NavigableFragment(), OnDateSelectedListener, OnMonthChangedListener, DayViewDecorator {

    @BindView(R.id.calendar)
    internal var mCalendar: MaterialCalendarView? = null
    @BindView(R.id.rv_day_assignments)
    internal var mRvDaysAssignments: RecyclerView? = null
    @BindView(R.id.text_done)
    internal var mTvAllDone: TextView? = null

    private var mAssignmentListViewModel: AssignmentListViewModel? = null
    private var mDateHasAssignmentList: MutableList<DateTime>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAssignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel::class.java!!)

        mAssignmentListViewModel!!
                .allAssignments
                .observe(this, Observer<List<Assignment>> { this.onDateRangeAssignmentsChange(it) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCalendar!!.setSelectedDate(Date())
        mCalendar!!.setOnMonthChangedListener(this)
        mCalendar!!.setOnDateChangedListener(this)


        mRvDaysAssignments!!.layoutManager = LinearLayoutManager(context)

        mAssignmentListViewModel!!.getAssignmentsDueOnDay(DateTime(mCalendar!!.selectedDate.date))
                .observe(this, Observer<List<Assignment>> { this.onSingleDayAssignmentsChange(it) })

        val touchSimpleCallback = AssignmentTouchHelper(
                0,
                ItemTouchHelper.RIGHT,
                OnAssignmentDismissListener { this.onDismiss(it) })
        val touchHelper = ItemTouchHelper(touchSimpleCallback)
        touchHelper.attachToRecyclerView(mRvDaysAssignments)
    }

    private fun onDismiss(holder: AssignmentViewHolder) {
        val adapter = mRvDaysAssignments!!.adapter
        adapter?.notifyItemRemoved(holder.adapterPosition)
        Thread { mAssignmentListViewModel!!.removeAssignments(holder.assignment) }.start()
    }

    private fun onDateRangeAssignmentsChange(assignments: List<Assignment>?) {
        mDateHasAssignmentList = ArrayList()
        if (assignments != null && assignments.size > 0) {
            mDateHasAssignmentList!!.add(assignments[0].dueDate)
            for (i in assignments.indices) {
                val nextAssign = Iterables.tryFind(assignments
                ) { assignment ->
                    val mostRecent = mDateHasAssignmentList!![mDateHasAssignmentList!!.size - 1]
                    mostRecent.isBefore(assignment.dueDate)
                }
                if (nextAssign.isPresent) {
                    mDateHasAssignmentList!!.add(nextAssign.get().dueDate)
                }
            }
        }
        mCalendar!!.addDecorator(this)
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        mAssignmentListViewModel!!.getAssignmentsDueOnDay(DateTime(date.date))
                .observe(this, Observer<List<Assignment>> { this.onSingleDayAssignmentsChange(it) })
    }

    private fun onSingleDayAssignmentsChange(assignments: List<Assignment>?) {
        if (assignments != null && assignments.size > 0) {
            if (mRvDaysAssignments!!.adapter == null) {
                val adapter = AssignmentAdapter(context, assignments, subjects)
                mRvDaysAssignments!!.swapAdapter(adapter, true)
            } else {
                val adapter = mRvDaysAssignments!!.adapter as AssignmentAdapter?
                adapter!!.swapAssignmentsList(assignments)
            }
            mTvAllDone!!.visibility = View.GONE
            mRvDaysAssignments!!.visibility = View.VISIBLE
        } else {
            mRvDaysAssignments!!.visibility = View.GONE
            mTvAllDone!!.visibility = View.VISIBLE
            mRvDaysAssignments!!.adapter = null
        }
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        for (date in mDateHasAssignmentList!!) {
            if (DateTime(day.date) == date) return true
        }
        return false
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(5f))
    }

    override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) {

    }
}
