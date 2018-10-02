package io.eodc.planit.helper;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardFocusManager {
    public static void clearTextFocus(Context context,
                                      View v,
                                      EditText... editTexts) {
        if (context != null) {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                for (EditText editText : editTexts) {
                    editText.clearFocus();
                }
            }
        }
    }
}
