package com.KTA.APF.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.KTA.APF.MyApp
import com.KTA.APF.config.Rule
import com.KTA.APF.databinding.ItemRuleBinding // <-- Cần tạo file layout item_rule.xml
import com.KTA.APF.util.PackageHelper

class RuleAdapter(private val onClick: (Rule) -> Unit) :
    ListAdapter<Rule, RuleAdapter.RuleViewHolder>(RuleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RuleViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RuleViewHolder(
        private val binding: ItemRuleBinding,
        private val onClick: (Rule) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rule: Rule) {
            binding.root.setOnClickListener { onClick(rule) }
            binding.tvTargetPackage.text = rule.targetPackage
            binding.tvProfileName.text = "Spoof as: ${rule.profileName}"

            // Tải thông tin và icon của ứng dụng
            val appInfo = PackageHelper.getAppInfo(rule.targetPackage)
            if (appInfo != null) {
                binding.tvAppLabel.text = appInfo.label
                binding.ivAppIcon.setImageDrawable(
                    MyApp.instance.appIconLoader.loadIcon(appInfo.applicationInfo)
                )
            } else {
                // Xử lý trường hợp ứng dụng đã bị gỡ cài đặt
                binding.tvAppLabel.text = "App not found"
                binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }
}

class RuleDiffCallback : DiffUtil.ItemCallback<Rule>() {
    override fun areItemsTheSame(oldItem: Rule, newItem: Rule): Boolean {
        return oldItem.targetPackage == newItem.targetPackage
    }

    override fun areContentsTheSame(oldItem: Rule, newItem: Rule): Boolean {
        return oldItem == newItem
    }
}