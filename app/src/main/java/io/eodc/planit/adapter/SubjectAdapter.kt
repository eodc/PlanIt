package io.eodc.planit.adapter

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import io.eodc.planit.R
import io.eodc.planit.db.Subject
import io.eodc.planit.fragment.ModifyClassFragment
import kotlinx.android.synthetic.main.item_class.view.*

/**
 * Adapter for interfacing Classes with [RecyclerView]
 *
 * @author 2n
 */

class SubjectAdapter
/**
 * Constructs a new instance of ClassAdapter
 *
 * @param mSubjects    A list containing the subjects to show
 * @param mContext     The context to pull strings, colors, etc. from
 */
(private var mSubjects: List<Subject>, private val mContext: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<SubjectAdapter.ClassViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        return ClassViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false))
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val currentSubject = mSubjects[position]

        holder.imageClassColor.setBackgroundColor(Color.parseColor(currentSubject.color))
        holder.textClassName.text = currentSubject.name
        holder.textTeacherName.text = currentSubject.teacher
        holder.itemView.setOnLongClickListener {
            ModifyClassFragment.newInstance(currentSubject)
                    .show((mContext as AppCompatActivity).supportFragmentManager, null)

            val v = mContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else
                v.vibrate(500)
            true
        }
    }

    fun swapClassesList(mSubjects: List<Subject>) {
        this.mSubjects = mSubjects
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mSubjects.size
    }

    /**
     * Holder for information and attributes for the class view
     */
    inner class ClassViewHolder
    /**
     * Constructs a new instance of ClassViewHolder
     *
     * @param itemView The view to bind information to this holder
     */
    (itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var imageClassColor: ImageView = itemView.border_class_color
        var textClassName: TextView = itemView.textHeaderTitle
        var textTeacherName: TextView = itemView.text_teacher

    }
}
