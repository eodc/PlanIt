package io.eodc.planit.preference;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.joda.time.DateTime;

/**
 * A preference that shows a TimePicker dialog
 *
 * @author 2n
 */
public class NotificationTimeChooserPreference extends PreferenceDialogFragmentCompat implements
        TimePickerDialog.OnTimeSetListener {
    private static final String ARG_KEY = "key";

    private String key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            key = args.getString(ARG_KEY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateTime dt = new DateTime();
        return new TimePickerDialog(getContext(), this, dt.getHourOfDay(), dt.getMinuteOfHour(), DateFormat.is24HourFormat(getContext()));
    }

    /**
     * Creates a new instance of a NotificationTimeChooserPreference
     *
     * @param key The key of the preference
     * @return A new instance of NotificationTimeChooserPreference
     */
    public static NotificationTimeChooserPreference newInstance(String key) {
        NotificationTimeChooserPreference fragment = new NotificationTimeChooserPreference();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) { }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String time = hourOfDay + ":" + (minute < 10 ? "0" + minute : minute);
        getPreference().getSharedPreferences().edit()
                .putString(key, time)
                .apply();
    }
}
