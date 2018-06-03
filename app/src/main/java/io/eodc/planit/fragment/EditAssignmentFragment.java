package io.eodc.planit.fragment;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentType;
import io.eodc.planit.adapter.AssignmentTypeAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.listener.OnAssignmentChangeListener;

/**
 * Created by 2n on 5/16/18.
 */

public class EditAssignmentFragment extends DialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemSelectedListener {

    private static final String ID_KEY = "id";

    private OnAssignmentChangeListener listener;
    private SimpleCursorAdapter classAdapter;
    private Context mContext;

    private int id;
    private Cursor assignmentCursor;

    private int selectedType = 0;
    private long selectedClassId = 1;
    private int dueDay;
    private int dueMonth;
    private int dueYear;

    @BindView(R.id.btn_restore)                 Button btnRestore;
    @BindView(R.id.type_chooser)                Spinner typeSpinner;
    @BindView(R.id.class_chooser)               Spinner classSpinner;
    @BindView(R.id.edit_assignment_name)        EditText editTitle;
    @BindView(R.id.edit_due_date)               EditText editDue;
    @BindView(R.id.edit_notes)                  EditText editNotes;
    @BindView(R.id.edit_layout_assignment_name) TextInputLayout editNameLayout;
    @BindView(R.id.edit_layout_due_date)        TextInputLayout editDueLayout;

    @OnClick(R.id.btn_cancel) void dismissDialog() { dismiss(); }

    @OnClick(R.id.btn_edit) void editAssignment() {
        ContentValues values = new ContentValues();

        String name = editTitle.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        if (name.equals("") || editDue.getText().toString().trim().equals("")) {
            if (name.equals(""))
                editNameLayout.setError("Title cannot be empty.");
            if (editDue.getText().toString().trim().equals(""))
                editDueLayout.setError("Due date can't be empty");
        } else {
            values.put(PlannerContract.AssignmentColumns.TITLE, name);
            values.put(PlannerContract.AssignmentColumns.CLASS_ID, selectedClassId);
            switch (selectedType) {
                case 0:
                    values.put(PlannerContract.AssignmentColumns.TYPE, PlannerContract.TYPE_HOMEWORK);
                    break;
                case 1:
                    values.put(PlannerContract.AssignmentColumns.TYPE, PlannerContract.TYPE_TEST);
                    break;
                case 2:
                    values.put(PlannerContract.AssignmentColumns.TYPE, PlannerContract.TYPE_PROJECT);
                    break;
            }


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String dueDate = sdf.format(new DateTime(dueYear, dueMonth, dueDay, 0, 0).toDate());

            values.put(PlannerContract.AssignmentColumns.DUE_DATE, dueDate);
            values.put(PlannerContract.AssignmentColumns.NOTES, notes);

            if (getActivity() != null) {
                ContentResolver resolver = getActivity().getContentResolver();
                resolver.update(PlannerContract.AssignmentColumns.CONTENT_URI,
                        values, PlannerContract.AssignmentColumns._ID + "=?",
                        new String[]{String.valueOf(id)});

                listener.onAssignmentEdit();
            }

            dismiss();
        }
    }

    @OnClick(R.id.edit_due_date) void showDatePicker() {
        DatePickerFragment fragment = DatePickerFragment.newInstance(this, dueYear, dueMonth, dueDay);

        if (getFragmentManager() != null) fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.btn_restore) void restoreAssignment() {
        if (getContext() != null) {
            ContentValues values = new ContentValues();
            values.put(PlannerContract.AssignmentColumns.COMPLETED, false);
            getContext().getContentResolver().update(PlannerContract.AssignmentColumns.CONTENT_URI,
                    values, PlannerContract.AssignmentColumns._ID + "=?",
                    new String[]{String.valueOf(id)});
            listener.onAssignmentEdit();
        }

        dismiss();
    }

    public EditAssignmentFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();

        if (args != null) {
            id = args.getInt(ID_KEY);

            getLoaderManager().initLoader(0, null, this);

            mContext = requireContext();

            classAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                    null, new String[] { PlannerContract.ClassColumns.NAME },
                    new int[] { android.R.id.text1 }, 0);
        }
    }

    public void setListener(OnAssignmentChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null &&
                getDialog().getWindow() != null) {
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
        setupTypeSpinner();
        fillAssignmentInfo();
    }

    private void setupTypeSpinner() {
        List<AssignmentType> types = new ArrayList<>();
        types.add(new AssignmentType("Homework", R.drawable.ic_homework_black_24dp));
        types.add(new AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp));
        types.add(new AssignmentType("Project", R.drawable.ic_group_black_24dp));
        AssignmentTypeAdapter typeAdapter = new AssignmentTypeAdapter(mContext, R.layout.item_assignment_type, R.id.title, types);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(this);
    }

    private void setupClassSpinner(Cursor data) {
        classAdapter.swapCursor(data);
        classSpinner.setAdapter(classAdapter);
        classSpinner.setOnItemSelectedListener(this);
        classSpinner.setSelection(assignmentCursor.getInt(assignmentCursor.getColumnIndex(PlannerContract.AssignmentColumns.CLASS_ID)) - 1);
    }

    public static EditAssignmentFragment newInstance(int id, OnAssignmentChangeListener listener) {
        Bundle args = new Bundle();
        EditAssignmentFragment fragment = new EditAssignmentFragment();
        fragment.setListener(listener);

        args.putInt(ID_KEY, id);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[]{
                PlannerContract.ClassColumns._ID,
                PlannerContract.ClassColumns.NAME};
        return new CursorLoader(requireContext(), PlannerContract.ClassColumns.CONTENT_URI,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        setupClassSpinner(data);
    }

    private void fillAssignmentInfo() {
        assignmentCursor = mContext.getContentResolver().query(PlannerContract.AssignmentColumns.CONTENT_URI,
                null, PlannerContract.AssignmentColumns._ID + "=?", new String[] { String.valueOf(id) },
                null);
        if (assignmentCursor != null) {
            assignmentCursor.moveToFirst();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            try {
                DateTime dtDue = new DateTime(sdf.parse(assignmentCursor.getString(assignmentCursor.getColumnIndex(PlannerContract.AssignmentColumns.DUE_DATE))));
                editTitle.setText(assignmentCursor.getString(
                        assignmentCursor.getColumnIndex(PlannerContract.AssignmentColumns.TITLE)));
                editDue.setText(getString(R.string.date_format, dtDue.getDayOfMonth(),
                        dtDue.getMonthOfYear(),
                        dtDue.getYear()));
                editNotes.setText(assignmentCursor.getString(assignmentCursor.getColumnIndex(PlannerContract.AssignmentColumns.NOTES)));

                if (assignmentCursor.getInt(assignmentCursor.getColumnIndex(PlannerContract.AssignmentColumns.COMPLETED)) != 0) {
                    btnRestore.setVisibility(View.VISIBLE);
                }

                dueDay = dtDue.getDayOfMonth();
                dueMonth = dtDue.getMonthOfYear();
                dueYear = dtDue.getYear();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        assignmentCursor.close();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dueDay = dayOfMonth;
        dueMonth = month + 1;
        dueYear = year;

        editDue.setText(getString(R.string.date_format, dueDay, dueMonth, dueYear));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(typeSpinner)) {
            selectedType = position;
        } else if (parent.equals(classSpinner)) {
            selectedClassId = id;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
