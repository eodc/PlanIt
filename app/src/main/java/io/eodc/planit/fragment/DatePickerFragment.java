package io.eodc.planit.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/**
 * Fragment that shows a {@link DatePickerDialog} and interfaces it with an
 * {@link android.app.DatePickerDialog.OnDateSetListener}
 *
 * @author 2n
 */
public class DatePickerFragment extends DialogFragment {
    private int year;
    private int month;
    private int day;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    /**
     * Creates an instance of DatePickerFragment, automatically showing the current day.
     *
     * @param listener The listener listening for date selections
     * @return A new instance of DatePickerFragment
     */
    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setOnDateSetListener(listener);

        return fragment;
    }

    /**
     * Creates an instance of DatePickerFragment, showing the specified day
     *
     * @param listener The listener listening for date selections
     * @param year     The year to show
     * @param month    The month to show, 0-indexed (January is 0, December is 11)
     * @param day      THe day to show
     * @return A new instance of DatePickerFragment
     */
    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener, int year,
                                                 int month,
                                                 int day) {
        DatePickerFragment fragment = newInstance(listener);
        fragment.setDate(year, month, day);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        if (year == 0 && month == 0 && day == 0) {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH) + 1;
        }

        return new DatePickerDialog(requireActivity(), dateSetListener, year, month - 1, day);
    }

    /**
     * Sets the date listener
     */
    private void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        dateSetListener = listener;
    }

    /**
     * Sets the shown date on the dialog
     *
     * @param year  The year to show
     * @param month The month to show, 0-indexed (January is 0, December is 11)
     * @param day   The day to show
     */
    private void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}
