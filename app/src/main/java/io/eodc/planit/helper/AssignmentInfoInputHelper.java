package io.eodc.planit.helper;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;

public class AssignmentInfoInputHelper implements TextWatcher {
    private TextInputLayout mLayoutEditTitle;

    public AssignmentInfoInputHelper(TextInputLayout layoutEditTitle) {
        this.mLayoutEditTitle = layoutEditTitle;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (s.toString().equals("")) {
            mLayoutEditTitle.setError("Title can't be empty");
        } else {
            mLayoutEditTitle.setError("");
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


}
