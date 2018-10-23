package io.eodc.planit.helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyboardFocusManager {
    fun clearTextFocus(context: Context?,
                       v: View,
                       vararg editTexts: EditText) {
        if (context != null) {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(v.windowToken, 0)
            for (editText in editTexts) {
                editText.clearFocus()
            }
        }
    }
}
