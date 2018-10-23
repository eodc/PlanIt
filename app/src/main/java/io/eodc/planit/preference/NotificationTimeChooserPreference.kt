package io.eodc.planit.preference

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.text.format.DateFormat
import android.widget.TimePicker

import org.joda.time.DateTime

/**
 * A preference that shows a TimePicker dialog
 *
 * @author 2n
 */
class NotificationTimeChooserPreference : PreferenceDialogFragmentCompat(), TimePickerDialog.OnTimeSetListener {

    private var key: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) key = args.getString(ARG_KEY)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dt = DateTime()
        return TimePickerDialog(context, this, dt.hourOfDay, dt.minuteOfHour, DateFormat.is24HourFormat(context))
    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        val time = hourOfDay.toString() + ":" + if (minute < 10) "0$minute" else minute
        preference.sharedPreferences.edit()
                .putString(key, time)
                .apply()
    }

    companion object {
        private const val ARG_KEY = "key"

        /**
         * Creates a new instance of a NotificationTimeChooserPreference
         *
         * @param key The key of the preference
         * @return A new instance of NotificationTimeChooserPreference
         */
        fun newInstance(key: String): NotificationTimeChooserPreference {
            val fragment = NotificationTimeChooserPreference()
            val args = Bundle()
            args.putString(ARG_KEY, key)
            fragment.arguments = args
            return fragment
        }
    }
}
