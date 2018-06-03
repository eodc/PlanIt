package io.eodc.planit.fragment;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.thebluealliance.spectrum.SpectrumDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.eodc.planit.R;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.listener.OnClassListChangeListener;

/**
 * Fragment that shows a dialog prompting the user to either add or modify a class
 *
 * @author 2n
 */
public class ModifyClassFragment extends DialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SpectrumDialog.OnColorSelectedListener {
    public static final int FLAG_NEW_CLASS = 0;
    public static final int FLAG_MOD_CLASS = 1;

    private static final String ARG_FLAG = "flag";
    private static final String ARG_ID = "id";

    private int flag;

    private String colorChosen;
    private OnClassListChangeListener listener;

    @BindView(R.id.tv_title)            TextView    tvTitle;
    @BindView(R.id.tv_subtitle)         TextView    tvSubtitle;
    @BindView(R.id.edit_class_name)     EditText    editClassName;
    @BindView(R.id.edit_teacher_name)   EditText    editTeacherName;
    @BindView(R.id.color_picker)        ImageView   colorPicker;
    @BindView(R.id.btn_confirm)      Button      btnConfirm;
    @BindView(R.id.btn_delete)          Button      btnDelete;

    /**
     * Shows the color picker dialog when the color picker button is clicked
     */
    @OnClick(R.id.color_picker) void showColorPicker() {
        if (getFragmentManager() != null)
            new SpectrumDialog.Builder(getContext())
                    .setColors(R.array.spectrum_colors)
                    .setSelectedColorRes(R.color.class_red)
                    .setDismissOnColorSelected(false)
                    .setOnColorSelectedListener(this)
                    .build().show(getFragmentManager(), null);
    }

    /**
     * Dismisses this dialog when the cancel button is clicked
     */
    @OnClick(R.id.btn_cancel) void dismissDialog() { dismiss(); }

    /**
     * Shows the delete dialog when the delete button is pressed
     */
    @OnClick(R.id.btn_delete) void showDeleteDialog() {
        if (getActivity() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Class?")
                    .setMessage("Deleting this class will also remove all related assignments from your planner!")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = getArguments();
                            if (args != null) {
                                int id = args.getInt(ARG_ID);
                                getActivity().getContentResolver().delete(PlannerContract.ClassColumns.CONTENT_URI,
                                        PlannerContract.ClassColumns._ID + "=?",
                                        new String[]{String.valueOf(id)});
                                getActivity().getContentResolver().delete(PlannerContract.AssignmentColumns.CONTENT_URI,
                                        PlannerContract.AssignmentColumns.CLASS_ID + "=?",
                                        new String[]{String.valueOf(id)});
                            }
                            Cursor cursor = getActivity().getContentResolver().query(PlannerContract.ClassColumns.CONTENT_URI,
                                    null, null, null, null);
                            if (cursor != null) {
                                listener.onClassListChange(cursor.getCount());
                                cursor.close();
                            }
                            dismiss();
                        }
                    })
                    .show();
        }
    }

    /**
     * Confirms this dialog's action, whether its editing a class or adding one.
     */
    @OnClick(R.id.btn_confirm) void setBtnConfirm() {
        ContentValues values = new ContentValues();

        String className = editClassName.getText().toString().trim();
        String teacherName = editTeacherName.getText().toString().trim();

        if (!className.equals("") &&
                !teacherName.equals("") &&
                getActivity() != null &&
                getActivity().getContentResolver() != null) {
              values.put(PlannerContract.ClassColumns.NAME, className);
              values.put(PlannerContract.ClassColumns.TEACHER, teacherName);
              values.put(PlannerContract.ClassColumns.COLOR, colorChosen);
              if (flag == FLAG_NEW_CLASS)
                getActivity().getContentResolver().insert(PlannerContract.ClassColumns.CONTENT_URI, values);
              else if (flag == FLAG_MOD_CLASS) {
                  Bundle args = getArguments();
                  if (args != null) {
                      int id = args.getInt(ARG_ID);
                      getActivity().getContentResolver().update(PlannerContract.ClassColumns.CONTENT_URI,
                              values, PlannerContract.ClassColumns._ID + "=?",
                              new String[] { String.valueOf(id) });
                  }
              }
              Cursor cursor = getActivity().getContentResolver().query(PlannerContract.ClassColumns.CONTENT_URI,
                    null, null, null, null);
              if (cursor != null) {
                  listener.onClassListChange(cursor.getCount());
                  cursor.close();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            flag = args.getInt(ARG_FLAG);
            if (flag == FLAG_MOD_CLASS) {
                int id = args.getInt(ARG_ID);
                getLoaderManager().initLoader(id, null, this);
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
        if (flag == FLAG_NEW_CLASS) {
            tvTitle.setText(R.string.create_class_label);
            tvSubtitle.setText(R.string.create_class_sublabel);
            btnConfirm.setText(R.string.btn_create);
        } else if (flag == FLAG_MOD_CLASS) {
            btnDelete.setVisibility(View.VISIBLE);
        }
        colorChosen = "#" + ContextCompat.getColor(requireContext(), R.color.class_red);
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

    /**
     * Creates an new instance of a ModifyClassFragment
     *
     * @param listener The listener listening for class list changes
     * @param flag Specifies whether a new class is being added or an existing one is being modified
     * @param id If being modified, the row number in the class table. If being added use 0
     * @return A new instance of ModifyClassFragment
     *
     * @see #FLAG_NEW_CLASS
     * @see #FLAG_MOD_CLASS
     */
    public static ModifyClassFragment newInstance(OnClassListChangeListener listener, int flag, int id) {
        Bundle args = new Bundle();
        args.putInt(ARG_FLAG, flag);
        args.putInt(ARG_ID, id);

        ModifyClassFragment fragment = new ModifyClassFragment();
        fragment.swapListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Swaps the current listener with the specified one
     */
    public void swapListener(OnClassListChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(requireContext(), PlannerContract.ClassColumns.CONTENT_URI, null,
                PlannerContract.ClassColumns._ID + "=?", new String[] {String.valueOf(id)}, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        editClassName.setText(data.getString(data.getColumnIndex(PlannerContract.ClassColumns.NAME)));
        editTeacherName.setText(data.getString(data.getColumnIndex(PlannerContract.ClassColumns.TEACHER)));
        colorPicker.setImageDrawable(new ColorDrawable(Color.parseColor(data.getString(data.getColumnIndex(PlannerContract.ClassColumns.COLOR)))));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { }

    @Override
    public void onColorSelected(boolean positiveResult, int color) {
        if (positiveResult) {
            colorPicker.setImageDrawable(new ColorDrawable(color));
            colorChosen = "#" + Integer.toHexString(color).toUpperCase();
        }
    }
}
