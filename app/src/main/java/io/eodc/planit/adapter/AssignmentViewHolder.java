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
    int id;

    @BindView(R.id.header)
    LinearLayout header;
    @BindView(R.id.header_label)
    TextView headerLabel;
    @BindView(R.id.class_color)
    ImageView classColor;
    @BindView(R.id.assignment_name)
    TextView assignmentName;
    @BindView(R.id.class_and_type)
    TextView classAndTypeName;
    @BindView(R.id.due_date)
    TextView dueDate;
    @BindView(R.id.ic_due_date)
    ImageView dueDateIcon;
    @BindView(R.id.expand_button)
    ImageView expandButton;
    @BindView(R.id.notes_text)
    TextView notes;

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
     * Hides the notes section of the view
     */
    public void shrinkNotes() {
        expandButton.animate()
                .rotation(0f);
        notes.setVisibility(View.GONE);
        isExpanded = false;
    }

    /**
     * Shows the notes section of the vie
     */
    public void expandNotes() {
        expandButton.animate()
                .rotation(-180f);
        notes.setVisibility(View.VISIBLE);
        isExpanded = true;
    }
}
