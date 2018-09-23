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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.Class;
import io.eodc.planit.model.AssignmentListViewModel;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Fragment that shows a dialog prompting the user to either add or modify a class
 *
 * @author 2n
 */
public class ModifyClassFragment extends DialogFragment implements
        SpectrumDialog.OnColorSelectedListener {

    @BindView(R.id.tv_title)            TextView    mTextTitle;
    @BindView(R.id.tv_subtitle)         TextView    mTextSubtitle;
    @BindView(R.id.edit_class_name)     EditText    mEditClassName;
    @BindView(R.id.edit_teacher_name)   EditText    mEditTeacherName;
    @BindView(R.id.color_picker)        ImageView   mColorPicker;
    @BindView(R.id.btn_confirm)         Button      mBtnConfirm;
    @BindView(R.id.btn_delete)          Button      mBtnDelete;

    private String  mColorChosen;

    private Class   mClass;

    /**
     * Creates an new instance of a ModifyClassFragment
     *
     * @param inClass     Class to modify, or null if new class.
     * @return A new instance of ModifyClassFragment
     */
    public static ModifyClassFragment newInstance(Class inClass) {
        ModifyClassFragment fragment = new ModifyClassFragment();
        fragment.mClass = inClass;
        return fragment;
    }

    /**
     * Shows the color picker dialog when the color picker button is clicked
     */
    @OnClick(R.id.color_picker)
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
                    .setTitle("Delete Class?")
                    .setMessage("Deleting this class will also remove all related assignments from your planner!")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        AssignmentListViewModel assignmentListViewModel =
                                ViewModelProviders.of(this)
                                        .get(AssignmentListViewModel.class);
                        ClassListViewModel classListViewModel =
                                ViewModelProviders.of(this)
                                        .get(ClassListViewModel.class);
                        classListViewModel.getClasses().observe(this, classes -> {
                            if (classes != null) {
                                if (classes.size() > 1) {
                                    List<Assignment> assignments = assignmentListViewModel.getAssignmentsByClassId(mClass.getId()).getValue();

                                    new Thread(() -> classListViewModel.removeClasses(mClass)).start();

                                    if (assignments != null && assignments.size() > 0) {
                                        new Thread(() ->
                                                assignmentListViewModel
                                                        .removeAssignments(
                                                                assignments.toArray(new Assignment[assignments.size()])
                                                        )
                                        ).start();
                                    }
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
            ClassListViewModel viewModel = ViewModelProviders.of(this).get(ClassListViewModel.class);
            if (mClass == null) {
                Class newClass = new Class(className, teacherName, mColorChosen);
                new Thread(() -> viewModel.insertClasses(newClass)).start();
            } else {
                mClass.setName(className);
                mClass.setTeacher(teacherName);
                mClass.setColor(mColorChosen);
                new Thread(() -> viewModel.updateClasses(mClass)).start();
            }
            dismiss();
        } else if (getView() != null) {
            if (className.equals("")) {
                TextInputLayout layout = getView().findViewById(R.id.layout_class_name);
                layout.setError("Class name can't be empty");
            }
            if (teacherName.equals("")) {
                TextInputLayout layout = getView().findViewById(R.id.layout_teacher_name);
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
            if (mClass == null) {
                mTextTitle.setText(R.string.create_class_title);
                mTextSubtitle.setText(R.string.create_class_description);
                mBtnConfirm.setText(R.string.btn_create_label);
            } else {
                mBtnDelete.setVisibility(View.VISIBLE);

                mEditClassName.setText(mClass.getName());
                mEditTeacherName.setText(mClass.getTeacher());
                mColorPicker.setImageDrawable(new ColorDrawable(Color.parseColor(mClass.getColor())));
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
