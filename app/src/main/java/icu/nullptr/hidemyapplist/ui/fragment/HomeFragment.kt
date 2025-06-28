// file: app/src/main/java/icu/nullptr/hidemyapplist/ui/fragment/HomeFragment.kt

package icu.nullptr.hidemyapplist.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialElevationScale
import com.tsng.hidemyapplist.BuildConfig
import com.tsng.hidemyapplist.R
import com.tsng.hidemyapplist.databinding.FragmentHomeBinding
import icu.nullptr.hidemyapplist.common.Constants
import icu.nullptr.hidemyapplist.hmaApp
import icu.nullptr.hidemyapplist.service.ConfigManager
import icu.nullptr.hidemyapplist.service.ServiceClient
import icu.nullptr.hidemyapplist.ui.util.ThemeUtils.getColor
import icu.nullptr.hidemyapplist.ui.util.ThemeUtils.themeColor
import icu.nullptr.hidemyapplist.ui.util.makeToast
import icu.nullptr.hidemyapplist.ui.util.navController
import icu.nullptr.hidemyapplist.ui.util.setupToolbar
import java.io.IOException

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val binding by viewBinding<FragmentHomeBinding>()

    // Logic sao lưu/khôi phục vẫn giữ nguyên vì nó hữu ích
    private val backupSAFLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri ?: return@registerForActivityResult
            try {
                ConfigManager.configFile.inputStream().use { input ->
                    hmaApp.contentResolver.openOutputStream(uri)?.use { output ->
                        input.copyTo(output)
                    }
                }
                makeToast(R.string.home_exported)
            } catch (e: IOException) {
                makeToast(R.string.home_export_failed)
            }
        }

    private val restoreSAFLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            runCatching {
                val backupJson = hmaApp.contentResolver.openInputStream(uri)?.reader()?.readText()
                    ?: throw IOException(getString(R.string.home_import_file_damaged))
                
                // Sử dụng ConfigManager để import, nó đã có sẵn logic xử lý
                ConfigManager.importConfig(backupJson)
                makeToast(R.string.home_import_successful)
                // Có thể cần refresh UI hoặc thông báo cho service sau khi import
                // ServiceClient.syncConfig(ConfigManager.configFile.readText())
            }.onFailure {
                it.printStackTrace()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.home_import_failed)
                    .setMessage(it.localizedMessage ?: "Unknown error")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Giữ lại hiệu ứng chuyển cảnh
        exitTransition = MaterialElevationScale(false)
        reenterTransition = MaterialElevationScale(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Thiết lập Toolbar, không còn menu "About"
        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(R.string.app_name)
        )

        // Tải quảng cáo nếu cần
        runCatching {
            binding.adBanner.loadAd(AdRequest.Builder().build())
        }
        
        // --- Cập nhật OnClickListener ---

        // 1. Điều hướng đến màn hình quản lý Profile
        // **Lưu ý:** Bạn cần đổi ID của view trong `fragment_home.xml`
        // từ `template_manage` thành `profile_manage` (hoặc tên tương tự)
        binding.profileManage.setOnClickListener {
            // Sử dụng action đã định nghĩa trong home_nav_graph.xml
            navController.navigate(R.id.action_nav_home_to_nav_profile_manage)
        }

        // 2. Điều hướng đến màn hình quản lý ứng dụng
        binding.appManage.setOnClickListener {
            navController.navigate(R.id.action_nav_home_to_nav_app_manage)
        }
        
        // 3. Xóa bỏ OnClickListener của `detectionTest`

        // 4. Giữ lại OnClickListener cho sao lưu và khôi phục
        binding.backupConfig.setOnClickListener {
            // Đặt tên file backup mặc định
            backupSAFLauncher.launch("APF_Config_${System.currentTimeMillis()}.json")
        }
        binding.restoreConfig.setOnClickListener {
            restoreSAFLauncher.launch("application/json")
        }
    }

    override fun onStart() {
        super.onStart()
        // Cập nhật trạng thái của module, logic này vẫn hữu ích
        updateStatusCard()
    }
    
    private fun updateStatusCard() {
        val serviceVersion = ServiceClient.serviceVersion
        
        val color = when {
            // Module chưa được kích hoạt trong Xposed
            !hmaApp.isHooked -> getColor(R.color.gray)
            // Service trung tâm chưa chạy
            serviceVersion == 0 -> getColor(R.color.invalid)
            // Hoạt động bình thường
            else -> themeColor(com.google.android.material.R.attr.colorPrimary)
        }
        binding.statusCard.setCardBackgroundColor(color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.statusCard.outlineAmbientShadowColor = color
            binding.statusCard.outlineSpotShadowColor = color
        }

        if (hmaApp.isHooked) {
            binding.moduleStatusIcon.setImageResource(R.drawable.outline_done_all_24)
            val versionNameSimple = BuildConfig.VERSION_NAME.substringBefore(".r")
            binding.moduleStatus.text = getString(R.string.home_xposed_activated, versionNameSimple, BuildConfig.VERSION_CODE)
        } else {
            binding.moduleStatusIcon.setImageResource(R.drawable.outline_extension_off_24)
            binding.moduleStatus.setText(R.string.home_xposed_not_activated)
        }

        if (serviceVersion != 0) {
            if (serviceVersion < icu.nullptr.hidemyapplist.common.BuildConfig.SERVICE_VERSION) {
                binding.serviceStatus.text = getString(R.string.home_xposed_service_old)
            } else {
                binding.serviceStatus.text = getString(R.string.home_xposed_service_on, serviceVersion)
            }
            // Không còn 'filterCount'
            binding.filterCount.visibility = View.GONE
        } else {
            binding.serviceStatus.setText(R.string.home_xposed_service_off)
            binding.filterCount.visibility = View.GONE
        }
    }
}