// file: app/src/main/java/icu/nullptr/hidemyapplist/ui/fragment/AppSettingsFragment.kt

package icu.nullptr.hidemyapplist.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tsng.hidemyapplist.R
import com.tsng.hidemyapplist.databinding.FragmentSettingsBinding // Layout này vẫn dùng chung
import icu.nullptr.hidemyapplist.common.JsonConfig
import icu.nullptr.hidemyapplist.service.ConfigManager
import icu.nullptr.hidemyapplist.ui.util.setupToolbar
import icu.nullptr.hidemyapplist.util.PackageHelper

class AppSettingsFragment : Fragment(R.layout.fragment_settings) {

    private val binding by viewBinding<FragmentSettingsBinding>()
    private val args: AppSettingsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập toolbar
        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(R.string.title_app_settings),
            navigationIcon = R.drawable.baseline_arrow_back_24,
            navigationOnClick = { findNavController().navigateUp() }
        )

        // Thêm AppPreferenceFragment vào container
        if (childFragmentManager.findFragmentById(R.id.settings_container) == null) {
            val fragment = AppPreferenceFragment().apply {
                // Truyền packageName vào fragment con thông qua arguments
                arguments = Bundle().apply {
                    putString("packageName", args.packageName)
                }
            }
            childFragmentManager.beginTransaction()
                .replace(R.id.settings_container, fragment)
                .commit()
        }
    }

    /**
     * Fragment con chứa các Preference, chịu trách nhiệm xử lý logic chính.
     */
    class AppPreferenceFragment : PreferenceFragmentCompat() {

        private lateinit var packageName: String
        private lateinit var appFakerConfig: JsonConfig.AppFakerConfig

        private lateinit var enableFakerSwitch: SwitchPreference
        private lateinit var chooseProfilePref: Preference

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Lấy packageName từ arguments
            packageName = requireArguments().getString("packageName")!!
            // Lấy cấu hình faker hiện tại cho ứng dụng này từ ConfigManager
            appFakerConfig = ConfigManager.getAppFakerConfig(packageName)
            
            setPreferencesFromResource(R.xml.app_settings, rootKey)

            // Tìm các preference cần dùng
            enableFakerSwitch = findPreference("enableFaker")!!
            chooseProfilePref = findPreference("chooseProfile")!!

            // Thiết lập thông tin ứng dụng
            findPreference<Preference>("appInfo")?.let {
                it.icon = PackageHelper.loadAppIcon(packageName).toDrawable(resources)
                it.title = PackageHelper.loadAppLabel(packageName)
                it.summary = packageName
            }
            
            // Thiết lập trạng thái ban đầu và xử lý sự kiện
            setupFakerSwitch()
            setupChooseProfilePreference()
        }

        private fun setupFakerSwitch() {
            enableFakerSwitch.isChecked = appFakerConfig.isEnabled
            
            enableFakerSwitch.setOnPreferenceChangeListener { _, newValue ->
                val isEnabled = newValue as Boolean
                ConfigManager.setFakerEnabled(packageName, isEnabled)
                // Cập nhật lại config cục bộ để UI đồng bộ
                appFakerConfig = ConfigManager.getAppFakerConfig(packageName)
                true // Cho phép thay đổi được lưu
            }
        }

        private fun setupChooseProfilePreference() {
            updateChooseProfileSummary()

            chooseProfilePref.setOnPreferenceClickListener {
                showProfileSelectionDialog()
                true
            }
        }

        private fun updateChooseProfileSummary() {
            val currentProfileName = appFakerConfig.appliedProfileName
            if (currentProfileName.isNullOrEmpty()) {
                chooseProfilePref.summary = getString(R.string.app_no_profile_chosen)
            } else {
                chooseProfilePref.summary = getString(R.string.app_current_profile, currentProfileName)
            }
        }

        private fun showProfileSelectionDialog() {
            val profiles = ConfigManager.getProfiles()
            if (profiles.isEmpty()) {
                Toast.makeText(context, "Chưa có hồ sơ nào được tạo!", Toast.LENGTH_SHORT).show()
                return
            }

            val profileNames = profiles.keys.toTypedArray()
            val currentProfileName = appFakerConfig.appliedProfileName
            var checkedItem = profileNames.indexOf(currentProfileName)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_choose_profile)
                .setSingleChoiceItems(profileNames, checkedItem) { dialog, which ->
                    checkedItem = which // Cập nhật lựa chọn khi người dùng click
                }
                .setPositiveButton(R.string.save) { dialog, _ ->
                    if (checkedItem != -1) {
                        val selectedProfile = profileNames[checkedItem]
                        ConfigManager.setProfileForApp(packageName, selectedProfile)
                        // Cập nhật config cục bộ và UI
                        appFakerConfig = ConfigManager.getAppFakerConfig(packageName)
                        updateChooseProfileSummary()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                // Thêm một nút để bỏ chọn profile
                .setNeutralButton("Bỏ chọn") { _, _ ->
                     ConfigManager.setProfileForApp(packageName, null)
                     appFakerConfig = ConfigManager.getAppFakerConfig(packageName)
                     updateChooseProfileSummary()
                }
                .show()
        }
    }
}