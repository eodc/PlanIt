package io.eodc.planit.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.common.collect.Iterables;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentType;
import io.eodc.planit.adapter.AssignmentTypeAdapter;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.Class;
import io.eodc.planit.db.PlannerDatabase;
import io.eodc.planit.helper.AssignmentInfoInputHelper;
import io.eodc.planit.helper.KeyboardFocusManager;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Created by 2n on 5/16/18.
 */

public class ModifyAssignmentFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.btn_restore)                 Button              mBtnRestore;
    @BindView(R.id.spinner_type)                Spinner             mSpinnerType;
    @BindView(R.id.spinner_class)               Spinner             mSpinnerClass;
    @BindView(R.id.edit_title)        EditText            mEditTitle;
    @BindView(R.id.edit_due_date)               EditText            mEditDueDate;
    @BindView(R.id.edit_notes)                  EditText            mEditNotes;
    @BindView(R.id.layout_edit_title) TextInputLayout     mLayoutEditTitle;
    @BindView(R.id.layout_edit_due)        TextInputLayout     mLayoutEditDueDate;

    private Assignment  mAssignment;

    private long    mSelectedClassId = 1;
    private int     mSelectedType;
    private int     mDueDay;
    private int     mDueMonth;
    private int     mDueYear;

    public static ModifyAssignmentFragment newInstance(Assignment assignment) {
        ModifyAssignmentFragment fragment = new ModifyAssignmentFragment();
        fragment.mAssignment = assignment;
        return fragment;
    }

    @OnClick(R.id.btn_cancel)
    void dismissDialog() {
        dismiss();
    }

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_confirm)
    void editAssignment() {
        String title = mEditTitle.getText().toString().trim();
        String notes = mEditNotes.getText().toString().trim();
        String dueDate = mEditDueDate.getText().toString().trim();

        if (title.equals("") || mEditDueDate.getText().toString().trim().equals("")) {
            if (title.equals("")) {
                mLayoutEditTitle.setError("Title cannot be empty.");
            }
            if (dueDate.equals("")) {
                mLayoutEditDueDate.setError("Due date can't be empty");
            }
        } else {
            mAssignment.setTitle(title);
            mAssignment.setNotes(notes);
            mAssignment.setClassId((int) mSelectedClassId);
            switch (mSelectedType) {
                case 0:
                    mAssignment.setType(Assignment.TYPE_HOMEWORK);
                    break;
                case 1:
                    mAssignment.setType(Assignment.TYPE_TEST);
                    break;
                case 2:
                    mAssignment.setType(Assignment.TYPE_PROJECT);
                    break;
            }

            mAssignment.setDueDate(new DateTime(mDueYear, mDueMonth, mDueDay, 0, 0));

            new Thread(() -> PlannerDatabase.getInstance(getContext()).assignmentDao().updateAssignment(mAssignment)).start();
            dismiss();
        }
    }

    @OnClick(R.id.edit_due_date)
    void showDatePicker() {
        DatePickerFragment fragment = DatePickerFragment.newInstance(this, mDueYear, mDueMonth, mDueDay);

        if (getFragmentManager() != null) fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) getResources().getDimension(R.dimen.edit_dialog_width);
            int height = (int) getResources().getDimension(R.dimen.edit_dialog_height);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_assignment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProviders.of(this).get(ClassListViewModel.class).getClasses()
                .observe(this, this::setupClassSpinner);

    }

    private void setupInputListeners() {
        mEditTitle.addTextChangedListener(new AssignmentInfoInputHelper(mLayoutEditTitle));
        if (getView() != null) {
            getView().setOnTouchListener((v, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        KeyboardFocusManager.clearTextFocus(getContext(), v,
                                mEditDueDate, mEditNotes, mEditTitle);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                }
                return true;
            });
        }
    }

    private void setupTypeSpinner() {
        if (getContext() != null) {
            List<AssignmentType> types = new ArrayList<AssignmentType>() {{
                add(new AssignmentType("Homework", R.drawable.ic_homework_black_24dp));
                add(new AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp));
                add(new AssignmentType("Project", R.drawable.ic_group_black_24dp));
            }};

            AssignmentTypeAdapter typeAdapter = new AssignmentTypeAdapter(getContext(),
                    R.layout.item_assignment_type,
                    R.id.text_title,
                    types);
            mSpinnerType.setAdapter(typeAdapter);
            mSpinnerType.setOnItemSelectedListener(this);
        }
    }

    private void setupClassSpinner(List<Class> classes) {
        if (getContext() != null) {
            ArrayAdapter mClassAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1,
                    classes);
            mSpinnerClass.setAdapter(mClassAdapter);
            mSpinnerClass.setOnItemSelectedListener(this);

            mSpinnerClass.setSelection(Iterables.indexOf(classes,
                    c -> c.getId() == mAssignment.getClassId()));

            setupTypeSpinner();
            fillAssignmentInfo();
            setupInputListeners();
        }
    }

    private void fillAssignmentInfo() {
        DateTime dtDue = mAssignment.getDueDate();
        mEditTitle.setText(mAssignment.getTitle());
        mEditDueDate.setText(dtDue.toString("dd MMM, YYYY"));
        mEditNotes.setText(mAssignment.getNotes());

        mDueDay = dtDue.getDayOfMonth();
        mDueMonth = dtDue.getMonthOfYear();
        mDueYear = dtDue.getYear();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDueDay = dayOfMonth;
        mDueMonth = month + 1;
        mDueYear = year;

        DateTime dueDate = new DateTime(mDueYear, mDueMonth, mDueDay, 0, 0);

        mEditDueDate.setText(dueDate.toString(getString(R.string.due_date_pattern)));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(mSpinnerType)) mSelectedType = position;
        else if (parent.equals(mSpinnerClass)) {
            Class selected = (Class) parent.getSelectedItem();
            mSelectedClassId = selected.getId();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
