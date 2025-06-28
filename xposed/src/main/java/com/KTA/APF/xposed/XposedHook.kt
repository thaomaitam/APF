package com.KTA.APF.xposed

import com.KTA.APF.config.MasterConfig
import com.KTA.APF.xposed.hook.AndroidIdHook
import com.KTA.APF.xposed.hook.BuildHook
import com.KTA.APF.xposed.hook.SystemPropertiesHook
import com.KTA.APF.xposed.util.Logger
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlinx.serialization.json.Json

class XposedHook : IXposedHookLoadPackage {

    // Khởi tạo các module hook, chúng sẽ được tái sử dụng
    private val buildHook = BuildHook()
    private val systemPropertiesHook = SystemPropertiesHook()
    private val androidIdHook = AndroidIdHook()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Chỉ thực hiện hook nếu package name không phải là của chính ứng dụng APF
        // để tránh vòng lặp hoặc các hành vi không mong muốn.
        if (lpparam.packageName == "com.KTA.APF") {
            return
        }

        // 1. Đọc cấu hình từ SharedPreferences
        // Cờ "xposedsharedprefs" trong AndroidManifest cho phép Xposed đọc file này.
        val prefs = XSharedPreferences("com.KTA.APF", "app_prefs")
        
        // Đôi khi file prefs chưa sẵn sàng ngay lập tức, reload() để đảm bảo đọc được dữ liệu mới nhất.
        prefs.reload()
        
        val configJson = prefs.getString("master_config", null)

        // Nếu không có cấu hình, không làm gì cả.
        if (configJson == null) {
            // Có thể log ra để debug nếu cần: Logger.log("No config found.")
            return
        }

        try {
            // 2. Parse chuỗi JSON thành đối tượng MasterConfig
            val masterConfig = Json { ignoreUnknownKeys = true }.decodeFromString<MasterConfig>(configJson)

            // 3. Tìm quy tắc (rule) cho ứng dụng hiện tại (lpparam.packageName)
            val rule = masterConfig.rules.find { it.targetPackage == lpparam.packageName }
            if (rule == null) {
                // Không có quy tắc nào được đặt cho ứng dụng này, thoát.
                return
            }

            // 4. Lấy thông tin profile thiết bị tương ứng từ quy tắc
            val profile = masterConfig.profiles[rule.profileName]
            if (profile == null) {
                Logger.error("Profile '${rule.profileName}' not found for package ${lpparam.packageName}")
                return
            }

            // Nếu mọi thứ hợp lệ, bắt đầu quá trình hook
            Logger.log("Rule found for ${lpparam.packageName}. Spoofing as '${rule.profileName}'.")

            // 5. Áp dụng từng hook với profile đã chọn
            buildHook.apply(lpparam.classLoader, profile)
            systemPropertiesHook.apply(lpparam.classLoader, profile)
            androidIdHook.apply(lpparam.classLoader, profile)

        } catch (e: Exception) {
            Logger.error("Failed to parse or apply config for ${lpparam.packageName}", e)
        }
    }
}