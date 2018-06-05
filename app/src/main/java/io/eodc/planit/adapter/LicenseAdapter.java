package io.eodc.planit.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.eodc.planit.R;

public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder> {
    private Context         mContext;
    private List<License>   mLicenses;

    public LicenseAdapter(Context mContext, List<License> licenses) {
        this.mContext = mContext;
        this.mLicenses = licenses;
    }

    @NonNull
    @Override
    public LicenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LicenseViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_license, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LicenseViewHolder holder, int position) {
        final License license = mLicenses.get(position);
        holder.textName.setText(license.getName());
        holder.textCopyright.setText(mContext.getString(R.string.license_cpyrght,
                license.getAuthor(), license.getYear()));
        holder.btnLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(license.getLicenseUrl()));
                mContext.startActivity(intent);
            }
        });
        holder.btnProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(license.getProjectUrl()));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLicenses.size();
    }

    class LicenseViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_name)         TextView    textName;
        @BindView(R.id.tv_copyright)    TextView    textCopyright;
        @BindView(R.id.btn_license)     Button      btnLicense;
        @BindView(R.id.btn_project)     Button      btnProject;

        LicenseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
