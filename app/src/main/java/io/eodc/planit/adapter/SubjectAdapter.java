package io.eodc.planit.adapter;

import android.content.Context;
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

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;
import io.eodc.planit.db.Subject;
import io.eodc.planit.fragment.ModifyClassFragment;

/**
 * Adapter for interfacing Classes with {@link RecyclerView}
 *
 * @author 2n
 */

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ClassViewHolder> {
    private List<Subject> mSubjects;
    private Context           mContext;

    /**
     * Constructs a new instance of ClassAdapter
     *
     * @param subjects      A LiveData Model containing the subjects to show
     * @param mContext     The context to pull strings, colors, etc. from
     */
    public SubjectAdapter(List<Subject> subjects, Context mContext) {
        this.mSubjects = subjects;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ClassViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        if (mSubjects != null) {
            Subject currentSubject = mSubjects.get(position);

            holder.imageClassColor.setBackgroundColor(Color.parseColor(currentSubject.getColor()));
            holder.textClassName.setText(currentSubject.getName());
            holder.textTeacherName.setText(currentSubject.getTeacher());
            holder.itemView.setOnLongClickListener(view -> {
                ModifyClassFragment.newInstance(currentSubject)
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), null);

                Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else v.vibrate(500);
                }
                return true;
            });
        }
    }

    public void swapClassesList(List<Subject> mSubjects) {
        this.mSubjects = mSubjects;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mSubjects != null) return mSubjects.size();
        else return 0;
    }

    /**
     * Holder for information and attributes for the class view
     */
    class ClassViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.border_class_color) ImageView   imageClassColor;
        @BindView(R.id.text_title)    TextView    textClassName;
        @BindView(R.id.text_teacher)  TextView    textTeacherName;

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