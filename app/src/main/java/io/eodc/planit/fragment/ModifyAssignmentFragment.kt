package io.eodc.planit.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import com.google.common.collect.Iterables
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.adapter.AssignmentType
import io.eodc.planit.adapter.AssignmentTypeAdapter
import io.eodc.planit.db.Assignment
import io.eodc.planit.db.PlannerDatabase
import io.eodc.planit.db.Subject
import io.eodc.planit.helper.AssignmentInfoInputHelper
import io.eodc.planit.helper.KeyboardFocusManager
import kotlinx.android.synthetic.main.dialog_edit_assignment.*
import org.joda.time.DateTime

/**
 * Created by 2n on 5/16/18.
 */

class ModifyAssignmentFragment : androidx.fragment.app.DialogFragment(), DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {
    private lateinit var mAssignment: Assignment

    private var mSelectedClassId: Long = 1
    private var mSelectedType: Int = 0
    private var mDueDay: Int = 0
    private var mDueMonth: Int = 0
    private var mDueYear: Int = 0

    private fun editAssignment() {
        val title = editTitle.text.toString().trim { it <= ' ' }
        val notes = editNotes.text.toString().trim { it <= ' ' }
        val dueDate = editDue.text.toString().trim { it <= ' ' }

        if (title == "" || editDue.text.toString().trim { it <= ' ' } == "") {
            if (title == "") {
                layoutEditTitle.error = "Title cannot be empty."
            }
            if (dueDate == "") {
                layoutEditDue!!.error = "Due date can't be empty"
            }
        } else {
            mAssignment.title = title
            mAssignment.notes = notes
            mAssignment.classId = mSelectedClassId.toInt()
            when (mSelectedType) {
                0 -> mAssignment.type = Assignment.TYPE_HOMEWORK
                1 -> mAssignment.type = Assignment.TYPE_TEST
                2 -> mAssignment.type = Assignment.TYPE_PROJECT
            }

            mAssignment.dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

            Thread { context?.let { PlannerDatabase.getInstance(it) }
                    ?.assignmentDao()
                    ?.updateAssignment(mAssignment) }.start()
            dismiss()
        }
    }

    private fun showDatePicker() {
        val fragment = DatePickerFragment.newInstance(this, mDueYear, mDueMonth, mDueDay)

        if (fragmentManager != null) fragment.show(fragmentManager!!, null)
    }

    override fun onResume() {
        super.onResume()
        if (dialog != null && dialog.window != null) {
            val width = resources.getDimension(R.dimen.edit_dialog_width).toInt()
            val height = resources.getDimension(R.dimen.edit_dialog_height).toInt()
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_edit_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClassSpinner()
    }

    private fun setupInputListeners() {
        editTitle.addTextChangedListener(AssignmentInfoInputHelper(layoutEditTitle))
        if (view != null) {
            view!!.setOnTouchListener { v, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> KeyboardFocusManager.clearTextFocus(context, v,
                            editDue, editNotes, editTitle)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }
        }

        btnCancel.setOnClickListener { it ->
            Thread {
                context?.let { PlannerDatabase.getInstance(it) }
                        ?.assignmentDao()
                        ?.updateAssignment(mAssignment)
            }.start()
            dismiss()
        } // Force a redraw of the recyclerview
        btnConfirm.setOnClickListener { editAssignment() }
        editDue.setOnClickListener { showDatePicker() }
    }

    private fun setupTypeSpinner() {
        if (context != null) {
            val types = arrayListOf(
                    AssignmentType("Homework", R.drawable.ic_homework_black_24dp),
                    AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp),
                    AssignmentType("Project", R.drawable.ic_group_black_24dp))

            val typeAdapter = AssignmentTypeAdapter(context!!,
                    R.layout.item_assignment_type,
                    R.id.textHeaderTitle,
                    types)
            spinnerType.adapter = typeAdapter
            spinnerType.onItemSelectedListener = this

            val position =
                    when (mAssignment.type) {
                        Assignment.TYPE_HOMEWORK -> 0
                        Assignment.TYPE_TEST -> 1
                        Assignment.TYPE_PROJECT -> 2
                        else -> 0
                    }
            spinnerType.setSelection(position)
        }
    }

    private fun setupClassSpinner() {
        if (context != null && activity != null) {
            val subjects = (activity as MainActivity).subjects
            val mClassAdapter = ArrayAdapter(context!!,
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    subjects!!)
            spinnerSubject.adapter = mClassAdapter
            spinnerSubject.onItemSelectedListener = this

            spinnerSubject.setSelection(Iterables.indexOf(subjects) { c ->
                c?.id == mAssignment.classId
            })

            setupTypeSpinner()
            fillAssignmentInfo()
            setupInputListeners()
        }
    }

    private fun fillAssignmentInfo() {
        val dtDue = mAssignment.dueDate
        editTitle.setText(mAssignment.title)
        editDue.setText(dtDue.toString("dd MMM, YYYY"))
        editNotes.setText(mAssignment.notes)

        mDueDay = dtDue.dayOfMonth
        mDueMonth = dtDue.monthOfYear
        mDueYear = dtDue.year
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        mDueDay = dayOfMonth
        mDueMonth = month + 1
        mDueYear = year

        val dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

        editDue.setText(dueDate.toString(getString(R.string.due_date_pattern)))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent == spinnerType)
            mSelectedType = position
        else if (parent == spinnerSubject) {
            val selected = parent.selectedItem as Subject
            mSelectedClassId = selected.id.toLong()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    companion object {

        fun newInstance(assignment: Assignment): ModifyAssignmentFragment {
            val fragment = ModifyAssignmentFragment()
            fragment.mAssignment = assignment
            return fragment
        }
    }
}
