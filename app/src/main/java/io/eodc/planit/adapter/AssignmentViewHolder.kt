package io.eodc.planit.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.eodc.planit.db.Assignment
import kotlinx.android.synthetic.main.item_assignment.view.*

/**
 * Holder for information and attributes of the assignment view
 *
 * @author 2n
 */
class AssignmentViewHolder
/**
 * Constructs an instance of an AssignmentViewHolder
 *
 * @param itemView The view to bind to this holder
 */
internal constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    private var isExpanded = false
    var assignment: Assignment? = null

    internal var layoutHeader: LinearLayout = itemView.header
    internal var iconExpand: ImageView = itemView.btn_expand
    internal var imageClassColor: ImageView = itemView.border_class_color
    internal var textHeader: TextView = itemView.text_header
    internal var textAssignmentName: TextView = itemView.textHeaderTitle
    internal var textClassType: TextView = itemView.text_class_type
    internal var textDueDate: TextView = itemView.text_due
    internal var textNotes: TextView = itemView.text_notes

    internal fun hideDueDate() {
        textDueDate.visibility = View.GONE
    }

    internal fun showDueDate() {
        textDueDate.visibility = View.VISIBLE
    }

    /**
     * Hides the textNotes section of the view
     */
    private fun shrinkNotes() {
        iconExpand.animate()
                .rotation(0f)
        textNotes.visibility = View.GONE
        isExpanded = false
    }

    /**
     * Shows the textNotes section of the vie
     */
    private fun expandNotes() {
        iconExpand.animate()
                .rotation(-180f)
        textNotes.visibility = View.VISIBLE
        isExpanded = true
    }

    internal fun handleNoteClick() {
        if (isExpanded) {
            shrinkNotes()
        } else {
            expandNotes()
        }
    }
}
