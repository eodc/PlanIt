package io.eodc.planit.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import io.eodc.planit.R

class LicenseAdapter(private val mContext: Context, private val mLicenses: List<License>) : RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        return LicenseViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_license, parent, false))
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        val license = mLicenses[position]
        holder.textName!!.text = license.name
        holder.textCopyright!!.text = mContext.getString(R.string.license_cpyrght,
                license.author, license.year)
        holder.btnLicense!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(license.licenseUrl)
            mContext.startActivity(intent)
        }
        holder.btnProject!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(license.projectUrl)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mLicenses.size
    }

    internal inner class LicenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.text_title)
        var textName: TextView? = null
        @BindView(R.id.text_copyright)
        var textCopyright: TextView? = null
        @BindView(R.id.btn_license)
        var btnLicense: Button? = null
        @BindView(R.id.btn_project)
        var btnProject: Button? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
