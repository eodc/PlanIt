package io.eodc.planit.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.db.PlannerContract;
import io.eodc.planit.fragment.ModifyClassFragment;
import io.eodc.planit.listener.OnClassListChangeListener;

/**
 * Adapter for interfacing Classes with {@link RecyclerView}
 *
 * @author 2n
 */

public class ClassesAdapter extends RecyclerView.Adapter<ClassesAdapter.ClassViewHolder> {
    private Cursor                      mClassCursor;
    private Context                     mContext;
    private OnClassListChangeListener   mListener;

    /**
     * Constructs a new instance of ClassAdapter
     *
     * @param mClassCursor The cursor containing the classes to show
     * @param mContext     The context to pull strings, colors, etc. from
     * @param l            The mListener listening for changes to the class list.
     * @see OnClassListChangeListener
     */
    public ClassesAdapter(Cursor mClassCursor, Context mContext, OnClassListChangeListener l) {
        this.mClassCursor = mClassCursor;
        this.mContext = mContext;
        this.mListener = l;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClassViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        mClassCursor.moveToPosition(position);
        holder.imageClassColor.setBackgroundColor(Color.parseColor(mClassCursor.getString(mClassCursor.getColumnIndex(PlannerContract.ClassColumns.COLOR))));
        holder.textClassName.setText(mClassCursor.getString(mClassCursor.getColumnIndex(PlannerContract.ClassColumns.NAME)));
        holder.textTeacherName.setText(mClassCursor.getString(mClassCursor.getColumnIndex(PlannerContract.ClassColumns.TEACHER)));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ModifyClassFragment.newInstance(mListener, ModifyClassFragment.FLAG_MOD_CLASS,
                        mClassCursor.getInt(mClassCursor.getColumnIndex(PlannerContract.ClassColumns._ID)))
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), null);

                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else v.vibrate(500);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mClassCursor == null) return 0;
        else return mClassCursor.getCount();
    }

    /**
     * Holder for information and attributes for the class view
     */
    class ClassViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.class_color) ImageView   imageClassColor;
        @BindView(R.id.tv_title)    TextView    textClassName;
        @BindView(R.id.tv_teacher)  TextView    textTeacherName;

        /**
         * Constructs a new instance of ClassViewHolder
         *
         * @param itemView The view to bind information to this holder
         */
        ClassViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
