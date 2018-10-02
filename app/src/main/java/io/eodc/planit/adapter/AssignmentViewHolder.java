package io.eodc.planit.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.db.Assignment;

/**
 * Holder for information and attributes of the assignment view
 *
 * @author 2n
 */
public class AssignmentViewHolder extends RecyclerView.ViewHolder {
    boolean isExpanded = false;
    Assignment assignment;

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

    public Assignment getAssignment() {
        return assignment;
    }

    void hideDueDate() {
        textDueDate.setVisibility(View.GONE);
        iconDueDate.setVisibility(View.GONE);
    }

    void showDueDate() {
        textDueDate.setVisibility(View.VISIBLE);
        iconDueDate.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the textNotes section of the view
     */
    void shrinkNotes() {
        iconExpand.animate()
                .rotation(0f);
        textNotes.setVisibility(View.GONE);
        isExpanded = false;
    }

    /**
     * Shows the textNotes section of the vie
     */
    void expandNotes() {
        iconExpand.animate()
                .rotation(-180f);
        textNotes.setVisibility(View.VISIBLE);
        isExpanded = true;
    }

    void handleNoteClick() {
        if (isExpanded) {
            shrinkNotes();
        } else {
            expandNotes();
        }
    }
}
