package io.eodc.planit.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Iterables;

import org.joda.time.DateTime;

import java.util.List;

import io.eodc.planit.R;
import io.eodc.planit.activity.MainActivity;
import io.eodc.planit.db.Assignment;
import io.eodc.planit.db.Class;
import io.eodc.planit.fragment.ModifyAssignmentFragment;

/**
 * Adapter for interfacing mAssignments to {@link RecyclerView}
 *
 * @author 2n
 */
public class AssignmentsAdapter extends RecyclerView.Adapter<AssignmentViewHolder> {

    private static final int NEVER_SHOW_DIVIDER         = 0;

    private static final int VIEW_TYPE_NORMAL           = 1 << 1;
    private static final int VIEW_TYPE_DIVIDER          = 1 << 2;
    private static final int VIEW_TYPE_NOTES            = 1 << 3;

    private Context                     mContext;

    private List<Assignment>  mAssignments;
    private List<Class>       mClasses;

    private int     mShowDividerFlag;

    /**
     * Constructs a new instance of AssignmentsAdapter. Dynamically displays relevant information
     * about the assignment in its view depending on its position in the list and it due date.
     * <p>
     * {@link #swapAssignmentsList(List)} should be called as soon as the mAssignments cursor is
     * available, or else nothing will be shown.
     *
     * @param context     The context to use for grabbing strings, colors, etc.
     * @param classes     The LiveData containing all the user's classes.
     * @see AssignmentViewHolder
     **/
    public AssignmentsAdapter(Context context, List<Class> classes) {
        this.mContext = context;
        this.mClasses = classes;
        this.mShowDividerFlag = -1;
    }

    /**
     * Constructs a new instance of AssignmentsAdapter while specifying if dividers should be shown.
     * This constructor should be used in instances where the full list of mAssignments is not being
     * displayed, or the context of when the mAssignments are due have been established.
     *
     * @param context      The context to use for grabbing strings, colors, etc.
     * @param classes     The LiveData containing all the user's classes.
     * @param showDividers Whether or not dividers should be shown.
     */
    public AssignmentsAdapter(Context context, List<Class> classes, boolean showDividers) {
        this.mContext = context;
        this.mClasses = classes;
        this.mShowDividerFlag = showDividers ? -1 : NEVER_SHOW_DIVIDER;
    }

    /**
     * Swaps the current assignment cursor, then notifies the parent {@link RecyclerView} that the
     * current data set has been changed.
     *
     * @param liveData The new {@link Cursor} that should be used as the data set.
     */
    public void swapAssignmentsList(List<Assignment> liveData) {
        mAssignments = liveData;
        notifyDataSetChanged();
    }

    /**
     * Differentiates one assignment from another, and decides whether to show that it has textNotes,
     * is on a different day, etc.
     *
     * @param position Position in the cursor
     * @return Type of item view
     */
    @Override
    public int getItemViewType(int position) {
        Assignment currentAssignment = mAssignments.get(position);
        boolean hasNotes = !currentAssignment.getNotes()
                .trim()
                .equals("");

        if (mShowDividerFlag == NEVER_SHOW_DIVIDER) {
            return getNormalViewType(hasNotes);
        }
        if (position == 0) {
            return getDividerViewType(hasNotes);
        } else {
            DateTime previousDue = mAssignments.get(position - 1)
                    .getDueDate();
            DateTime currentDue = currentAssignment.getDueDate();
            DateTime now = new DateTime();

            if (currentDue.isBeforeNow() && now.getDayOfYear() - currentDue.getDayOfYear() >= 1) { // Overdue [Don't divide]
                return getNormalViewType(hasNotes);
            }

            if (currentDue.isBefore(now.plusWeeks(1))) { // Within same week (Now + 7 days) [Divide for every day]
                return !currentDue.dayOfWeek().equals(previousDue.dayOfWeek()) ?
                    getDividerViewType(hasNotes) :
                    getNormalViewType(hasNotes);
            } else if (currentDue.monthOfYear().equals(now.monthOfYear())) { // Within same month [Divide for every week]
                return previousDue.isBefore(now.plusWeeks(1)) ||
                        !currentDue.weekOfWeekyear().equals(previousDue.weekOfWeekyear()) ?
                        getDividerViewType(hasNotes) :
                        getNormalViewType(hasNotes);
            } else if (currentDue.year().equals(now.year())) { // Within same year [Divide for every month]
                return !currentDue.monthOfYear().equals(previousDue.monthOfYear()) ||
                        previousDue.isBefore(now.plusWeeks(1)) ?
                        getDividerViewType(hasNotes) :
                        getNormalViewType(hasNotes);
            } else {
                return !currentDue.year().equals(previousDue.year()) ?
                        getDividerViewType(hasNotes) :
                        getNormalViewType(hasNotes);
            }
        }
    }

    private int getDividerViewType(boolean hasNotes) {
        return hasNotes ?
                VIEW_TYPE_DIVIDER | VIEW_TYPE_NOTES :
                VIEW_TYPE_DIVIDER;
    }

