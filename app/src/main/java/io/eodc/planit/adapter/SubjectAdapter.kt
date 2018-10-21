package io.eodc.planit.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifyClassFragment

/**
 * Adapter for interfacing Classes with [RecyclerView]
 *
 * @author 2n
 */

class SubjectAdapter
/**
 * Constructs a new instance of ClassAdapter
 *
 * @param subjects      A LiveData Model containing the subjects to show
 * @param mContext     The context to pull strings, colors, etc. from
 */
(private var mSubjects: List<Subject>?, private val mContext: Context) : RecyclerView.Adapter<SubjectAdapter.ClassViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        return ClassViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false))
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        if (mSubjects != null) {
            val currentSubject = mSubjects!![position]

            holder.imageClassColor!!.setBackgroundColor(Color.parseColor(currentSubject.color))
            holder.textClassName!!.text = currentSubject.name
            holder.textTeacherName!!.text = currentSubject.teacher
            holder.itemView.setOnLongClickListener { view ->
                ModifyClassFragment.newInstance(currentSubject)
                        .show((mContext as AppCompatActivity).supportFragmentManager, null)

                val v = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else
                        v.vibrate(500)
                }
                true
            }
        }
    }

    fun swapClassesList(mSubjects: List<Subject>) {
        this.mSubjects = mSubjects
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (mSubjects != null)
            mSubjects!!.size
        else
            0
    }

    /**
     * Holder for information and attributes for the class view
     */
    internal inner class ClassViewHolder
    /**
     * Constructs a new instance of ClassViewHolder
     *
     * @param itemView The view to bind information to this holder
     */
    (itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.border_class_color)
        var imageClassColor: ImageView? = null
        @BindView(R.id.text_title)
        var textClassName: TextView? = null
        @BindView(R.id.text_teacher)
        var textTeacherName: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
