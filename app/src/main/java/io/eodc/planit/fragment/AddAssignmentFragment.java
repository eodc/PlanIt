package io.eodc.planit.fragment;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.adapter.AssignmentType;
import io.eodc.planit.adapter.AssignmentTypeAdapter;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.listener.OnAssignmentChangeListener;

/**
 * Fragment that is a bottom sheet. It is the main interface the user interacts with to add
 * assignments to their planner
 *
 * @author 2n
 */
public class AddAssignmentFragment extends Fragment implements
        AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.due_date_chooser)
    EditText editDueDate;
    @BindView(R.id.type_chooser)
    Spinner typeSpinner;
    @BindView(R.id.class_chooser)
    Spinner classSpinner;
    private Context mContext;
    private OnAssignmentChangeListener assignmentCreationListener;
    private SimpleCursorAdapter classAdapter;
    private int selectedType = 0;
    private long selectedClass = 1;
    private int dueDay = 0;
    private int dueMonth = 0;
    private int dueYear = 0;

    /**
     * Creates a new instance of this AddAssignmentFragment
     *
     * @param listener The listener listening for assignment changes
     * @return A new instance of AddAssignmentFragment
     */
    public static AddAssignmentFragment newInstance(OnAssignmentChangeListener listener) {
        Bundle args = new Bundle();

        AddAssignmentFragment fragment = new AddAssignmentFragment();
        fragment.setAssignmentCreationListener(listener);
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

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    /**
     * Attaches the specified listener to this fragment
     */
    private void setAssignmentCreationListener(OnAssignmentChangeListener listener) {
        assignmentCreationListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        classAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                null, new String[]{PlannerContract.ClassColumns.NAME},
                new int[]{android.R.id.text1}, 0);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_assignment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            mContext = getActivity().getApplicationContext();

            setupTypeSpinner();
            setupCreateButton(view);
        }
    }

    /**
     * Sets up the create button
     *
     * @param view The root view of the fragment
     */
    private void setupCreateButton(final View view) {
        Button creationButton = view.findViewById(R.id.create_button);
        creationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextInputLayout editTitleLayout = view.findViewById(R.id.edit_layout_assignment_name);
                TextInputLayout editDueDateLayout = view.findViewById(R.id.edit_layout_due_date);
                TextInputEditText editTitle = view.findViewById(R.id.edit_assignment_name);
                TextInputEditText editNotes = view.findViewById(R.id.edit_notes);

                String titleText = editTitle.getText().toString().trim();
                String dueDateText = editDueDate.getText().toString().trim();

                if (!titleText.equals("") && !dueDateText.equals("")) {
                    ContentResolver provider = mContext.getContentResolver();
                    ContentValues values = new ContentValues();

                    values.put(PlannerContract.AssignmentColumns.TITLE, titleText);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    String dueDate = sdf.format(new DateTime(dueYear, dueMonth, dueDay, 0, 0).toDate());

                    values.put(PlannerContract.AssignmentColumns.DUE_DATE, dueDate);
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
                    values.put(PlannerContract.AssignmentColumns.CLASS_ID, selectedClass);
                    values.put(PlannerContract.AssignmentColumns.COMPLETED, false);
                    values.put(PlannerContract.AssignmentColumns.NOTES, editNotes.getText().toString());
                    provider.insert(PlannerContract.AssignmentColumns.CONTENT_URI, values);
                    assignmentCreationListener.onAssignmentCreation();
                } else {
                    if (titleText.equals(""))
                        editTitleLayout.setError("Title can't be empty");
                    if (dueDateText.equals(""))
                        editDueDateLayout.setError("Due date can't be empty");
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.equals(typeSpinner)) {
            selectedType = position;
        } else if (parent.equals(classSpinner)) {
            selectedClass = id;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /**
     * Sets up the type spinner
     */
    private void setupTypeSpinner() {
        List<AssignmentType> types = new ArrayList<>();
        types.add(new AssignmentType("Homework", R.drawable.ic_homework_black_24dp));
        types.add(new AssignmentType("Quiz/Test", R.drawable.ic_test_black_24dp));
        types.add(new AssignmentType("Project", R.drawable.ic_group_black_24dp));
        AssignmentTypeAdapter typeAdapter = new AssignmentTypeAdapter(mContext, R.layout.item_assignment_type, R.id.title, types);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Setups the class spinner
     *
     * @param data A cursor containing all classes
     */
    private void setupClassSpinner(Cursor data) {
        classAdapter.swapCursor(data);
        classSpinner.setAdapter(classAdapter);
        classSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dueYear = year;
        dueMonth = month + 1;
        dueDay = dayOfMonth;
        editDueDate.setText(getString(R.string.date_format, dueDay, dueMonth, dueYear));
    }

}
