package io.eodc.planit.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
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
import org.joda.time.DateTime
import java.util.*

/**
 * Created by 2n on 5/16/18.
 */

class ModifyAssignmentFragment : DialogFragment(), DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    @BindView(R.id.btn_restore)
    internal var mBtnRestore: Button? = null
    @BindView(R.id.spinner_type)
    internal var mSpinnerType: Spinner? = null
    @BindView(R.id.spinner_class)
    internal var mSpinnerClass: Spinner? = null
    @BindView(R.id.edit_title)
    internal var mEditTitle: EditText? = null
    @BindView(R.id.edit_due_date)
    internal var mEditDueDate: EditText? = null
    @BindView(R.id.edit_notes)
    internal var mEditNotes: EditText? = null
    @BindView(R.id.layout_edit_title)
    internal var mLayoutEditTitle: TextInputLayout? = null
    @BindView(R.id.layout_edit_due)
    internal var mLayoutEditDueDate: TextInputLayout? = null

    private var mAssignment: Assignment? = null

    private var mSelectedClassId: Long = 1
    private var mSelectedType: Int = 0
    private var mDueDay: Int = 0
    private var mDueMonth: Int = 0
    private var mDueYear: Int = 0

    @OnClick(R.id.btn_cancel)
    internal fun dismissDialog() {
        dismiss()
    }

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_confirm)
    internal fun editAssignment() {
        val title = mEditTitle!!.text.toString().trim { it <= ' ' }
        val notes = mEditNotes!!.text.toString().trim { it <= ' ' }
        val dueDate = mEditDueDate!!.text.toString().trim { it <= ' ' }

        if (title == "" || mEditDueDate!!.text.toString().trim { it <= ' ' } == "") {
            if (title == "") {
                mLayoutEditTitle!!.error = "Title cannot be empty."
            }
            if (dueDate == "") {
                mLayoutEditDueDate!!.error = "Due date can't be empty"
            }
        } else {
            mAssignment!!.title = title
            mAssignment!!.notes = notes
            mAssignment!!.classId = mSelectedClassId.toInt()
            when (mSelectedType) {
                0 -> mAssignment!!.type = Assignment.TYPE_HOMEWORK
                1 -> mAssignment!!.type = Assignment.TYPE_TEST
                2 -> mAssignment!!.type = Assignment.TYPE_PROJECT
            }

            mAssignment!!.dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

            Thread { PlannerDatabase.getInstance(context)!!.assignmentDao().updateAssignment(mAssignment) }.start()
            dismiss()
        }
    }

    @OnClick(R.id.edit_due_date)
    internal fun showDatePicker() {
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
        val view = inflater.inflate(R.layout.dialog_edit_assignment, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClassSpinner()
    }

    private fun setupInputListeners() {
        mEditTitle!!.addTextChangedListener(AssignmentInfoInputHelper(mLayoutEditTitle))
        if (view != null) {
            view!!.setOnTouchListener { v, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> KeyboardFocusManager.clearTextFocus(context, v,
                            mEditDueDate, mEditNotes, mEditTitle)
                    MotionEvent.ACTION_UP -> v.performClick()
                }
                true
            }
        }
    }

    private fun setupTypeSpinner() {
        if (context != null) {
            val types = object : ArrayList<AssignmentType>() {
                init {
                    add(AssignmentType("Homework", R.drawable.ic_homework_black_24dp))
                    add(AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp))
                    add(AssignmentType("Project", R.drawable.ic_group_black_24dp))
                }
            }

            val typeAdapter = AssignmentTypeAdapter(context!!,
                    R.layout.item_assignment_type,
                    R.id.text_title,
                    types)
            mSpinnerType!!.adapter = typeAdapter
            mSpinnerType!!.onItemSelectedListener = this
        }
    }

    private fun setupClassSpinner() {
        if (context != null && activity != null) {
            val subjects = (activity as MainActivity).classes
            val mClassAdapter = ArrayAdapter(context!!,
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    subjects!!)
            mSpinnerClass!!.adapter = mClassAdapter
            mSpinnerClass!!.onItemSelectedListener = this

            mSpinnerClass!!.setSelection(Iterables.indexOf(subjects
            ) { c -> c.id == mAssignment!!.classId })

            setupTypeSpinner()
            fillAssignmentInfo()
            setupInputListeners()
        }
    }

    private fun fillAssignmentInfo() {
        val dtDue = mAssignment!!.dueDate
        mEditTitle!!.setText(mAssignment!!.title)
        mEditDueDate!!.setText(dtDue!!.toString("dd MMM, YYYY"))
        mEditNotes!!.setText(mAssignment!!.notes)

        mDueDay = dtDue.dayOfMonth
        mDueMonth = dtDue.monthOfYear
        mDueYear = dtDue.year
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        mDueDay = dayOfMonth
        mDueMonth = month + 1
        mDueYear = year

        val dueDate = DateTime(mDueYear, mDueMonth, mDueDay, 0, 0)

        mEditDueDate!!.setText(dueDate.toString(getString(R.string.due_date_pattern)))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent == mSpinnerType)
            mSelectedType = position
        else if (parent == mSpinnerClass) {
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
