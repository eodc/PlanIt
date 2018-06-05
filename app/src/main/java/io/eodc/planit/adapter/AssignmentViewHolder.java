package io.eodc.planit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;

/**
 * Holder for information and attributes of the assignment view
 *
 * @author 2n
 */
public class AssignmentViewHolder extends RecyclerView.ViewHolder {
    boolean isExpanded = false;
    int     id;

    @BindView(R.id.header)              LinearLayout    layoutHeader;
    @BindView(R.id.ic_due_date)         ImageView       iconDueDate;
    @BindView(R.id.expand_button)       ImageView       iconExpand;
    @BindView(R.id.class_color)         ImageView       imageClassColor;
    @BindView(R.id.header_label)        TextView        textHeader;
    @BindView(R.id.assignment_name)     TextView        textAssignmentName;
    @BindView(R.id.class_and_type)      TextView        textClassType;
    @BindView(R.id.due_date)            TextView        textDueDate;
    @BindView(R.id.notes_text)          TextView        textNotes;

    /**
     * Constructs an instance of an AssignmentViewHolder
     *
     * @param itemView The view to bind to this holder
     */
    AssignmentViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    /**
     * Returns the row of the assignment in the table
     *
     * @see io.eodc.planit.db.PlannerContract.AssignmentColumns
     */
    public int getId() {
        return id;
    }

    /**
     * Hides the textNotes section of the view
     */
    public void shrinkNotes() {
        iconExpand.animate()
                .rotation(0f);
        textNotes.setVisibility(View.GONE);
        isExpanded = false;
    }

    /**
     * Shows the textNotes section of the vie
     */
    public void expandNotes() {
        iconExpand.animate()
                .rotation(-180f);
        textNotes.setVisibility(View.VISIBLE);
        isExpanded = true;
    }
}
