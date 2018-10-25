package io.eodc.planit.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter
import com.google.common.collect.Iterables
import io.eodc.planit.R
import io.eodc.planit.db.Assignment
import io.eodc.planit.db.Subject
import org.joda.time.DateTime

/**
 * Adapter for interfacing mAssignments to [RecyclerView]
 *
 * @author 2n
 */
class AssignmentAdapter(dataSet: List<Assignment> = emptyList()) : DragDropSwipeAdapter<Assignment, AssignmentViewHolder>(dataSet) {
    private lateinit var mContext: Context

    private var mShowDividerFlag: Int = 0

    private lateinit var mSubjects: List<Subject>

    /**
     * Constructs a new instance of AssignmentAdapter. Dynamically displays relevant information
     * about the assignment in its view depending on its position in the list and it due date.
     *
     * @param context     The context to use for grabbing strings, colors, etc.
     * @param assignments The list of assignments to show
     * @param subjects     The LiveData containing all the user's subjects.
     * @see AssignmentViewHolder
     */
    constructor(context: Context, assignments: List<Assignment>, subjects: List<Subject>) : this(assignments) {
        this.mContext = context
        this.mSubjects = subjects
        this.mShowDividerFlag = -1
        setHasStableIds(true)
    }

    /**
     * Constructs a new instance of AssignmentAdapter while specifying if dividers should be shown.
     * This constructor should be used in instances where the full list of mAssignments is not being
     * displayed, or the context of when the mAssignments are due have been established.
     * @param context      The context to use for grabbing strings, colors, etc.
     * @param assignments The list of assignments to show
     * @param subjects     The LiveData containing all the user's subjects.
     * @param showDividers Whether or not dividers should be shown.
     */
    constructor(context: Context, assignments: List<Assignment>, subjects: List<Subject>, showDividers: Boolean) : this(assignments) {
        this.mContext = context
        this.mSubjects = subjects
        this.mShowDividerFlag = if (showDividers) -1 else NEVER_SHOW_DIVIDER
        setHasStableIds(true)
    }

    fun swapAssignmentsList(newList: List<Assignment>) {
        dataSet = newList
        notifyDataSetChanged()
    }

