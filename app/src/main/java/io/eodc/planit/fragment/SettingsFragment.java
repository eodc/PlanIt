package io.eodc.planit.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateFormat;

import java.util.Iterator;
import java.util.Set;

import io.eodc.planit.R;
import io.eodc.planit.activity.SubjectsActivity;
import io.eodc.planit.helper.NotificationHelper;
import io.eodc.planit.preference.DialogPreferenceContainer;
import io.eodc.planit.preference.NotificationTimeChooserPreference;

/**
 * Fragment showing all settings specified in the corresponding xml file
 *
 * @author 2n
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_planner);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();
        int pCatCount = prefScreen.getPreferenceCount();

        for (int i = 0; i < pCatCount; ++i) {
            PreferenceCategory pCat = (PreferenceCategory) prefScreen.getPreference(i);
            int count = pCat.getPreferenceCount();
            for (int j = 0; j < count; ++j) {
                Preference p = pCat.getPreference(j);
                if (p instanceof ListPreference) {
                    setPreferenceSummary(p, sharedPreferences.getString(p.getKey(), ""));
                } else if (p instanceof DialogPreferenceContainer) {
                    p.setSummary(parseTime(sharedPreferences.getString(p.getKey(), "")));
                } else if (p instanceof MultiSelectListPreference) {
                    Set<String> selectedDays = sharedPreferences.getStringSet(p.getKey(), null);
                    p.setSummary(getNotificationDays(selectedDays, (MultiSelectListPreference) p));
                }
            }
        }
        findPreference(getString(R.string.pref_classes_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), SubjectsActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    /**
     * Utility method setting the preference summary of {@link ListPreference}
     *
     * @param pref The preference to set the summary
     * @param val  The value of the preference
     */
    private void setPreferenceSummary(Preference pref, String val) {
        if (pref instanceof ListPreference) {
            ListPreference lp = (ListPreference) pref;
            int prefIndex = lp.findIndexOfValue(val);
            if (prefIndex >= 0)
                pref.setSummary(lp.getEntries()[prefIndex]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        NotificationHelper helper = new NotificationHelper(getContext());
        if (pref instanceof SwitchPreference) {
            if (((SwitchPreference) pref).isChecked()) helper.scheduleNotification();
            else helper.cancelNotification();
        } else if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        } else if (pref instanceof DialogPreferenceContainer) {
            String time = sharedPreferences.getString(getString(R.string.pref_show_notif_time_key), "");
            pref.setSummary(parseTime(time));
            helper.scheduleNotification();
        } else if (pref instanceof MultiSelectListPreference) {
            Set<String> selectedDays = sharedPreferences.getStringSet(pref.getKey(), null);
            pref.setSummary(getNotificationDays(selectedDays, (MultiSelectListPreference) pref));
            helper.scheduleNotification();
        }
    }

    /**
     * Utility method to parse time
     *
     * @param time The time to parse
     * @return A formatted string of this time
     */
    private String parseTime(String time) {
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int min = Integer.parseInt(timeParts[1]);
            if (!DateFormat.is24HourFormat(getContext())) {
                boolean isAm = true;
                if (hour > 12) {
                    hour -= 12;
                    isAm = false;
                }
                return hour + ":" + (min < 10 ? "0" + min : min) + (isAm ? "AM" : "PM");
            } else return hour + ":" + (min < 10 ? "0" + min : min);
        } catch (NumberFormatException e) {
            return "Not set.";
        }
    }

    /**
     * Utility method to format user selected days to a human readable format
     *
     * @param selectedDays A set of values for selected days
     * @param pref         The preference with the selected days
     * @return A formatted string
     */
    private String getNotificationDays(Set<String> selectedDays, MultiSelectListPreference pref) {
        if (selectedDays != null) {
            if (selectedDays.size() < 7) {
                StringBuilder sb = new StringBuilder();
                CharSequence[] entries = pref.getEntries();
                for (CharSequence entry : entries) {
                    Iterator<String> it = selectedDays.iterator();
                    while (it.hasNext()) {
                        String value = it.next();
                        if (entries[pref.findIndexOfValue(value)].equals(entry)) {
                            sb.append(entry.subSequence(0, 3));
                            if (it.hasNext()) sb.append(", ");
                        }
                    }
                }
                return sb.toString();
            } else return "Every Day";
        } else return "";
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof DialogPreferenceContainer) {
            NotificationTimeChooserPreference fragment = NotificationTimeChooserPreference.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            if (getFragmentManager() != null) fragment.show(getFragmentManager(), null);
        } else super.onDisplayPreferenceDialog(preference);
    }
}
