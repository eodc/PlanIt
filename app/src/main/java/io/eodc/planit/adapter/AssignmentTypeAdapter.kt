package io.eodc.planit.adapter

import android.content.Context
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import io.eodc.planit.R

/**
 * Adapter for interfacing [AssignmentType] with a Spinner
 *
 * @author 2n
 */
class AssignmentTypeAdapter
/**
 * Constructs a new AssignmentTypeAdapter
 *
 * @param context    The context to pull strings, colors, etc.
 * @param resource   The layout to use for the item view
 * @param textViewId The id of a TextView inside the layout specified
 * @param objects    A list of [AssignmentType] to interface into the Spinner
 */
(private val mContext: Context,
 @param:LayoutRes private val mResource: Int,
 @IdRes textViewId: Int,
 private val mTypes: List<AssignmentType>) : ArrayAdapter<AssignmentType>(mContext, mResource, textViewId, mTypes) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView)
    }

    /**
     * Equivalent of [android.support.v7.widget.RecyclerView.Adapter.onBindViewHolder];
     * Binds information from [AssignmentType] to the view
     *
     * @param position    Position to get from the list of objects
     * @param convertView View to replace
     * @return A view with all information and icons bound to it
     */
    private fun getCustomView(position: Int, convertView: View?): View {
        var v = convertView
        val holder: ViewHolder

        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(mResource, null)
            holder = ViewHolder()
            holder.icon = v!!.findViewById(R.id.ic_type)
            holder.title = v.findViewById(R.id.text_title)
            v.tag = holder
        } else {
            holder = v.tag as ViewHolder
        }

        val type = mTypes[position]

        holder.icon!!.setImageResource(type.iconId)
        holder.title!!.text = type.name

        return v
    }

    /**
     * A "holder" for the information of the view
     */
    internal class ViewHolder {
        var icon: ImageView? = null
        var title: TextView? = null
    }
}