package io.eodc.planit.fragment;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import io.eodc.planit.db.PlannerDatabase;
import io.eodc.planit.model.ClassListViewModel;

/**
 * Fragment that is a bottom sheet. It is the main interface the user interacts with to add
 * assignments to their planner
 *
 * @author 2n
 */
public class AddAssignmentFragment extends BottomSheetDialogFragment implements
        AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener {

    @BindView(R.id.due_date_chooser)            EditText        mEditDueDate;
    @BindView(R.id.edit_assignment_name)        EditText        mEditTitle;
    @BindView(R.id.edit_notes)                  EditText        mEditNotes;
    @BindView(R.id.edit_layout_assignment_name) TextInputLayout mLayoutEditTitle;
    @BindView(R.id.edit_layout_due_date)        TextInputLayout mLayoutEditDueDate;
    @BindView(R.id.type_chooser)                Spinner         mTypeSpinner;
    @BindView(R.id.class_chooser)               Spinner         mClassSpinner;

    @SuppressLint("StaticFieldLeak")
    @OnClick(R.id.create_button) void addAssignment() {
        if (getView() != null) {
            String titleText = mEditTitle.getText().toString().trim();
            String dueDateText = mEditDueDate.getText().toString().trim();

            if (!titleText.equals("") && !dueDateText.equals("")) {
                Assignment newAssignment = new Assignment(titleText,
                        (int) mSelectedClass,
                        new DateTime(mDueYear, mDueMonth, mDueDay, 0, 0),
                        mEditNotes.getText().toString());


                switch (mSelectedType) {
                    case 0:
                        newAssignment.setType(Assignment.TYPE_HOMEWORK);
                        break;
                    case 1:
                        newAssignment.setType(Assignment.TYPE_TEST);
                        break;
                    case 2:
                        newAssignment.setType(Assignment.TYPE_PROJECT);
                        break;
                }

                new Thread(() -> PlannerDatabase.getInstance(getContext())
                        .assignmentDao()
                        .insertAssignments(newAssignment)).start();
                dismiss();
            } else {
                if (titleText.equals("")) {
                    mLayoutEditTitle.setError("Title can't be empty");
                }
                if (dueDateText.equals("")) {
                    mLayoutEditDueDate.setError("Due date can't be empty");
                }
            }
        }
    }

    @OnClick(R.id.due_date_chooser) void handleDueDateChooser() {
        if (getFragmentManager() != null) {
            DialogFragment datePicker = DatePickerFragment.newInstance(this);
            datePicker.show(getFragmentManager(), "datePicker");
        }
    }

    private long    mSelectedClass = 1;
    private int     mSelectedType;
    private int     mDueDay;
    private int     mDueMonth;
    private int     mDueYear;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new BottomSheetDialog(requireContext(), R.style.AppTheme_BottomSheet);
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            ViewModelProviders.of(this)
                    .get(ClassListViewModel.class).getClasses().observe(this, classes -> {
                if (classes != null) {
                    ArrayAdapter classAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item,
                            android.R.id.text1,
                            classes);
                    mClassSpinner.setAdapter(classAdapter);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_assignment, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            setupTypeSpinner();
            setupClassSpinner();
            setupInputListeners();
        }
    }

    private void setupInputListeners() {
        mEditTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    mLayoutEditTitle.setError("Title can't be empty");
                } else {
                    mLayoutEditTitle.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEditDueDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    mLayoutEditDueDate.setError("Due date can't be empty");
                } else {
                    mLayoutEditDueDate.setError("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (getView() != null) {
            getView().setOnTouchListener((v, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (getContext() != null) {
                            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (inputManager != null) {
                                inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                mEditTitle.clearFocus();
                                mEditDueDate.clearFocus();
                                mEditNotes.clearFocus();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                }
                return true;
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(mTypeSpinner)) mSelectedType = position;
        else if (parent.equals(mClassSpinner)) mSelectedClass = id + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Sets up the type spinner
     */
    private void setupTypeSpinner() {
        if (getContext() != null) {
            List<AssignmentType> types = new ArrayList<>();
            types.add(new AssignmentType("Homework", R.drawable.ic_homework_black_24dp));
            types.add(new AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp));
            types.add(new AssignmentType("Project", R.drawable.ic_group_black_24dp));
            AssignmentTypeAdapter typeAdapter = new AssignmentTypeAdapter(getContext(), R.layout.item_assignment_type, R.id.title, types);
            mTypeSpinner.setAdapter(typeAdapter);
            mTypeSpinner.setSelection(0);
            mTypeSpinner.setOnItemSelectedListener(this);
        }
    }

    private void setupClassSpinner() {
        mClassSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mDueYear = year;
        mDueMonth = month + 1;
        mDueDay = dayOfMonth;

        DateTime dueDate = new DateTime(mDueYear, mDueMonth, mDueDay, 0, 0);

        mEditDueDate.setText(dueDate.toString(getString(R.string.due_date_pattern)));
    }

}
