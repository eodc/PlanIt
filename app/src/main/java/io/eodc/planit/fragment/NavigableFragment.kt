package io.eodc.planit.fragment

import androidx.fragment.app.Fragment

import io.eodc.planit.activity.MainActivity
import io.eodc.planit.db.Subject

open class NavigableFragment : androidx.fragment.app.Fragment() {
    internal val subjects: List<Subject>?
        get() {
            if (activity != null && activity is MainActivity) {
                val subjects = (activity as MainActivity).classes
                return subjects ?: subjects
            }
            return null
        }
}
