package io.eodc.planit.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

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
import io.eodc.planit.model.ClassListViewModel;

/**
 * Created by 2n on 5/16/18.
 */

public class EditAssignmentFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.btn_restore)                 Button              mBtnRestore;
    @BindView(R.id.type_chooser)                Spinner             mSpinnerType;
    @BindView(R.id.class_chooser)               Spinner             mSpinnerClass;
    @BindView(R.id.edit_assignment_name)        EditText            mEditTitle;
    @BindView(R.id.edit_due_date)               EditText            mEditDue;
    @BindView(R.id.edit_notes)                  EditText            mEditNotes;
    @BindView(R.id.edit_layout_assignment_name) TextInputLayout     mEditNameLayout;
    @BindView(R.id.edit_layout_due_date)        TextInputLayout     mEditDueLayout;

    private Assignment  mAssignment;

    private long    mSelectedClassId = 1;
    private int     mSelectedType;
    private int     mDueDay;
    private int     mDueMonth;
    private int     mDueYear;

    public static EditAssignmentFragment newInstance(Assignment assignment) {
        EditAssignmentFragment fragment = new EditAssignmentFragment();
        fragment.mAssignment = assignment;
        return fragment;
    }

    @OnClick(R.id.btn_cancel)
    void dismissDialog() {
        dismiss();
    }

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.btn_edit)
    void editAssignment() {
        String title = mEditTitle.getText().toString().trim();
        String notes = mEditNotes.getText().toString().trim();
        String dueDate = mEditDue.getText().toString().trim();

        if (title.equals("") || mEditDue.getText().toString().trim().equals("")) {
            if (title.equals("")) {
                mEditNameLayout.setError("Title cannot be empty.");
            }
            if (dueDate.equals("")) {
                mEditDueLayout.setError("Due date can't be empty");
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

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    PlannerDatabase.getInstance(getContext()).assignmentDao().updateAssignment(mAssignment);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);

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

        setupTypeSpinner();
        fillAssignmentInfo();
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
                    R.id.title,
                    types);
            mSpinnerType.setAdapter(typeAdapter);
            mSpinnerType.setOnItemSelectedListener(this);
        }
    }

    private void setupClassSpinner(List<Class> classes) {
        if (getContext() != null) {
            ArrayAdapter mClassAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item,
                    android.R.id.text1, classes);
            mSpinnerClass.setAdapter(mClassAdapter);
            mSpinnerClass.setOnItemSelectedListener(this);
        }
    }

    private void fillAssignmentInfo() {
        DateTime dtDue = mAssignment.getDueDate();
        mEditTitle.setText(mAssignment.getTitle());
        mEditDue.setText(getString(R.string.date_format,
                dtDue.getDayOfMonth(),
                dtDue.getMonthOfYear(),
                dtDue.getYear()));
        mEditNotes.setText(mAssignment.getNotes());

        mSpinnerClass.setSelection(mAssignment.getClassId() - 1);

        mDueDay = dtDue.getDayOfMonth();
        mDueMonth = dtDue.getMonthOfYear();
        mDueYear = dtDue.getYear();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDueDay = dayOfMonth;
        mDueMonth = month + 1;
        mDueYear = year;

        mEditDue.setText(getString(R.string.date_format, mDueDay, mDueMonth, mDueYear));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(mSpinnerType)) mSelectedType = position;
        else if (parent.equals(mSpinnerClass)) mSelectedClassId = id + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
