package io.eodc.planit.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.eodc.planit.R;

/**
 * Adapter for interfacing {@link AssignmentType} with a Spinner
 *
 * @author 2n
 */
public class AssignmentTypeAdapter extends ArrayAdapter<AssignmentType> {
    private Context mContext;
    private int mResource;
    private List<AssignmentType> types;

    /**
     * Constructs a new AssignmentTypeAdapter
     *
     * @param context    The context to pull strings, colors, etc.
     * @param resource   The layout to use for the item view
     * @param textViewId The id of a TextView inside the layout specified
     * @param objects    A list of {@link AssignmentType} to interface into the Spinner
     */
    public AssignmentTypeAdapter(@NonNull Context context,
                                 @LayoutRes int resource,
                                 @IdRes int textViewId,
                                 @NonNull List<AssignmentType> objects) {
        super(context, resource, textViewId, objects);
        mContext = context;
        mResource = resource;
        types = objects;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, convertView);
    }

    /**
     * Equivalent of {@link android.support.v7.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)};
     * Binds information from {@link AssignmentType} to the view
     *
     * @param position    Position to get from the list of objects
     * @param convertView View to replace
     * @return A view with all information and icons bound to it
     */
    private View getCustomView(int position, @Nullable View convertView) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(mResource, null);
            holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.icon);
            holder.title = v.findViewById(R.id.title);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        AssignmentType type = types.get(position);

        holder.icon.setImageResource(type.getIconId());
        holder.title.setText(type.getName());

        return v;
    }

    /**
     * A "holder" for the information of the view
     */
    static class ViewHolder {
        ImageView icon;
        TextView title;
    }
}
