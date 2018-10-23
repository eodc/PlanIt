package io.eodc.planit.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.FrameLayout
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.adapter.AssignmentType
import io.eodc.planit.adapter.AssignmentTypeAdapter
import io.eodc.planit.db.Assignment
import io.eodc.planit.helper.AssignmentInfoInputHelper
import io.eodc.planit.helper.KeyboardFocusManager
import io.eodc.planit.model.AssignmentListViewModel
import kotlinx.android.synthetic.main.fragment_create_assignment.*
import org.joda.time.DateTime
import java.util.*

/**
 * Fragment that is a bottom sheet. It is the main interface the user interacts with to add
 * assignments to their planner
 *
 * @author 2n
 */
class AddAssignmentFragment : BottomSheetDialogFragment(), AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private var mSelectedClass: Long = 1
    private var mSelectedType: Int = 0
    private var mDueDay: Int = 0
    private var mDueMonth: Int = 0
    private var mDueYear: Int = 0

    private fun addAssignment() {
        if (view != null) {
            val titleText = editTitle.text.toString().trim { it <= ' ' }
            val dueDateText = editDue.text.toString().trim { it <= ' ' }

            if (titleText != "" && dueDateText != "") {
                val newAssignment = Assignment(titleText,
                        mSelectedClass.toInt(),
                        DateTime(mDueYear, mDueMonth, mDueDay, 0, 0),
                        editNotes!!.text.toString())


                when (mSelectedType) {
                    0 -> newAssignment.type = Assignment.TYPE_HOMEWORK
                    1 -> newAssignment.type = Assignment.TYPE_TEST
                    2 -> newAssignment.type = Assignment.TYPE_PROJECT
                }

                Thread {
                            ViewModelProviders.of(this)
                                    .get(AssignmentListViewModel::class.java)
                                    .insertAssignments(newAssignment)
                }.start()
                dismiss()
            } else {
                if (titleText == "") {
                    layoutEditTitle!!.error = "Title can't be empty"
                }
                if (dueDateText == "") {
                    layoutEditDue!!.error = "Due date can't be empty"
                }
            }
        }
    }

    private fun handleDueDateChooser() {
        if (fragmentManager != null) {
            val datePicker = DatePickerFragment.newInstance(this)
            datePicker.show(fragmentManager!!, "datePicker")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.AppTheme_BottomSheet)
        dialog.setOnShowListener { dialog1 ->
            val d = dialog1 as BottomSheetDialog

            val bottomSheet = d.findViewById<FrameLayout>(android.support.design.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTypeSpinner()
        setupClassSpinner()
        setupInputListeners()
    }

    private fun setupInputListeners() {
        editTitle.addTextChangedListener(AssignmentInfoInputHelper(layoutEditTitle))
        view?.setOnTouchListener { v, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> KeyboardFocusManager.clearTextFocus(context,
                        v, editDue, editNotes, editTitle)
                MotionEvent.ACTION_UP -> v.performClick()
            }
            true
        }

        btnCreate.setOnClickListener { addAssignment() }
        editDue.setOnClickListener { handleDueDateChooser() }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent == spinnerType)
            mSelectedType = position
        else if (parent == spinnerClass) mSelectedClass = id + 1
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    /**
     * Sets up the type spinner
     */
    private fun setupTypeSpinner() {
        if (context != null) {
            val types = ArrayList<AssignmentType>()
            types.add(AssignmentType("Homework", R.drawable.ic_homework_black_24dp))
            types.add(AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp))
            types.add(AssignmentType("Project", R.drawable.ic_group_black_24dp))
            val typeAdapter = AssignmentTypeAdapter(context!!, R.layout.item_assignment_type, R.id.textHeaderTitle, types)
            spinnerType!!.adapter = typeAdapter
            spinnerType!!.setSelection(0)
            spinnerType!!.onItemSelectedListener = this
        }
    }

    private fun setupClassSpinner() {
        if (activity != null && context != null) {
            spinnerClass!!.onItemSelectedListener = this
            val subjects = (activity as MainActivity).classes
            val subjectAdapter = ArrayAdapter(context!!,
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    subjects!!)
            spinnerClass!!.adapter = subjectAdapter
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        mDueYear = year
        mDueMonth = month + 1
        mDueDay = dayOfMonth

        val dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

        editDue!!.setText(dueDate.toString(getString(R.string.due_date_pattern)))
    }

}
