package io.eodc.planit.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import androidx.preference.*
import io.eodc.planit.R
import io.eodc.planit.activity.SubjectsActivity
import io.eodc.planit.helper.NotificationHelper
import io.eodc.planit.preference.DialogPreferenceContainer
import io.eodc.planit.preference.NotificationTimeChooserPreference

/**
 * Fragment showing all settings specified in the corresponding xml file
 *
 * @author 2n
 */
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_planner)

        val prefScreen = preferenceScreen
        val sharedPreferences = prefScreen.sharedPreferences
        val pCatCount = prefScreen.preferenceCount

        for (i in 0 until pCatCount) {
            val pCat = prefScreen.getPreference(i) as PreferenceCategory
            val count = pCat.preferenceCount
            for (j in 0 until count) {
                val p = pCat.getPreference(j)
                when (p) {
                    is ListPreference -> setPreferenceSummary(p, sharedPreferences.getString(p.getKey(), ""))
                    is DialogPreferenceContainer -> p.setSummary(parseTime(sharedPreferences.getString(p.getKey(), "")!!))
                    is MultiSelectListPreference -> {
                        val selectedDays = sharedPreferences.getStringSet(p.getKey(), null)
                        p.setSummary(getNotificationDays(selectedDays, p))
                    }
                }
            }
        }
        findPreference(getString(R.string.pref_classes_key)).onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val intent = Intent(context, SubjectsActivity::class.java)
            startActivity(intent)
            true
        }
    }

    /**
     * Utility method setting the preference summary of [ListPreference]
     *
     * @param pref The preference to set the summary
     * @param val  The value of the preference
     */
    private fun setPreferenceSummary(pref: Preference, `val`: String?) {
        if (pref is ListPreference) {
            val prefIndex = pref.findIndexOfValue(`val`)
            if (prefIndex >= 0)
                pref.setSummary(pref.entries[prefIndex])
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val pref = findPreference(key)
        val helper = NotificationHelper(context!!)
        if (pref is SwitchPreference) {
            if (pref.isChecked)
                helper.scheduleNotification()
            else
                helper.cancelNotification()
        } else if (pref is ListPreference) {
            pref.setSummary(pref.entry)
        } else if (pref is DialogPreferenceContainer) {
            val time = sharedPreferences.getString(getString(R.string.pref_show_notif_time_key), "")
            pref.setSummary(parseTime(time!!))
            helper.scheduleNotification()
        } else if (pref is MultiSelectListPreference) {
            val selectedDays = sharedPreferences.getStringSet(pref.getKey(), null)
            pref.setSummary(getNotificationDays(selectedDays, pref))
            helper.scheduleNotification()
        }
    }

    /**
     * Utility method to parse time
     *
     * @param time The time to parse
     * @return A formatted string of this time
     */
    private fun parseTime(time: String): String {
        try {
            val timeParts = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var hour = Integer.parseInt(timeParts[0])
            val min = Integer.parseInt(timeParts[1])
            return if (!DateFormat.is24HourFormat(context)) {
                var isAm = true
                if (hour > 12) {
                    hour -= 12
                    isAm = false
                }
                hour.toString() + ":" + (if (min < 10) "0$min" else min) + if (isAm) "AM" else "PM"
            } else
                hour.toString() + ":" + if (min < 10) "0$min" else min
        } catch (e: NumberFormatException) {
            return "Not set."
        }

    }

    /**
     * Utility method to format user selected days to a human readable format
     *
     * @param selectedDays A set of values for selected days
     * @param pref         The preference with the selected days
     * @return A formatted string
     */
    private fun getNotificationDays(selectedDays: Set<String>?, pref: MultiSelectListPreference): String {
        if (selectedDays != null) {
            if (selectedDays.size < 7) {
                val sb = StringBuilder()
                val entries = pref.entries
                for (entry in entries) {
                    val it = selectedDays.iterator()
                    while (it.hasNext()) {
                        val value = it.next()
                        if (entries[pref.findIndexOfValue(value)] == entry) {
                            sb.append(entry.subSequence(0, 3))
                            if (it.hasNext()) sb.append(", ")
                        }
                    }
                }
                return sb.toString()
            } else
                return "Every Day"
        } else
            return ""
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceContainer) {
            val fragment = NotificationTimeChooserPreference.newInstance(preference.getKey())
            fragment.setTargetFragment(this, 0)
            if (fragmentManager != null) fragment.show(fragmentManager!!, null)
        } else
            super.onDisplayPreferenceDialog(preference)
    }
}
