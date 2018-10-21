package io.eodc.planit.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.common.collect.Iterables
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.db.Assignment
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifyAssignmentFragment
import org.joda.time.DateTime

/**
 * Adapter for interfacing mAssignments to [RecyclerView]
 *
 * @author 2n
 */
class AssignmentAdapter : RecyclerView.Adapter<AssignmentViewHolder> {

    private var mContext: Context? = null


    private val mSubjects: List<Subject>

    private var mAssignments: List<Assignment>? = null

    private var mShowDividerFlag: Int = 0

    /**
     * Constructs a new instance of AssignmentAdapter. Dynamically displays relevant information
     * about the assignment in its view depending on its position in the list and it due date.
     *
     * @param context     The context to use for grabbing strings, colors, etc.
     * @param assignments The list of assignments to show
     * @param subjects     The LiveData containing all the user's subjects.
     * @see AssignmentViewHolder
     */
    constructor(context: Context, assignments: List<Assignment>, subjects: List<Subject>) {
        this.mContext = context
        this.mAssignments = assignments
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
    constructor(context: Context, assignments: List<Assignment>, subjects: List<Subject>, showDividers: Boolean) {
        this.mContext = context
        this.mAssignments = assignments
        this.mSubjects = subjects
        this.mShowDividerFlag = if (showDividers) -1 else NEVER_SHOW_DIVIDER
        setHasStableIds(true)
    }

    fun swapAssignmentsList(newList: List<Assignment>) {
        mAssignments = newList
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
        val currentAssignment = mAssignments!![position]
        val hasNotes = currentAssignment.notes!!
                .trim { it <= ' ' } != ""

        if (mShowDividerFlag == NEVER_SHOW_DIVIDER) {
            return getNormalViewType(hasNotes)
        }
        if (position == 0) {
            return getDividerViewType(hasNotes)
        } else {
            val previousDue = mAssignments!![position - 1]
                    .dueDate
            val currentDue = currentAssignment.dueDate
            val now = DateTime()

            if (currentDue!!.isBeforeNow && now.dayOfYear - currentDue.dayOfYear >= 1) { // Overdue [Don't divide]
                return getNormalViewType(hasNotes)
            }

            return if (currentDue.isBefore(now.plusWeeks(1))) { // Within same week (Now + 7 days) [Divide for every day]
                if (currentDue.dayOfWeek() != previousDue!!.dayOfWeek())
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else if (currentDue.monthOfYear() == now.monthOfYear()) { // Within same month [Divide for every week]
                if (previousDue!!.isBefore(now.plusWeeks(1)) || currentDue.weekOfWeekyear() != previousDue.weekOfWeekyear())
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else if (currentDue.year() == now.year()) { // Within same year [Divide for every month]
                if (currentDue.monthOfYear() != previousDue!!.monthOfYear() || previousDue.isBefore(now.plusWeeks(1)))
                    getDividerViewType(hasNotes)
                else
                    getNormalViewType(hasNotes)
            } else {
                if (currentDue.year() != previousDue!!.year())
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssignmentViewHolder {
        return AssignmentViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_assignment, parent, false))
    }

    override fun onBindViewHolder(holder: AssignmentViewHolder, position: Int) {
        val assignment = mAssignments!![position]
        var previousAssignment: Assignment? = null
        if (position > 0) {
            previousAssignment = mAssignments!![position - 1]
        }

        val assignmentSubject = Iterables.find(mSubjects) { value -> value.id == assignment.classId }

        val dtCurrent = assignment.dueDate
        val assignmentTypeFlag = assignment.type

        var assignmentType = ""

        when (assignmentTypeFlag) {
            Assignment.TYPE_TEST -> assignmentType = "Test/Quiz"
            Assignment.TYPE_PROJECT -> assignmentType = "Project"
            Assignment.TYPE_HOMEWORK -> assignmentType = "Homework"
        }


        val classAndTypeText = mContext!!.getString(R.string.class_name_and_type_text,
                assignmentSubject.name,
                assignmentType)

        holder.textClassType!!.text = classAndTypeText
        holder.textDueDate!!.text = dtCurrent!!.toString(mContext!!.getString(R.string.due_date_pattern))
        holder.assignment = assignment

        if (holder.itemViewType and VIEW_TYPE_DIVIDER == VIEW_TYPE_DIVIDER) {
            val headerText = getHeaderText(dtCurrent)
            holder.textHeader!!.text = headerText
            holder.layoutHeader!!.visibility = View.VISIBLE
            holder.showDueDate()
        } else if (previousAssignment != null && assignment.dueDate!!.dayOfYear() != previousAssignment.dueDate!!.dayOfYear()) {
            holder.showDueDate()
        } else {
            holder.hideDueDate()
        }

        if (holder.itemViewType and VIEW_TYPE_NOTES == VIEW_TYPE_NOTES) {
            holder.iconExpand!!.visibility = View.VISIBLE
            holder.textNotes!!.text = assignment.notes
            holder.itemView.setOnClickListener { view -> holder.handleNoteClick() }
        }

        holder.itemView.setOnLongClickListener { view ->
            if (mContext is MainActivity) {
                val activity = mContext as MainActivity?
                val editFragment = ModifyAssignmentFragment.newInstance(holder.assignment)

                editFragment.show(activity!!.supportFragmentManager, null)

                val v = mContext!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect
                                .createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    else
                        v.vibrate(500)
                }
            }
            true
        }

        holder.imageClassColor!!.setBackgroundColor(Color.parseColor(assignmentSubject.color))
        holder.textAssignmentName!!.text = assignment.title
    }

    override fun getItemCount(): Int {
        return if (mAssignments == null) 0 else mAssignments!!.size
    }

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
        if (currentDue.isBefore(now.plusWeeks(1))) { // Within same week (Now + 7 days) [Divide for every day]
            val daysUntil = currentDue.dayOfYear - now.dayOfYear
            when (daysUntil) {
                0 -> return mContext!!.getString(R.string.assignment_header_near_future_text,
                        "Today")
                1 -> return mContext!!.getString(R.string.assignment_header_near_future_text,
                        "Tomorrow")
                else -> return mContext!!.getString(R.string.assignment_header_far_future_text,
                        daysUntil, "Days")
            }
        } else if (currentDue.monthOfYear() == now.monthOfYear()) { // Within same month [Divide for every week]
            val weeksUntil = currentDue.weekOfWeekyear - now.weekOfWeekyear
            when (weeksUntil) {
                1 -> return mContext!!.getString(R.string.assignment_header_near_future_text,
                        "Next Week")
                else -> return mContext!!.getString(R.string.assignment_header_far_future_text,
                        weeksUntil,
                        "Weeks")
            }
        } else if (currentDue.year() == now.year()) { // Within same year [Divide for every month]
            return mContext!!.getString(R.string.assignment_header_near_future_text,
                    "in " + currentDue.toString(mContext!!.getString(R.string.assignment_header_month_pattern)))
        } else {
            val yearsUntil = currentDue.year - now.year
            when (yearsUntil) {
                1 -> return mContext!!.getString(R.string.assignment_header_near_future_text,
                        "Next Year")
                else -> return mContext!!.getString(R.string.assignment_header_far_future_text,
                        yearsUntil,
                        "Years")
            }
        }
    }

    companion object {

        private val NEVER_SHOW_DIVIDER = 0

        private val VIEW_TYPE_NORMAL = 1 shl 1
        private val VIEW_TYPE_DIVIDER = 1 shl 2
        private val VIEW_TYPE_NOTES = 1 shl 3
    }

}
