package io.eodc.planit.adapter;

import android.support.annotation.DrawableRes;

/**
 * Represents an Assignment Type for use in a Spinner
 *
 * @author 2n
 * @see AssignmentTypeAdapter
 */
public class AssignmentType {
    private String name;
    private int iconId;

    /**
     * Constructs a new instance of an Assignment Type
     *
     * @param name   The name of the type
     * @param iconId The drawable resource id to be used as an icon in the view
     */
    public AssignmentType(String name, @DrawableRes int iconId) {
        this.name = name;
        this.iconId = iconId;
    }

    public String getName() {
        return name;
    }

    public int getIconId() {
        return iconId;
    }
}
