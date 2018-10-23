package io.eodc.planit.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import io.eodc.planit.R
import kotlinx.android.synthetic.main.item_license.view.*

class LicenseAdapter(private val mContext: Context, private val mLicenses: List<License>) : androidx.recyclerview.widget.RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        return LicenseViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_license, parent, false))
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        val license = mLicenses[position]
        holder.textName.text = license.name
        holder.textCopyright.text = mContext.getString(R.string.license_copyright,
                license.author, license.year)
        holder.btnLicense.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(license.licenseUrl)
            mContext.startActivity(intent)
        }
        holder.btnProject.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(license.projectUrl)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mLicenses.size
    }

    inner class LicenseViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var textName: TextView = itemView.textHeaderTitle
        var textCopyright: TextView = itemView.text_copyright
        var btnLicense: Button = itemView.btn_license
        var btnProject: Button = itemView.btn_project
    }
}
