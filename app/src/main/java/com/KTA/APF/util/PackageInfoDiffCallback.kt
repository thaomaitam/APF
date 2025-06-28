package com.KTA.APF.util

import androidx.recyclerview.widget.DiffUtil

/**
 * Lớp callback để ListAdapter so sánh các đối tượng AppInfo.
 * Giúp RecyclerView biết chính xác item nào đã thay đổi, thêm, hoặc xóa.
 */
class PackageInfoDiffCallback : DiffUtil.ItemCallback<PackageHelper.AppInfo>() {

    /**
     * Kiểm tra xem hai item có phải là cùng một đối tượng hay không (thường dựa trên ID duy nhất).
     */
    override fun areItemsTheSame(oldItem: PackageHelper.AppInfo, newItem: PackageHelper.AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    /**
     * Kiểm tra xem nội dung của hai item có giống nhau hay không.
     * Hàm này chỉ được gọi nếu areItemsTheSame trả về true.
     */
    override fun areContentsTheSame(oldItem: PackageHelper.AppInfo, newItem: PackageHelper.AppInfo): Boolean {
        // So sánh label vì nó có thể thay đổi (ví dụ sau khi cập nhật ngôn ngữ)
        return oldItem.label == newItem.label
    }
}