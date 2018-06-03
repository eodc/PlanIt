package io.eodc.planit.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import io.eodc.planit.adapter.AssignmentViewHolder;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.listener.OnAssignmentChangeListener;

/**
 * Touch helper that deals with swiping right to "dismiss" assignments
 *
 * @author 2n
 */
public class AssignmentTouchHelper extends ItemTouchHelper.SimpleCallback {
    private Context mContext;
    private OnAssignmentChangeListener listener;

    public AssignmentTouchHelper(Context context, int dragDirs, int swipeDirs, OnAssignmentChangeListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
        this.mContext = context;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            AssignmentViewHolder holder = (AssignmentViewHolder) viewHolder;
            int id = holder.getId();

            ContentValues values = new ContentValues();

            Cursor currentItem = mContext.getContentResolver().query(PlannerContract.AssignmentColumns.CONTENT_URI,
                    new String[] {PlannerContract.AssignmentColumns._ID, PlannerContract.AssignmentColumns.COMPLETED},
                    PlannerContract.AssignmentColumns._ID + "=?",
                    new String[] {String.valueOf(id)}, null);

            if (currentItem != null) {
                currentItem.moveToFirst();
                if (currentItem.getInt(currentItem.getColumnIndex(PlannerContract.AssignmentColumns.COMPLETED)) == 0) {
                    values.put(PlannerContract.AssignmentColumns.COMPLETED, true);

                    mContext.getContentResolver().update(PlannerContract.AssignmentColumns.CONTENT_URI, values,
                            PlannerContract.AssignmentColumns._ID + "=?",
                            new String[]{String.valueOf(id)});

                    Cursor cursor = mContext.getContentResolver().query(PlannerContract.AssignmentColumns.CONTENT_URI,
                            null,
                            PlannerContract.AssignmentColumns._ID + "=?",
                            new String[]{String.valueOf(id)},
                            null);

                    listener.onAssignmentComplete(cursor);
                } else {
                    mContext.getContentResolver().delete(PlannerContract.AssignmentColumns.CONTENT_URI,
                            PlannerContract.AssignmentColumns._ID + "=?",
                            new String[] {String.valueOf(id)});
                    listener.onAssignmentEdit();
                }
                currentItem.close();
            }
        }
    }
}
