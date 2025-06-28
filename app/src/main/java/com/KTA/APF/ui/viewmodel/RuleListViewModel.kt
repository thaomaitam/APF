package com.KTA.APF.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KTA.APF.config.ConfigManager
import com.KTA.APF.config.Rule
import com.KTA.APF.util.PackageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.*

class RuleListViewModel : ViewModel() {

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules = _rules.asStateFlow()

    init {
        loadRules()
    }

    /**
     * Tải lại danh sách quy tắc từ ConfigManager và cập nhật StateFlow.
     * Sắp xếp danh sách dựa trên tên hiển thị (label) của ứng dụng.
     */
    fun loadRules() {
        viewModelScope.launch {
            val currentRules = ConfigManager.getConfig().rules
            // Sắp xếp các quy tắc dựa trên label của ứng dụng để hiển thị thân thiện hơn.
            val sortedRules = currentRules.sortedWith(
                compareBy(Collator.getInstance(Locale.getDefault())) { rule ->
                    PackageHelper.getAppInfo(rule.targetPackage)?.label?.lowercase() ?: rule.targetPackage
                }
            )
            _rules.value = sortedRules
        }
    }
}