    /**
     * Differentiates one assignment from another, and decides whether to show that it has textNotes,
     * is on a different day, etc.
     *
     * @param position Position in the cursor
     * @return Type of item view
     */
    override fun getItemViewType(position: Int): Int {
        val currentAssignment = dataSet[position]
        val hasNotes = currentAssignment.notes
                .trim { it <= ' ' } != ""

        if (mShowDividerFlag == NEVER_SHOW_DIVIDER) {
            return getNormalViewType(hasNotes)
        }
        if (position == 0) {
            return getDividerViewType(hasNotes)
        } else {
            val previousDue = dataSet[position - 1]
                    .dueDate
            val currentDue = currentAssignment.dueDate
            val now = DateTime()

            if (currentDue.isBeforeNow && now.dayOfYear - currentDue.dayOfYear >= 1) { // Overdue [Don't divide]
                return getNormalViewType(hasNotes)
            }

            return if (currentDue.isBefore(now.plusWeeks(1))) { // Within same week (Now + 7 days) [Divide for every day]
                if (currentDue.dayOfWeek() != previousDue.dayOfWeek())
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else if (currentDue.monthOfYear() == now.monthOfYear()) { // Within same month [Divide for every week]
                if (previousDue.isBefore(now.plusWeeks(1)) || currentDue.weekOfWeekyear() != previousDue.weekOfWeekyear())
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else if (currentDue.year() == now.year()) { // Within same year [Divide for every month]
                if (currentDue.monthOfYear() != previousDue.monthOfYear() || previousDue.isBefore(now.plusWeeks(1)))
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else {
                if (currentDue.year() != previousDue.year())
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            }
        }
    }

    private fun getDividerViewType(hasNotes: Boolean): Int {
        return if (hasNotes)
            VIEW_TYPE_DIVIDER or VIEW_TYPE_NOTES
        else
            VIEW_TYPE_DIVIDER
    }

    private fun getNormalViewType(hasNotes: Boolean): Int {
        return if (hasNotes)
            VIEW_TYPE_NORMAL or VIEW_TYPE_NOTES
        else
            VIEW_TYPE_NORMAL
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id.toLong()
    }

    override fun getViewHolder(itemView: View): AssignmentViewHolder {
        return AssignmentViewHolder(itemView)
    }

    override fun getViewToTouchToStartDraggingItem(item: Assignment, viewHolder: AssignmentViewHolder, position: Int): View? {
        return viewHolder.itemView
    }

    override fun onBindViewHolder(item: Assignment, viewHolder: AssignmentViewHolder, position: Int) {
        var prevItem: Assignment? = null
        if (position > 0) {
            prevItem = dataSet[position - 1]
        }

        val assignmentSubject = Iterables.find(mSubjects) { value -> value?.id == item.classId }

        val dtCurrent = item.dueDate
        val assignmentTypeFlag = item.type

        var assignmentType = ""

        when (assignmentTypeFlag) {
            Assignment.TYPE_TEST -> assignmentType = "Test/Quiz"
            Assignment.TYPE_PROJECT -> assignmentType = "Project"
            Assignment.TYPE_HOMEWORK -> assignmentType = "Homework"
        }


        val classAndTypeText = mContext.getString(R.string.class_name_and_type_text,
                assignmentSubject.name,
                assignmentType)

        viewHolder.textClassType.text = classAndTypeText
        viewHolder.textDueDate.text = dtCurrent.toString(mContext.getString(R.string.due_date_pattern))
        viewHolder.assignment = item

        if (viewHolder.itemViewType and VIEW_TYPE_DIVIDER == VIEW_TYPE_DIVIDER) {
            val headerText = getHeaderText(dtCurrent)
            viewHolder.textHeader.text = headerText
            viewHolder.layoutHeader.visibility = View.VISIBLE
            viewHolder.showDueDate()
        } else if (prevItem != null && item.dueDate.dayOfYear() != prevItem.dueDate.dayOfYear()) {
            viewHolder.showDueDate()
        } else {
            viewHolder.hideDueDate()
        }

        if (viewHolder.itemViewType and VIEW_TYPE_NOTES == VIEW_TYPE_NOTES) {
            viewHolder.iconExpand.visibility = View.VISIBLE
            viewHolder.textNotes.text = item.notes
            viewHolder.itemView.setOnClickListener { viewHolder.handleNoteClick() }
        }

        viewHolder.itemView.setOnLongClickListener {
            viewHolder.editAssignment(mContext as AppCompatActivity)
            true
        }

        viewHolder.imageClassColor.setBackgroundColor(Color.parseColor(assignmentSubject.color))
        viewHolder.textAssignmentName.text = item.title
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun canBeDragged(item: Assignment, viewHolder: AssignmentViewHolder, position: Int) = false

    override fun canBeDroppedOver(item: Assignment, viewHolder: AssignmentViewHolder, position: Int) = false

    /**
     * Gets the text shown at the divider (layoutHeader) based off of the assignment's due date and the
     * current day.
     *
     * @param currentDue The [DateTime] of the assignment with the layoutHeader shown
     * @return The parsed layoutHeader text
     */
    private fun getHeaderText(currentDue: DateTime): String {
        val now = DateTime()
        if (currentDue.isBeforeNow && now.dayOfYear - currentDue.dayOfYear >= 1) { // Overdue
            return "Overdue"
        }
        when {
            currentDue.isBefore(now.plusWeeks(1)) -> { // Within same week (Now + 7 days) [Divide for every day]
                val daysUntil = currentDue.dayOfYear - now.dayOfYear
                return when (daysUntil) {
                    0 -> mContext.getString(R.string.assignment_header_near_future_text,
                            "Today")
                    1 -> mContext.getString(R.string.assignment_header_near_future_text,
                            "Tomorrow")
                    else -> mContext.getString(R.string.assignment_header_far_future_text,
                            daysUntil, "Days")
                }
            }
            currentDue.monthOfYear() == now.monthOfYear() -> { // Within same month [Divide for every week]
                val weeksUntil = currentDue.weekOfWeekyear - now.weekOfWeekyear
                return when (weeksUntil) {
                    1 -> mContext.getString(R.string.assignment_header_near_future_text,
                            "Next Week")
                    else -> mContext.getString(R.string.assignment_header_far_future_text,
                            weeksUntil,
                            "Weeks")
                }
            }
            currentDue.year() == now.year() -> // Within same year [Divide for every month]
                return mContext.getString(R.string.assignment_header_near_future_text,
                        "in " + currentDue.toString(mContext.getString(R.string.assignment_header_month_pattern)))
            else -> {
                val yearsUntil = currentDue.year - now.year
                return when (yearsUntil) {
                    1 -> mContext.getString(R.string.assignment_header_near_future_text,
                            "Next Year")
                    else -> mContext.getString(R.string.assignment_header_far_future_text,
                            yearsUntil,
                            "Years")
                }
            }
        }
    }

    companion object {

        private const val NEVER_SHOW_DIVIDER = 0

        private const val VIEW_TYPE_NORMAL = 1 shl 1
        private const val VIEW_TYPE_DIVIDER = 1 shl 2
        private const val VIEW_TYPE_NOTES = 1 shl 3
    }

}
