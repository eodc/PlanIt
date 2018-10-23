package io.eodc.planit.fragment

import android.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.thebluealliance.spectrum.SpectrumDialog
import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.model.AssignmentListViewModel
import io.eodc.planit.model.SubjectListViewModel
import kotlinx.android.synthetic.main.dialog_modify_class.*

/**
 * Fragment that shows a dialog prompting the user to either add or modify a class
 *
 * @author 2n
 */
class ModifyClassFragment : androidx.fragment.app.DialogFragment(), SpectrumDialog.OnColorSelectedListener {
    private var mSubject: Subject? = null
    private lateinit var mColorChosen: String

    /**
     * Shows the color picker dialog when the color picker button is clicked
     */
    private fun showColorPicker() {
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
     * Shows the delete dialog when the delete button is pressed
     */
    private fun showDeleteDialog() {
        if (activity != null) {
            AlertDialog.Builder(context)
                    .setTitle("Delete Subject?")
                    .setMessage("Deleting this class will also remove all related assignments from your planner!")
                    .setPositiveButton("Delete") { _, _ ->
                        val assignmentListViewModel = ViewModelProviders.of(this)
                                .get<AssignmentListViewModel>(AssignmentListViewModel::class.java)
                        val subjectListViewModel = ViewModelProviders.of(this)
                                .get<SubjectListViewModel>(SubjectListViewModel::class.java)
                        subjectListViewModel.subjectsObservable.observe(this, Observer { subjects ->
                            if (subjects != null) {
                                if (subjects.size > 1) {
                                    assignmentListViewModel.getAssignmentsByClassId(mSubject!!.id).observe(this, Observer { assignments ->
                                        Thread { subjectListViewModel.removeSubjects(mSubject!!) }.start()

                                        if (assignments != null && assignments.isNotEmpty()) {
                                            Thread {
                                                assignmentListViewModel
                                                        .removeAssignments(*assignments.toTypedArray())
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
    private fun confirmChanges() {
        val className = editTitle.text.toString().trim { it <= ' ' }
        val teacherName = editTeacherName.text.toString().trim { it <= ' ' }

        if (className != "" &&
                teacherName != "" &&
                context != null) {
            val viewModel = ViewModelProviders.of(this).get<SubjectListViewModel>(SubjectListViewModel::class.java)
            if (mSubject == null) {
                val newSubject = Subject(className, teacherName, mColorChosen)
                Thread { viewModel.insertSubjects(newSubject) }.start()
            } else {
                mSubject!!.name = className
                mSubject!!.teacher = teacherName
                mSubject!!.color = mColorChosen
                Thread { viewModel.updateSubjects(mSubject!!) }.start()
            }
            dismiss()
        } else if (view != null) {
            if (className == "") {
                val layout = view!!.findViewById<TextInputLayout>(R.id.layoutEditTitle)
                layout.error = "Subject name can't be empty"
            }
            if (teacherName == "") {
                val layout = view!!.findViewById<TextInputLayout>(R.id.layout_edit_teacher)
                layout.error = "Teacher name can't be empty"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_modify_class, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mSubject == null) {
            textHeaderTitle.setText(R.string.create_class_title)
            textHeaderSubtitle.setText(R.string.create_class_description)
            btnConfirm.setText(R.string.btn_create_label)
        } else {
            btnDelete.visibility = View.VISIBLE

            editTitle.setText(mSubject!!.name)
            editTeacherName.setText(mSubject!!.teacher)
            pickerColorClass.setImageDrawable(ColorDrawable(Color.parseColor(mSubject!!.color)))
        }

        mColorChosen = "#" + Integer.toHexString(ContextCompat.getColor(context!!, R.color.class_red))
        setupInputListeners()
    }

    private fun setupInputListeners() {
        btnConfirm.setOnClickListener { confirmChanges() }
        btnDelete.setOnClickListener { showDeleteDialog() }
        btnCancel.setOnClickListener { dismiss() }
        pickerColorClass.setOnClickListener { showColorPicker() }
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
            pickerColorClass.setImageDrawable(ColorDrawable(color))
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
        fun newInstance(inSubject: Subject?): ModifyClassFragment {
            val fragment = ModifyClassFragment()
            fragment.mSubject = inSubject
            return fragment
        }
    }
}
