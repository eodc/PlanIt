package io.eodc.planit.fragment;

import android.support.v4.app.Fragment;

import java.util.List;

import io.eodc.planit.activity.MainActivity;
import io.eodc.planit.db.Subject;

public class NavigableFragment extends Fragment {
    List<Subject> getSubjects() {
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            List<Subject> subjects = ((MainActivity) getActivity()).getClasses();
            if (subjects != null) {
                return subjects;
            } else {
                return getSubjects();
            }
        }
        return null;
    }
}
