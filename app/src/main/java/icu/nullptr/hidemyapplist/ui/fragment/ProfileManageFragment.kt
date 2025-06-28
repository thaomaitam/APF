// file: app/src/main/java/icu/nullptr/hidemyapplist/ui/fragment/ProfileManageFragment.kt

package icu.nullptr.hidemyapplist.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.tsng.hidemyapplist.R
import com.tsng.hidemyapplist.databinding.FragmentProfileManageBinding // Đổi tên binding nếu cần
import icu.nullptr.hidemyapplist.service.ConfigManager
import icu.nullptr.hidemyapplist.ui.adapter.ProfileAdapter
import icu.nullptr.hidemyapplist.ui.util.navController
import icu.nullptr.hidemyapplist.ui.util.setupToolbar

class ProfileManageFragment : Fragment(R.layout.fragment_profile_manage) {

    private val binding by viewBinding<FragmentProfileManageBinding>()
    private lateinit var profileAdapter: ProfileAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(R.string.title_profile_manage),
            navigationIcon = R.drawable.baseline_arrow_back_24,
            navigationOnClick = { navController.navigateUp() }
        )

        // Khởi tạo Adapter với callback khi một item được click
        profileAdapter = ProfileAdapter { profileName ->
            navigateToEditor(profileName)
        }

        binding.profileList.apply { // Đổi tên RecyclerView trong layout thành profileList
            layoutManager = LinearLayoutManager(context)
            adapter = profileAdapter
        }

        // Nút tạo Profile mới
        binding.newProfile.setOnClickListener { // Đổi tên view trong layout thành newProfile
            navigateToEditor(null) // Truyền null để báo cho Editor biết là tạo mới
        }

        // Load danh sách profile khi fragment được hiển thị
        loadProfiles()
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại danh sách mỗi khi quay lại màn hình này
        // để hiển thị profile mới tạo hoặc đã sửa/xóa
        loadProfiles()
    }

    private fun loadProfiles() {
        // Lấy danh sách profiles từ ConfigManager
        // ConfigManager sẽ đọc từ file config đã được đồng bộ
        val profiles = ConfigManager.getProfiles() // Cần thêm hàm này vào ConfigManager
        profileAdapter.submitList(profiles)
    }

    private fun navigateToEditor(profileName: String?) {
        // Sử dụng Safe Args để truyền dữ liệu
        val action = ProfileManageFragmentDirections.actionNavProfileManageToNavProfileEditor(profileName)
        navController.navigate(action)
    }
}