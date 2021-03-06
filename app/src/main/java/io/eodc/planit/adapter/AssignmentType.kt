package io.eodc.planit.adapter

import androidx.annotation.DrawableRes

/**
 * Represents an Assignment Type for use in a Spinner
 *
 * @author 2n
 * @see AssignmentTypeAdapter
 */
class AssignmentType
/**
 * Constructs a new instance of an Assignment Type
 *
 * @param name   The mName of the type
 * @param iconId The drawable resource id to be used as an icon in the view
 */
(val name: String, @param:DrawableRes val iconId: Int)
