package io.eodc.planit.fragment

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.adapter.AssignmentViewHolder
import io.eodc.planit.db.Assignment
import io.eodc.planit.helper.AssignmentTouchHelper
import io.eodc.planit.helper.DateValueFormatter
import io.eodc.planit.model.AssignmentListViewModel
import org.joda.time.DateTime
import java.util.ArrayList

/**
 * Fragment that shows a week's overview of assignments, the current day's assignments, and any
 * overdue assignments
 *
 * @author 2n
 */
class HomeFragment : NavigableFragment() {
    @BindView(R.id.text_done)
    internal var mLayoutAllDone: TextView? = null
    @BindView(R.id.card_overdue)
    internal var mCardOverdue: CardView? = null
    @BindView(R.id.graph_week)
    internal var mGraphWeek: LineChart? = null
    @BindView(R.id.recycle_today)
    internal var mRvTodayAssign: RecyclerView? = null
    @BindView(R.id.recycle_overdue)
    internal var mRvOverdueAssign: RecyclerView? = null
    @BindView(R.id.text_event_count)
    internal var mTextEventCount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val assignmentListViewModel = ViewModelProviders.of(this)
                .get<AssignmentListViewModel>(AssignmentListViewModel::class.java!!)

        val today = DateTime().withTimeAtStartOfDay()
        val dateToRetrieve: DateTime

        if (preferences.getString(getString(R.string.pref_what_assign_show_key), "") == getString(R.string.pref_what_assign_show_curr_day_value)) {
            dateToRetrieve = today
        } else {
            dateToRetrieve = today.plusDays(1)
        }

        assignmentListViewModel
                .getAssignmentsBetweenDates(today, today.plusWeeks(1).minusDays(1))
                .observe(this, Observer<List<Assignment>> { this.onWeekAssignmentsGet(it) })
        assignmentListViewModel
                .getAssignmentsDueOnDay(dateToRetrieve)
                .observe(this, Observer<List<Assignment>> { this.onDaysAssignmentsGet(it) })
        assignmentListViewModel
                .getOverdueAssignments(today)
                .observe(this, Observer<List<Assignment>> { this.onOverdueAssignmentsGet(it) })


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRvTodayAssign!!.layoutManager = LinearLayoutManager(context)
        mRvOverdueAssign!!.layoutManager = LinearLayoutManager(context)

        val overdueHelperCallback = AssignmentTouchHelper(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) { holder ->
            val adapter = mRvOverdueAssign!!.adapter
            onDismiss(adapter, holder)
        }
        val todayHelperCallback = AssignmentTouchHelper(ItemTouchHelper.RIGHT, ItemTouchHelper.RIGHT) { holder ->
            val adapter = mRvTodayAssign!!.adapter
            onDismiss(adapter, holder)
        }
        val overdueHelper = ItemTouchHelper(overdueHelperCallback)
        val todayHelper = ItemTouchHelper(todayHelperCallback)

        overdueHelper.attachToRecyclerView(mRvOverdueAssign)
        todayHelper.attachToRecyclerView(mRvTodayAssign)

        setupGraph()
    }

    private fun onDismiss(adapter: RecyclerView.Adapter<*>?, holder: AssignmentViewHolder) {
        adapter?.notifyItemRemoved(holder.adapterPosition)
        Thread {
            ViewModelProviders.of(this)
                    .get<AssignmentListViewModel>(AssignmentListViewModel::class.java!!)
                    .removeAssignments(holder.assignment)
        }.start()
    }

    private fun onOverdueAssignmentsGet(assignments: List<Assignment>) {
        if (activity != null) {
            if (assignments.size > 0) {
                mCardOverdue!!.visibility = View.VISIBLE
                populateRecyclerView(assignments, mRvOverdueAssign)
            } else {
                mCardOverdue!!.visibility = View.GONE
            }
        }
    }

    private fun onWeekAssignmentsGet(assignments: List<Assignment>) {
        val entries = ArrayList<Entry>()
        var checkDate = DateTime()
        var currentDate: DateTime?
        var totalCount = 0
        var iterator = assignments.listIterator()
        for (i in 0..6) {
            var count = 0
            while (iterator.hasNext()) {
                val current = iterator.next()
                currentDate = current.dueDate
                if (currentDate!!.dayOfYear == checkDate.dayOfYear) count++
            }
            iterator = assignments.listIterator(0)
            entries.add(Entry(checkDate.millis.toFloat(), count.toFloat()))
            mGraphWeek!!.axisLeft.axisMaximum = Math.max(mGraphWeek!!.axisLeft.mAxisMaximum, count.toFloat())
            checkDate = checkDate.plusDays(1)
            totalCount += count
        }
        val dataSet = LineDataSet(entries, "")
        setupDataSet(dataSet)

        val lineData = LineData(dataSet)
        val formatter = DateValueFormatter()
        val xAxis = mGraphWeek!!.xAxis
        xAxis.setLabelCount(7, true)
        xAxis.valueFormatter = formatter

        if (totalCount > 1)
            mTextEventCount!!.text = getString(R.string.num_events_label_plural, totalCount)
        else
            mTextEventCount!!.text = getString(R.string.num_events_label, totalCount)
        mGraphWeek!!.data = lineData
        mGraphWeek!!.invalidate()
    }

    private fun onDaysAssignmentsGet(assignments: List<Assignment>) {
        if (assignments.size > 0)
            mLayoutAllDone!!.visibility = View.GONE
        else
            mLayoutAllDone!!.visibility = View.VISIBLE
        populateRecyclerView(assignments, mRvTodayAssign)
    }

    private fun populateRecyclerView(assignments: List<Assignment>, recyclerView: RecyclerView?) {
        if (activity != null) {
            val subjects = (activity as MainActivity).classes
            if (recyclerView!!.adapter == null) {
                val adapter = AssignmentAdapter(context, assignments, subjects, false)
                recyclerView.adapter = adapter
            } else {
                val adapter = recyclerView.adapter as AssignmentAdapter?
                adapter!!.swapAssignmentsList(assignments)
            }
        }
    }

    /**
     * Sets up the week graph
     */
    private fun setupGraph() {
        mGraphWeek!!.disableScroll()
        mGraphWeek!!.isDragEnabled = false
        mGraphWeek!!.setPinchZoom(false)
        mGraphWeek!!.axisLeft.setDrawLabels(false)
        mGraphWeek!!.axisLeft.setDrawGridLines(false)
        mGraphWeek!!.axisRight.setDrawLabels(false)
        mGraphWeek!!.axisRight.setDrawGridLines(false)
        mGraphWeek!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mGraphWeek!!.axisLeft.axisMinimum = 0f
        mGraphWeek!!.axisLeft.axisMaximum = 5f
        mGraphWeek!!.description.text = ""
        mGraphWeek!!.legend.isEnabled = false
        mGraphWeek!!.isDoubleTapToZoomEnabled = false
    }

    /**
     * Sets up the specified data set
     *
     * @param dataSet The data set to setup
     */
    private fun setupDataSet(dataSet: LineDataSet) {
        dataSet.isHighlightEnabled = false
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorAccentDark))
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        dataSet.setDrawFilled(true)
    }
}
