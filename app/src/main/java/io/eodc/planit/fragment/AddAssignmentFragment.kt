package io.eodc.planit.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import io.eodc.planit.R
import io.eodc.planit.activity.MainActivity
import io.eodc.planit.adapter.AssignmentType
import io.eodc.planit.adapter.AssignmentTypeAdapter
import io.eodc.planit.db.Assignment
import io.eodc.planit.db.PlannerDatabase
import io.eodc.planit.helper.AssignmentInfoInputHelper
import io.eodc.planit.helper.KeyboardFocusManager
import org.joda.time.DateTime
import java.util.*

/**
 * Fragment that is a bottom sheet. It is the main interface the user interacts with to add
 * assignments to their planner
 *
 * @author 2n
 */
class AddAssignmentFragment : BottomSheetDialogFragment(), AdapterView.OnItemSelectedListener, DatePickerDialog.OnDateSetListener {

    @BindView(R.id.edit_due)
    internal var mEditDueDate: EditText? = null
    @BindView(R.id.edit_title)
    internal var mEditTitle: EditText? = null
    @BindView(R.id.edit_notes)
    internal var mEditNotes: EditText? = null
    @BindView(R.id.layout_edit_title)
    internal var mLayoutEditTitle: TextInputLayout? = null
    @BindView(R.id.layout_edit_due)
    internal var mLayoutEditDueDate: TextInputLayout? = null
    @BindView(R.id.spinner_type)
    internal var mTypeSpinner: Spinner? = null
    @BindView(R.id.spinner_class)
    internal var mClassSpinner: Spinner? = null

    private var mSelectedClass: Long = 1
    private var mSelectedType: Int = 0
    private var mDueDay: Int = 0
    private var mDueMonth: Int = 0
    private var mDueYear: Int = 0

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_create)
    internal fun addAssignment() {
        if (view != null) {
            val titleText = mEditTitle!!.text.toString().trim { it <= ' ' }
            val dueDateText = mEditDueDate!!.text.toString().trim { it <= ' ' }

            if (titleText != "" && dueDateText != "") {
                val newAssignment = Assignment(titleText,
                        mSelectedClass.toInt(),
                        DateTime(mDueYear, mDueMonth, mDueDay, 0, 0),
                        mEditNotes!!.text.toString())


                when (mSelectedType) {
                    0 -> newAssignment.type = Assignment.TYPE_HOMEWORK
                    1 -> newAssignment.type = Assignment.TYPE_TEST
                    2 -> newAssignment.type = Assignment.TYPE_PROJECT
                }

                Thread {
                    PlannerDatabase.getInstance(context)!!
                            .assignmentDao()
                            .insertAssignments(newAssignment)
                }.start()
                dismiss()
            } else {
                if (titleText == "") {
                    mLayoutEditTitle!!.error = "Title can't be empty"
                }
                if (dueDateText == "") {
                    mLayoutEditDueDate!!.error = "Due date can't be empty"
                }
            }
        }
    }

    @OnClick(R.id.edit_due)
    internal fun handleDueDateChooser() {
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
        val v = inflater.inflate(R.layout.fragment_create_assignment, container, false)
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTypeSpinner()
        setupClassSpinner()
        setupInputListeners()
    }

    private fun setupInputListeners() {
        mEditTitle!!.addTextChangedListener(AssignmentInfoInputHelper(mLayoutEditTitle))
        if (view != null) {
            view!!.setOnTouchListener { v, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> KeyboardFocusManager.clearTextFocus(context,
                            v, mEditDueDate, mEditNotes, mEditTitle)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent == mTypeSpinner)
            mSelectedType = position
        else if (parent == mClassSpinner) mSelectedClass = id + 1
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
            val typeAdapter = AssignmentTypeAdapter(context!!, R.layout.item_assignment_type, R.id.text_title, types)
            mTypeSpinner!!.adapter = typeAdapter
            mTypeSpinner!!.setSelection(0)
            mTypeSpinner!!.onItemSelectedListener = this
        }
    }

    private fun setupClassSpinner() {
        if (activity != null && context != null) {
            mClassSpinner!!.onItemSelectedListener = this
            val subjects = (activity as MainActivity).classes
            val subjectAdapter = ArrayAdapter(context!!,
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    subjects!!)
            mClassSpinner!!.adapter = subjectAdapter
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        mDueYear = year
        mDueMonth = month + 1
        mDueDay = dayOfMonth

        val dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

        mEditDueDate!!.setText(dueDate.toString(getString(R.string.due_date_pattern)))
    }

}
