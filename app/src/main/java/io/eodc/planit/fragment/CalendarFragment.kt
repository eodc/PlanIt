package io.eodc.planit.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.common.collect.Iterables
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import io.eodc.planit.R
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.db.Assignment
import io.eodc.planit.helper.AssignmentTouchHelper
import io.eodc.planit.listener.OnAssignmentDismissListener
import io.eodc.planit.model.AssignmentListViewModel
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.joda.time.DateTime
import java.util.*

/**
 * Fragment that displays a month's assignments, as well as the information on those assignments.
 *
 * @author 2n
 */
class CalendarFragment : NavigableFragment(), OnDateSelectedListener, OnMonthChangedListener, DayViewDecorator {

    private lateinit var mAssignmentListViewModel: AssignmentListViewModel
    private lateinit var mDateHasAssignmentList: ArrayList<DateTime>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAssignmentListViewModel = ViewModelProviders.of(this)
                .get(AssignmentListViewModel::class.java)

        mAssignmentListViewModel
                .allAssignments
                .observe(this, Observer<List<Assignment>> { this.onDateRangeAssignmentsChange(it) })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar.setSelectedDate(Date())
        calendar.setOnMonthChangedListener(this)
        calendar.setOnDateChangedListener(this)


        recycleDaysAssignments.layoutManager = LinearLayoutManager(context)

        mAssignmentListViewModel.getAssignmentsDueOnDay(DateTime(calendar.selectedDate.date))
                .observe(this, Observer<List<Assignment>> { this.onSingleDayAssignmentsChange(it) })

        val touchSimpleCallback = AssignmentTouchHelper(
                0,
                ItemTouchHelper.RIGHT,
                OnAssignmentDismissListener { this.onDismiss(it) })
        val touchHelper = ItemTouchHelper(touchSimpleCallback)
        touchHelper.attachToRecyclerView(recycleDaysAssignments)
    }

    private fun onDismiss(holder: AssignmentViewHolder) {
        val adapter = recycleDaysAssignments.adapter
        adapter?.notifyItemRemoved(holder.adapterPosition)
        Thread { mAssignmentListViewModel.removeAssignments(holder.assignment!!) }.start()
    }

    private fun onDateRangeAssignmentsChange(assignments: List<Assignment>?) {
        mDateHasAssignmentList = ArrayList()
        if (assignments != null && assignments.isNotEmpty()) {
            mDateHasAssignmentList.add(assignments[0].dueDate)
            for (i in assignments.indices) {
                val nextAssign = Iterables.tryFind(assignments
                ) { assignment ->
                    val mostRecent = mDateHasAssignmentList[mDateHasAssignmentList.size - 1]
                    mostRecent.isBefore(assignment?.dueDate)
                }
                if (nextAssign.isPresent) {
                    mDateHasAssignmentList.add(nextAssign.get().dueDate)
                }
            }
        }
        calendar.addDecorator(this)
    }

    override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {
        mAssignmentListViewModel.getAssignmentsDueOnDay(DateTime(date.date))
                .observe(this, Observer<List<Assignment>> { this.onSingleDayAssignmentsChange(it) })
    }

    private fun onSingleDayAssignmentsChange(assignments: List<Assignment>?) {
        if (assignments != null && assignments.isNotEmpty()) {
            if (recycleDaysAssignments.adapter == null) {
                val adapter = AssignmentAdapter(context!!, assignments, subjects!!)
                recycleDaysAssignments.swapAdapter(adapter, true)
            } else {
                val adapter = recycleDaysAssignments.adapter as AssignmentAdapter?
                adapter!!.swapAssignmentsList(assignments)
            }
            textCalendarDone.visibility = View.GONE
            recycleDaysAssignments.visibility = View.VISIBLE
        } else {
            recycleDaysAssignments.visibility = View.GONE
            textCalendarDone.visibility = View.VISIBLE
            recycleDaysAssignments.adapter = null
        }
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        for (date in mDateHasAssignmentList) {
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
