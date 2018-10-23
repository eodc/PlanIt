package io.eodc.planit.preference

import android.content.Context
import androidx.preference.DialogPreference
import android.util.AttributeSet

/**
 * A container for preference dialogs. All constructors are for the superclass, I honestly don't know
 * wtf they do
 */
class DialogPreferenceContainer : DialogPreference {
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context) : super(context)
}
