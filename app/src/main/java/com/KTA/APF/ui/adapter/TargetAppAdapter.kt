package com.KTA.APF.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.KTA.APF.MyApp
import com.KTA.APF.databinding.ItemSelectableAppBinding // <-- Cần tạo file layout item_selectable_app.xml
import com.KTA.APF.util.PackageHelper

class TargetAppAdapter(private val onAppSelected: (PackageHelper.AppInfo) -> Unit) : AppSelectAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemSelectableAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding, onAppSelected)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppViewHolder).bind(getItem(position))
    }

    class AppViewHolder(
        private val binding: ItemSelectableAppBinding,
        private val onAppSelected: (PackageHelper.AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: PackageHelper.AppInfo) {
            binding.root.setOnClickListener { onAppSelected(appInfo) }

            binding.tvAppLabel.text = appInfo.label
            binding.tvPackageName.text = appInfo.packageName
            binding.ivAppIcon.setImageDrawable(
                MyApp.instance.appIconLoader.loadIcon(appInfo.applicationInfo)
            )
        }
    }
}