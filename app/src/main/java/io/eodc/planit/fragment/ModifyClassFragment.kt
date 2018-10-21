package io.eodc.planit.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.thebluealliance.spectrum.SpectrumDialog
import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.model.AssignmentListViewModel
import io.eodc.planit.model.SubjectListViewModel

/**
 * Fragment that shows a dialog prompting the user to either add or modify a class
 *
 * @author 2n
 */
class ModifyClassFragment : DialogFragment(), SpectrumDialog.OnColorSelectedListener {

    @BindView(R.id.text_title)
    internal var mTextTitle: TextView? = null
    @BindView(R.id.text_subtitle)
    internal var mTextSubtitle: TextView? = null
    @BindView(R.id.edit_title)
    internal var mEditClassName: EditText? = null
    @BindView(R.id.edit_teacher)
    internal var mEditTeacherName: EditText? = null
    @BindView(R.id.picker_class_color)
    internal var mColorPicker: ImageView? = null
    @BindView(R.id.btn_confirm)
    internal var mBtnConfirm: Button? = null
    @BindView(R.id.btn_delete)
    internal var mBtnDelete: Button? = null

    private var mColorChosen: String? = null

    private var mSubject: Subject? = null

    /**
     * Shows the color picker dialog when the color picker button is clicked
     */
    @OnClick(R.id.picker_class_color)
    internal fun showColorPicker() {
        if (fragmentManager != null) {
            SpectrumDialog.Builder(context)
                    .setColors(R.array.spectrum_colors)
                    .setSelectedColorRes(R.color.class_red)
                    .setDismissOnColorSelected(false)
                    .setOnColorSelectedListener(this)
                    .build().show(fragmentManager!!, null)
        }
    }

    /**
     * Dismisses this dialog when the cancel button is clicked
     */
    @OnClick(R.id.btn_cancel)
    internal fun dismissDialog() {
        dismiss()
    }

    /**
     * Shows the delete dialog when the delete button is pressed
     */
    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_delete)
    internal fun showDeleteDialog() {
        if (activity != null) {
            AlertDialog.Builder(context)
                    .setTitle("Delete Subject?")
                    .setMessage("Deleting this class will also remove all related assignments from your planner!")
                    .setPositiveButton("Delete") { dialog, which ->
                        val assignmentListViewModel = ViewModelProviders.of(this)
                                .get<AssignmentListViewModel>(AssignmentListViewModel::class.java!!)
                        val subjectListViewModel = ViewModelProviders.of(this)
                                .get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
                        subjectListViewModel.subjectsObservable.observe(this, { subjects ->
                            if (subjects != null) {
                                if (subjects!!.size > 1) {
                                    assignmentListViewModel.getAssignmentsByClassId(mSubject!!.id).observe(this, { assignments ->
                                        Thread { subjectListViewModel.removeSubjects(mSubject) }.start()

                                        if (assignments != null && assignments!!.size > 0) {
                                            Thread {
                                                assignmentListViewModel
                                                        .removeAssignments(
                                                                *assignments!!.toTypedArray()
                                                        )
                                            }.start()
                                        }
                                    })
                                    dismiss()
                                } else {
                                    Toast.makeText(context, "Cannot delete last class from planner!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        })
                    }.show()
        }
    }

    /**
     * Confirms this dialog's action, whether its editing a class or adding one.
     */
    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_confirm)
    internal fun setBtnConfirm() {
        val className = mEditClassName!!.text.toString().trim { it <= ' ' }
        val teacherName = mEditTeacherName!!.text.toString().trim { it <= ' ' }

        if (className != "" &&
                teacherName != "" &&
                context != null) {
            val viewModel = ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java!!)
            if (mSubject == null) {
                val newSubject = Subject(className, teacherName, mColorChosen)
                Thread { viewModel.insertSubjects(newSubject) }.start()
            } else {
                mSubject!!.name = className
                mSubject!!.teacher = teacherName
                mSubject!!.color = mColorChosen
                Thread { viewModel.updateSubjects(mSubject) }.start()
            }
            dismiss()
        } else if (view != null) {
            if (className == "") {
                val layout = view!!.findViewById<TextInputLayout>(R.id.layout_edit_title)
                layout.error = "Subject name can't be empty"
            }
            if (teacherName == "") {
                val layout = view!!.findViewById<TextInputLayout>(R.id.layout_edit_teacher)
                layout.error = "Teacher name can't be empty"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_modify_class, container, false)
        ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (context != null) {
            if (mSubject == null) {
                mTextTitle!!.setText(R.string.create_class_title)
                mTextSubtitle!!.setText(R.string.create_class_description)
                mBtnConfirm!!.setText(R.string.btn_create_label)
            } else {
                mBtnDelete!!.visibility = View.VISIBLE

                mEditClassName!!.setText(mSubject!!.name)
                mEditTeacherName!!.setText(mSubject!!.teacher)
                mColorPicker!!.setImageDrawable(ColorDrawable(Color.parseColor(mSubject!!.color)))
            }

            mColorChosen = "#" + Integer.toHexString(ContextCompat.getColor(context!!, R.color.class_red))
        }
    }

    override fun onResume() {
        super.onResume()
        if (dialog != null && dialog.window != null) {
            val width = resources.getDimension(R.dimen.edit_dialog_width).toInt()
            val height = resources.getDimension(R.dimen.mod_class_dialog_height).toInt()
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onColorSelected(positiveResult: Boolean, color: Int) {
        if (positiveResult) {
            mColorPicker!!.setImageDrawable(ColorDrawable(color))
            mColorChosen = "#" + Integer.toHexString(color)
        }
    }

    companion object {

        /**
         * Creates an new instance of a ModifyClassFragment
         *
         * @param inSubject     Subject to modify, or null if new class.
         * @return A new instance of ModifyClassFragment
         */
        fun newInstance(inSubject: Subject): ModifyClassFragment {
            val fragment = ModifyClassFragment()
            fragment.mSubject = inSubject
            return fragment
        }
    }
}
