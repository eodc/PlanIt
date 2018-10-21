package io.eodc.planit.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.thebluealliance.spectrum.SpectrumDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.Subject;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.SubjectListViewModel;

/**
 * Fragment that shows a dialog prompting the user to either add or modify a class
 *
 * @author 2n
 */
public class ModifyClassFragment extends DialogFragment implements
        SpectrumDialog.OnColorSelectedListener {

    @BindView(R.id.text_title)            TextView    mTextTitle;
    @BindView(R.id.text_subtitle)         TextView    mTextSubtitle;
    @BindView(R.id.edit_title)     EditText    mEditClassName;
    @BindView(R.id.edit_teacher)   EditText    mEditTeacherName;
    @BindView(R.id.picker_class_color)        ImageView   mColorPicker;
    @BindView(R.id.btn_confirm)         Button      mBtnConfirm;
    @BindView(R.id.btn_delete)          Button      mBtnDelete;

    private String  mColorChosen;

    private Subject mSubject;

    /**
     * Creates an new instance of a ModifyClassFragment
     *
     * @param inSubject     Subject to modify, or null if new class.
     * @return A new instance of ModifyClassFragment
     */
    public static ModifyClassFragment newInstance(Subject inSubject) {
        ModifyClassFragment fragment = new ModifyClassFragment();
        fragment.mSubject = inSubject;
        return fragment;
    }

    /**
     * Shows the color picker dialog when the color picker button is clicked
     */
    @OnClick(R.id.picker_class_color)
    void showColorPicker() {
        if (getFragmentManager() != null) {
            new SpectrumDialog.Builder(getContext())
                    .setColors(R.array.spectrum_colors)
                    .setSelectedColorRes(R.color.class_red)
                    .setDismissOnColorSelected(false)
                    .setOnColorSelectedListener(this)
                    .build().show(getFragmentManager(), null);
        }
    }

    /**
     * Dismisses this dialog when the cancel button is clicked
     */
    @OnClick(R.id.btn_cancel)
    void dismissDialog() {
        dismiss();
    }

    /**
     * Shows the delete dialog when the delete button is pressed
     */
    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_delete)
    void showDeleteDialog() {
        if (getActivity() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Subject?")
                    .setMessage("Deleting this class will also remove all related assignments from your planner!")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        AssignmentListViewModel assignmentListViewModel =
                                ViewModelProviders.of(this)
                                        .get(AssignmentListViewModel.class);
                        SubjectListViewModel subjectListViewModel =
                                ViewModelProviders.of(this)
                                        .get(SubjectListViewModel.class);
                        subjectListViewModel.getSubjectsObservable().observe(this, subjects -> {
                            if (subjects != null) {
                                if (subjects.size() > 1) {
                                    assignmentListViewModel.getAssignmentsByClassId(mSubject.getId()).observe(this, assignments -> {
                                        new Thread(() -> subjectListViewModel.removeSubjects(mSubject)).start();

                                        if (assignments != null && assignments.size() > 0) {
                                            new Thread(() -> assignmentListViewModel
                                                    .removeAssignments(
                                                            assignments.toArray(new Assignment[assignments.size()])
                                                    )
                                            ).start();
                                        }
                                    });
                                    dismiss();
                                } else {
                                    Toast.makeText(getContext(), "Cannot delete last class from planner!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }).show();
        }
    }

    /**
     * Confirms this dialog's action, whether its editing a class or adding one.
     */
    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_confirm)
    void setBtnConfirm() {
        String className = mEditClassName.getText().toString().trim();
        String teacherName = mEditTeacherName.getText().toString().trim();

        if (!className.equals("") &&
                !teacherName.equals("") &&
                getContext() != null) {
            SubjectListViewModel viewModel = ViewModelProviders.of(this).get(SubjectListViewModel.class);
            if (mSubject == null) {
                Subject newSubject = new Subject(className, teacherName, mColorChosen);
                new Thread(() -> viewModel.insertSubjects(newSubject)).start();
            } else {
                mSubject.setName(className);
                mSubject.setTeacher(teacherName);
                mSubject.setColor(mColorChosen);
                new Thread(() -> viewModel.updateSubjects(mSubject)).start();
            }
            dismiss();
        } else if (getView() != null) {
            if (className.equals("")) {
                TextInputLayout layout = getView().findViewById(R.id.layout_edit_title);
                layout.setError("Subject name can't be empty");
            }
            if (teacherName.equals("")) {
                TextInputLayout layout = getView().findViewById(R.id.layout_edit_teacher);
                layout.setError("Teacher name can't be empty");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_modify_class, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getContext() != null) {
            if (mSubject == null) {
                mTextTitle.setText(R.string.create_class_title);
                mTextSubtitle.setText(R.string.create_class_description);
                mBtnConfirm.setText(R.string.btn_create_label);
            } else {
                mBtnDelete.setVisibility(View.VISIBLE);

                mEditClassName.setText(mSubject.getName());
                mEditTeacherName.setText(mSubject.getTeacher());
                mColorPicker.setImageDrawable(new ColorDrawable(Color.parseColor(mSubject.getColor())));
            }

            mColorChosen = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.class_red));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) getResources().getDimension(R.dimen.edit_dialog_width);
            int height = (int) getResources().getDimension(R.dimen.mod_class_dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onColorSelected(boolean positiveResult, int color) {
        if (positiveResult) {
            mColorPicker.setImageDrawable(new ColorDrawable(color));
            mColorChosen = "#" + Integer.toHexString(color);
        }
    }
}
