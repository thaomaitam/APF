package com.KTA.APF.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.KTA.APF.config.ConfigManager
import com.KTA.APF.config.DeviceProfile
import com.KTA.APF.config.Rule
import com.KTA.APF.util.AppLogger

class RuleEditorViewModel(initialTargetPackage: String?) : ViewModel() {

    val isNewRule: Boolean = (initialTargetPackage == null)

    // Dùng LiveData để UI có thể observe và cập nhật tương ứng
    private val _targetPackage = MutableLiveData<String?>(initialTargetPackage)
    val targetPackage: LiveData<String?> = _targetPackage

    private val _selectedProfileName = MutableLiveData<String>()
    val selectedProfileName: LiveData<String> = _selectedProfileName

    init {
        if (isNewRule) {
            // Quy tắc mới: Chọn profile đầu tiên trong danh sách làm mặc định.
            _selectedProfileName.value = getAvailableProfiles().firstOrNull()?.name
            AppLogger.d("New rule mode. Default profile: ${_selectedProfileName.value}")
        } else {
            // Chế độ sửa: Tải quy tắc hiện có từ ConfigManager.
            AppLogger.d("Edit rule mode for package: $initialTargetPackage")
            val existingRule = ConfigManager.getConfig().rules.find { it.targetPackage == initialTargetPackage }
            if (existingRule != null) {
                _selectedProfileName.value = existingRule.profileName
            } else {
                AppLogger.w("Could not find rule for package $initialTargetPackage to edit.")
                _selectedProfileName.value = getAvailableProfiles().firstOrNull()?.name
            }
        }
    }

    fun getAvailableProfiles(): List<DeviceProfile> {
        return ConfigManager.getConfig().profiles.values.toList().sortedBy { it.name }
    }

    fun setTargetPackage(packageName: String) {
        if(isNewRule) {
            _targetPackage.value = packageName
        } else {
            AppLogger.w("Attempted to change target package on an existing rule.")
        }
    }

    fun setSelectedProfile(profileName: String) {
        _selectedProfileName.value = profileName
    }

    fun saveRule() {
        val pkg = _targetPackage.value
        val profile = _selectedProfileName.value

        if (pkg.isNullOrBlank() || profile.isNullOrBlank()) {
            AppLogger.e("Cannot save rule. Package or profile is missing. Pkg: $pkg, Profile: $profile")
            return
        }

        val rule = Rule(targetPackage = pkg, profileName = profile)
        ConfigManager.addOrUpdateRule(rule)
        AppLogger.i("Rule saved: $rule")
    }

    fun deleteRule() {
        val pkg = _targetPackage.value
        if (pkg != null && !isNewRule) {
            ConfigManager.removeRule(pkg)
            AppLogger.i("Rule deleted for package: $pkg")
        } else {
            AppLogger.w("Attempted to delete a new rule or rule with no package.")
        }
    }

    class Factory(private val targetPackage: String?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RuleEditorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RuleEditorViewModel(targetPackage) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}