    private int getNormalViewType(boolean hasNotes) {
        return hasNotes ?
                VIEW_TYPE_NORMAL | VIEW_TYPE_NOTES :
                VIEW_TYPE_NORMAL;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AssignmentViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_assignment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = mAssignments.get(position);
        Assignment previousAssignment = null;
        if (position > 0) {
            previousAssignment = mAssignments.get(position - 1);
        }
        Class assignmentClass = Iterables.find(mClasses, value -> value.getId() == assignment.getClassId());

        DateTime dtCurrent = assignment.getDueDate();
        int assignmentTypeFlag = assignment.getType();

        String assignmentType = "";

        switch (assignmentTypeFlag) {
            case Assignment.TYPE_TEST:
                assignmentType = "Test/Quiz";
                break;
            case Assignment.TYPE_PROJECT:
                assignmentType = "Project";
                break;
            case Assignment.TYPE_HOMEWORK:
                assignmentType = "Homework";
                break;
        }


        String classAndTypeText = mContext.getString(R.string.class_name_and_type_text,
                assignmentClass.getName(),
                assignmentType);

        holder.textClassType.setText(classAndTypeText);
        holder.textDueDate.setText(dtCurrent.toString(mContext.getString(R.string.due_date_pattern)));
        holder.assignment = assignment;

        if ((holder.getItemViewType() & VIEW_TYPE_DIVIDER) == VIEW_TYPE_DIVIDER) {
            String headerText = getHeaderText(dtCurrent);
            holder.textHeader.setText(headerText);
            holder.layoutHeader.setVisibility(View.VISIBLE);
            holder.showDueDate();
        } else if (previousAssignment != null &&
                !assignment.getDueDate().dayOfYear()
                .equals(previousAssignment.getDueDate().dayOfYear())) {
            holder.showDueDate();
        } else {
            holder.hideDueDate();
        }

        if ((holder.getItemViewType() & VIEW_TYPE_NOTES) == VIEW_TYPE_NOTES) {
            holder.iconExpand.setVisibility(View.VISIBLE);
            holder.textNotes.setText(assignment.getNotes());
            holder.itemView.setOnClickListener(view -> holder.handleNoteClick());
        }

        holder.itemView.setOnLongClickListener(view -> {
            if (mContext instanceof MainActivity) {
                MainActivity activity = (MainActivity) mContext;
                DialogFragment editFragment = ModifyAssignmentFragment.newInstance(holder.assignment);

                editFragment.show(activity.getSupportFragmentManager(), null);

                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        v.vibrate(VibrationEffect
                                .createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    else v.vibrate(500);
                }
            }
            return true;
        });

        holder.imageClassColor.setBackgroundColor(Color.parseColor(assignmentClass.getColor()));
        holder.textAssignmentName.setText(assignment.getTitle());
    }

    @Override
    public int getItemCount() {
        return mAssignments == null ? 0 : mAssignments.size();
    }

    /**
     * Gets the text shown at the divider (layoutHeader) based off of the assignment's due date and the
     * current day.
     *
     * @param currentDue The {@link DateTime} of the assignment with the layoutHeader shown
     * @return The parsed layoutHeader text
     */
    private String getHeaderText(DateTime currentDue) {
        DateTime now = new DateTime();
        if (currentDue.isBeforeNow() && now.getDayOfYear() - currentDue.getDayOfYear() >= 1) { // Overdue
            return "Overdue";
        }
        if (currentDue.isBefore(now.plusWeeks(1))) { // Within same week (Now + 7 days) [Divide for every day]
            int daysUntil = currentDue.getDayOfYear() - now.getDayOfYear();
            switch (daysUntil) {
                case 0:
                    return mContext.getString(R.string.assignment_header_near_future_text,
                            "Today");
                case 1:
                    return mContext.getString(R.string.assignment_header_near_future_text,
                            "Tomorrow");
                default:
                    return mContext.getString(R.string.assignment_header_far_future_text,
                            daysUntil, "Days");
            }
        } else if (currentDue.monthOfYear().equals(now.monthOfYear())) { // Within same month [Divide for every week]
            int weeksUntil = currentDue.getWeekOfWeekyear() - now.getWeekOfWeekyear();
            switch (weeksUntil) {
                case 1:
                    return mContext.getString(R.string.assignment_header_near_future_text,
                            "Next Week");
                default:
                    return mContext.getString(R.string.assignment_header_far_future_text,
                            weeksUntil,
                            "Weeks");
            }
        } else if (currentDue.year().equals(now.year())) { // Within same year [Divide for every month]
            return mContext.getString(R.string.assignment_header_near_future_text,
                    "in " + currentDue.toString(mContext.getString(R.string.assignment_header_month_pattern)));
        } else {
            int yearsUntil = currentDue.getYear() - now.getYear();
            switch (yearsUntil) {
                case 1:
                    return mContext.getString(R.string.assignment_header_near_future_text,
                            "Next Year");
                default:
                    return mContext.getString(R.string.assignment_header_far_future_text,
                            yearsUntil,
                            "Years");
            }
        }
    }

}
