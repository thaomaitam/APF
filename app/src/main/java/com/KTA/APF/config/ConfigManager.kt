package com.KTA.APF.config

import android.content.Context
import android.content.SharedPreferences
import com.KTA.APF.util.AppLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ConfigManager {

    private lateinit var prefs: SharedPreferences
    private lateinit var currentConfig: MasterConfig

    // Định dạng Json để có thể in ra đẹp mắt khi debug
    private val jsonFormatter = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /**
     * Phải được gọi một lần trong MyApp.onCreate() để khởi tạo SharedPreferences.
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadConfig()
    }

    /**
     * Tải cấu hình từ SharedPreferences. Nếu không có hoặc bị lỗi, tạo cấu hình mặc định.
     */
    private fun loadConfig() {
        val jsonString = prefs.getString(CONFIG_KEY, null)
        currentConfig = if (jsonString != null) {
            try {
                jsonFormatter.decodeFromString<MasterConfig>(jsonString)
            } catch (e: Exception) {
                AppLogger.e("Failed to parse config, creating default.", e)
                createDefaultConfig()
            }
        } else {
            createDefaultConfig()
        }
    }

    /**
     * Lưu cấu hình hiện tại vào SharedPreferences.
     */
    private fun saveConfig() {
        try {
            val jsonString = jsonFormatter.encodeToString(currentConfig)
            prefs.edit().putString(CONFIG_KEY, jsonString).apply()
            AppLogger.d("Config saved successfully.")
        } catch (e: Exception) {
            AppLogger.e("Failed to save config.", e)
        }
    }

    /**
     * Trả về cấu hình hiện tại.
     */
    fun getConfig(): MasterConfig {
        return currentConfig
    }

    /**
     * Thêm một quy tắc mới hoặc cập nhật một quy tắc đã có.
     * @param rule Quy tắc cần thêm/cập nhật.
     */
    fun addOrUpdateRule(rule: Rule) {
        val mutableRules = currentConfig.rules.toMutableList()
        val existingRuleIndex = mutableRules.indexOfFirst { it.targetPackage == rule.targetPackage }

        if (existingRuleIndex != -1) {
            // Cập nhật quy tắc đã có
            mutableRules[existingRuleIndex] = rule
        } else {
            // Thêm quy tắc mới
            mutableRules.add(rule)
        }
        currentConfig.rules = mutableRules
        saveConfig()
    }

    /**
     * Xóa một quy tắc dựa trên package name.
     * @param targetPackage Tên package của quy tắc cần xóa.
     */
    fun removeRule(targetPackage: String) {
        val mutableRules = currentConfig.rules.toMutableList()
        mutableRules.removeAll { it.targetPackage == targetPackage }
        currentConfig.rules = mutableRules
        saveConfig()
    }
    
    /**
     * Tạo một bộ cấu hình mặc định với một vài profile thiết bị.
     * @return Một đối tượng MasterConfig mặc định.
     */
    private fun createDefaultConfig(): MasterConfig {
        val pixel6 = DeviceProfile(
            name = "Google Pixel 6",
            model = "Pixel 6",
            brand = "google",
            manufacturer = "Google",
            device = "oriole",
            product = "oriole",
            fingerprint = "google/oriole/oriole:13/TQ2A.230505.002/9891397:user/release-keys",
            buildId = "TQ2A.230505.002",
            buildType = "user",
            buildTags = "release-keys",
            release = "13",
            sdk = "33",
            sdkInt = 33,
            androidId = "apf_pixel_6_android_id"
        )
        val pixel7Pro = DeviceProfile(
            name = "Google Pixel 7 Pro",
            model = "Pixel 7 Pro",
            brand = "google",
            manufacturer = "Google",
            device = "cheetah",
            product = "cheetah",
            fingerprint = "google/cheetah/cheetah:14/UPB2.230407.014/10011341:user/release-keys",
            buildId = "UPB2.230407.014",
            buildType = "user",
            buildTags = "release-keys",
            release = "14",
            sdk = "34",
            sdkInt = 34,
            androidId = "apf_pixel_7_pro_android_id"
        )

        val config = MasterConfig(
            profiles = mapOf(pixel6.name to pixel6, pixel7Pro.name to pixel7Pro),
            rules = emptyList() // Ban đầu không có quy tắc nào
        )
        // Lưu ngay cấu hình mặc định này vào SharedPreferences
        val jsonString = jsonFormatter.encodeToString(config)
        prefs.edit().putString(CONFIG_KEY, jsonString).apply()
        return config
    }
}