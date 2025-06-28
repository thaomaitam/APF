package com.KTA.APF.ui.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import by.kirich1409.viewbindingdelegate.viewBinding
import com.KTA.APF.R
import com.KTA.APF.databinding.FragmentRuleEditorBinding
import com.KTA.APF.ui.viewmodel.RuleEditorViewModel
import com.KTA.APF.util.AppLogger
import com.KTA.APF.util.PackageHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RuleEditorFragment : Fragment(R.layout.fragment_rule_editor) {
    // Không cần binding ở fragment cha nữa vì mọi thứ nằm trong fragment con
    private val args: RuleEditorFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (savedInstanceState == null) {
            // Tạo và thêm SettingsFragment vào container
            // Truyền bundle chứa packageName qua cho nó
            val settingsFragment = SettingsFragment().apply {
                arguments = bundleOf("targetPackage" to args.targetPackage)
            }
            childFragmentManager.beginTransaction()
                .replace(R.id.settings_container, settingsFragment)
                .commit()
        }
    }

    /**
     * Fragment con này chứa toàn bộ giao diện và logic.
     */
    class SettingsFragment : PreferenceFragmentCompat() {

        // Lấy ViewModel, chia sẻ với fragment cha
        private val viewModel: RuleEditorViewModel by viewModels({ requireParentFragment() }) {
            // Lấy packageName từ arguments để khởi tạo ViewModel
            val targetPackage = arguments?.getString("targetPackage")
            RuleEditorViewModel.Factory(targetPackage)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.rule_editor_preferences, rootKey)
            setupToolbar()
            setupAppSelection()
            setupProfileSelection()
        }
        
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            
            // Lắng nghe kết quả trả về từ TargetAppSelectFragment
            setFragmentResultListener("app_selection_request") { _, bundle ->
                val selectedPackage = bundle.getString("selected_package")
                if (selectedPackage != null) {
                    viewModel.setTargetPackage(selectedPackage)
                    AppLogger.d("Received selected package: $selectedPackage")
                }
            }
        }
        
        private fun setupToolbar() {
            // Thao tác với Toolbar của Activity cha
            val activity = requireActivity() as AppCompatActivity
            activity.supportActionBar?.title = if (viewModel.isNewRule) getString(R.string.new_rule_title) else getString(R.string.edit_rule_title)
            activity.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_rule_editor, menu)
                }

                override fun onPrepareMenu(menu: Menu) {
                    menu.findItem(R.id.action_delete).isVisible = !viewModel.isNewRule
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_save -> {
                            if (viewModel.targetPackage.value == null) {
                                Toast.makeText(context, "Please select an application.", Toast.LENGTH_SHORT).show()
                                return true
                            }
                            viewModel.saveRule()
                            Toast.makeText(context, getString(R.string.rule_saved_toast), Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                            true
                        }
                        R.id.action_delete -> {
                            showDeleteConfirmationDialog()
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }

        private fun showDeleteConfirmationDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_rule_dialog_title))
                .setMessage(getString(R.string.delete_rule_dialog_message))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.delete_dialog_positive_button)) { _, _ ->
                    viewModel.deleteRule()
                    Toast.makeText(context, getString(R.string.rule_deleted_toast), Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .show()
        }

        private fun setupAppSelection() {
            val appPreference: Preference? = findPreference("pref_target_app")
            
            appPreference?.setOnPreferenceClickListener {
                if (viewModel.isNewRule) {
                    findNavController().navigate(R.id.action_ruleEditorFragment_to_targetAppSelectFragment)
                } else {
                    Toast.makeText(context, getString(R.string.pref_toast_cant_change_app), Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            viewModel.targetPackage.observe(viewLifecycleOwner) { pkgName ->
                if (pkgName != null) {
                    val appInfo = PackageHelper.getAppInfo(pkgName)
                    appPreference?.title = appInfo?.label ?: pkgName
                    appPreference?.summary = pkgName
                    appPreference?.icon = requireActivity().packageManager.getApplicationIcon(pkgName)
                } else {
                    appPreference?.title = getString(R.string.pref_title_target_app)
                    appPreference?.summary = getString(R.string.pref_summary_select_app)
                    appPreference?.setIcon(R.drawable.ic_apps)
                }
            }
        }

        private fun setupProfileSelection() {
            val profilePreference: ListPreference? = findPreference("pref_device_profile")
            
            val profiles = viewModel.getAvailableProfiles()
            profilePreference?.entries = profiles.map { it.name }.toTypedArray()
            profilePreference?.entryValues = profiles.map { it.name }.toTypedArray()
            
            viewModel.selectedProfileName.observe(viewLifecycleOwner) { profileName ->
                profilePreference?.value = profileName
                // ListPreference sẽ tự cập nhật summary nếu dùng useSimpleSummaryProvider
            }

            profilePreference?.setOnPreferenceChangeListener { _, newValue ->
                viewModel.setSelectedProfile(newValue.toString())
                true
            }
        }
    }
}