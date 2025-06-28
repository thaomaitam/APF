// Tạo file mới: app/src/main/java/icu/nullptr/hidemyapplist/ui/adapter/ProfileAdapter.kt

package icu.nullptr.hidemyapplist.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tsng.hidemyapplist.R
import icu.nullptr.hidemyapplist.common.JsonConfig
import icu.nullptr.hidemyapplist.ui.view.ListItemView

// Adapter để hiển thị danh sách các AndroidProfile
class ProfileAdapter(
    private val onClickListener: (String) -> Unit // Truyền vào tên của profile được click
) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    private var profileNames: List<String> = emptyList()

    fun submitList(profiles: Map<String, JsonConfig.AndroidProfile>) {
        profileNames = profiles.keys.sorted() // Sắp xếp theo tên cho dễ nhìn
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: ListItemView) : RecyclerView.ViewHolder(view) {
        init {
            view.setOnClickListener {
                onClickListener.invoke(profileNames[absoluteAdapterPosition])
            }
        }

        fun bind(profileName: String) {
            with(itemView as ListItemView) {
                setIcon(R.drawable.outline_android_24) // Dùng icon Android
                text = profileName
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ListItemView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(view)
    }

    override fun getItemCount() = profileNames.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(profileNames[position])
}