package io.eodc.planit.helper

import com.google.android.material.textfield.TextInputLayout
import android.text.Editable
import android.text.TextWatcher

class AssignmentInfoInputHelper(private val mLayoutEditTitle: TextInputLayout) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        if (s.toString() == "") {
            mLayoutEditTitle.error = "Title can't be empty"
        } else {
            mLayoutEditTitle.error = ""
        }
    }

    override fun afterTextChanged(s: Editable) {}
}
