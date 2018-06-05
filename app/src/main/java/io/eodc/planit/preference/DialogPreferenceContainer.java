package io.eodc.planit.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * A container for preference dialogs. All constructors are for the superclass, I honestly don't know
 * wtf they do
 */
public class DialogPreferenceContainer extends DialogPreference {
    public DialogPreferenceContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DialogPreferenceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DialogPreferenceContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DialogPreferenceContainer(Context context) {
        super(context);
    }
}
