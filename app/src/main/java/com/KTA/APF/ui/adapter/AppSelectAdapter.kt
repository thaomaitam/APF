package com.KTA.APF.ui.adapter

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.KTA.APF.util.AppLogger
import com.KTA.APF.util.PackageHelper
import com.KTA.APF.util.PackageInfoDiffCallback

/**
 * Lớp Adapter cơ sở cho việc hiển thị danh sách ứng dụng.
 * Hỗ trợ lọc (search) và sử dụng ListAdapter để cập nhật danh sách hiệu quả.
 */
abstract class AppSelectAdapter :
    ListAdapter<PackageHelper.AppInfo, RecyclerView.ViewHolder>(PackageInfoDiffCallback()), Filterable {

    // Danh sách gốc, không bị thay đổi bởi bộ lọc
    private var originalList: List<PackageHelper.AppInfo> = emptyList()

    /**
     * Cập nhật danh sách ứng dụng gốc.
     * ListAdapter sẽ tự động xử lý việc hiển thị danh sách này.
     */
    fun updateAppList(list: List<PackageHelper.AppInfo>) {
        originalList = list
        submitList(list) // Gửi danh sách ban đầu cho ListAdapter
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString().trim().lowercase()
                
                val filteredList = if (query.isEmpty()) {
                    originalList // Nếu không có query, trả về danh sách gốc
                } else {
                    originalList.filter {
                        // Lọc theo tên ứng dụng hoặc tên package
                        it.label.lowercase().contains(query) || it.packageName.lowercase().contains(query)
                    }
                }
                
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filtered = results?.values as? List<PackageHelper.AppInfo>
                // Gửi danh sách đã lọc cho ListAdapter để nó cập nhật UI
                submitList(filtered) {
                    // Cuộn lên đầu danh sách sau khi lọc xong để có trải nghiệm tốt hơn
                    if (itemCount > 0) {
                        // TODO: Cần có tham chiếu đến RecyclerView để thực hiện cuộn
                        AppLogger.d("Filtered list published with ${filtered?.size} items.")
                    }
                }
            }
        }
    }
}