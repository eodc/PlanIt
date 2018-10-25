package io.eodc.planit.fragment

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.adapter.AssignmentAdapter
import io.eodc.planit.db.Assignment
import io.eodc.planit.helper.DateValueFormatter
import io.eodc.planit.model.AssignmentListViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.joda.time.DateTime
import java.util.*

/**
 * Fragment that shows a week's overview of assignments, the current day's assignments, and any
 * overdue assignments
 *
 * @author 2n
 */
class HomeFragment : NavigableFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val assignmentListViewModel = ViewModelProviders.of(this)
                .get<AssignmentListViewModel>(AssignmentListViewModel::class.java)

        val today = DateTime().withTimeAtStartOfDay()
        val dateToRetrieve: DateTime

        dateToRetrieve = if (preferences.getString(getString(R.string.pref_what_assign_show_key), "") == getString(R.string.pref_what_assign_show_curr_day_value)) {
            today
        } else {
            today.plusDays(1)
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews(recycleOverdueAssignments, recycleTodayAssignments)
        setupGraph()
    }

    private fun onOverdueAssignmentsGet(assignments: List<Assignment>?) {
        if (activity != null) {
            if (assignments != null && assignments.isNotEmpty()) {
                cardHomeOverdue.visibility = View.VISIBLE
                populateRecyclerView(assignments, recycleOverdueAssignments)
            } else {
                cardHomeOverdue.visibility = View.GONE
            }
        }
    }

    private fun onWeekAssignmentsGet(assignments: List<Assignment>?) {
        if (assignments != null) {
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
                    if (currentDate.dayOfYear == checkDate.dayOfYear) count++
                }
                iterator = assignments.listIterator(0)
                entries.add(Entry(checkDate.millis.toFloat(), count.toFloat()))
                graphHomeWeeksAssignments.axisLeft.axisMaximum = Math.max(graphHomeWeeksAssignments.axisLeft.mAxisMaximum, count.toFloat())
                checkDate = checkDate.plusDays(1)
                totalCount += count
            }
            val dataSet = LineDataSet(entries, "")
            setupDataSet(dataSet)

            val lineData = LineData(dataSet)
            val formatter = DateValueFormatter()
            val xAxis = graphHomeWeeksAssignments.xAxis
            xAxis.setLabelCount(7, true)
            xAxis.valueFormatter = formatter

            if (totalCount > 1)
                textGraphAssignmentCount.text = getString(R.string.num_events_label_plural, totalCount)
            else
                textGraphAssignmentCount.text = getString(R.string.num_events_label, totalCount)
            graphHomeWeeksAssignments.data = lineData
            graphHomeWeeksAssignments.invalidate()
        }
    }

    private fun onDaysAssignmentsGet(assignments: List<Assignment>?) {
        if (assignments == null || assignments.isEmpty())
            textHomeDone.visibility = View.VISIBLE
        else {
            textHomeDone.visibility = View.GONE
            populateRecyclerView(assignments, recycleTodayAssignments)
        }
    }

    private fun populateRecyclerView(assignments: List<Assignment>, recyclerView: androidx.recyclerview.widget.RecyclerView?) {
        if (activity != null) {
            val subjects = (activity as MainActivity).classes
            if (recyclerView!!.adapter == null) {
                val adapter = AssignmentAdapter(context!!, assignments, subjects!!, false)
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
        graphHomeWeeksAssignments.disableScroll()
        graphHomeWeeksAssignments.isDragEnabled = false
        graphHomeWeeksAssignments.setPinchZoom(false)
        graphHomeWeeksAssignments.axisLeft.setDrawLabels(false)
        graphHomeWeeksAssignments.axisLeft.setDrawGridLines(false)
        graphHomeWeeksAssignments.axisRight.setDrawLabels(false)
        graphHomeWeeksAssignments.axisRight.setDrawGridLines(false)
        graphHomeWeeksAssignments.xAxis.position = XAxis.XAxisPosition.BOTTOM
        graphHomeWeeksAssignments.axisLeft.axisMinimum = 0f
        graphHomeWeeksAssignments.axisLeft.axisMaximum = 5f
        graphHomeWeeksAssignments.description.text = ""
        graphHomeWeeksAssignments.legend.isEnabled = false
        graphHomeWeeksAssignments.isDoubleTapToZoomEnabled = false
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
