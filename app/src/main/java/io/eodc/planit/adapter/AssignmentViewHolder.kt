package io.eodc.planit.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R
import io.eodc.planit.db.Assignment

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
internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var isExpanded = false
    var assignment: Assignment? = null
        internal set

    @BindView(R.id.header)
    internal var layoutHeader: LinearLayout? = null
    @BindView(R.id.btn_expand)
    internal var iconExpand: ImageView? = null
    @BindView(R.id.border_class_color)
    internal var imageClassColor: ImageView? = null
    @BindView(R.id.text_header)
    internal var textHeader: TextView? = null
    @BindView(R.id.text_title)
    internal var textAssignmentName: TextView? = null
    @BindView(R.id.text_class_type)
    internal var textClassType: TextView? = null
    @BindView(R.id.text_due)
    internal var textDueDate: TextView? = null
    @BindView(R.id.text_notes)
    internal var textNotes: TextView? = null

    init {
        ButterKnife.bind(this, itemView)
    }

    internal fun hideDueDate() {
        textDueDate!!.visibility = View.GONE
    }

    internal fun showDueDate() {
        textDueDate!!.visibility = View.VISIBLE
    }

    /**
     * Hides the textNotes section of the view
     */
    private fun shrinkNotes() {
        iconExpand!!.animate()
                .rotation(0f)
        textNotes!!.visibility = View.GONE
        isExpanded = false
    }

    /**
     * Shows the textNotes section of the vie
     */
    private fun expandNotes() {
        iconExpand!!.animate()
                .rotation(-180f)
        textNotes!!.visibility = View.VISIBLE
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
