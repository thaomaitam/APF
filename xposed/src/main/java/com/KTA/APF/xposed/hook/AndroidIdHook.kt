package com.KTA.APF.xposed.hook

import android.provider.Settings
import com.KTA.APF.config.DeviceProfile
import com.KTA.APF.xposed.util.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class AndroidIdHook {

    /**
     * Áp dụng hook vào lớp Settings.Secure để giả mạo ANDROID_ID.
     * @param classLoader ClassLoader của ứng dụng mục tiêu.
     * @param profile Thông tin thiết bị cần giả mạo, chứa giá trị androidId.
     */
    fun apply(classLoader: ClassLoader, profile: DeviceProfile) {
        // Chỉ áp dụng hook nếu androidId trong profile được định nghĩa
        if (profile.androidId.isBlank()) {
            return
        }
        
        Logger.log("Applying AndroidIdHook...")

        try {
            val settingsSecureClass = XposedHelpers.findClass("android.provider.Settings.Secure", classLoader)

            // Hook vào phương thức getString(resolver, name)
            // Đây là cách phổ biến nhất để lấy ANDROID_ID
            XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(
                    settingsSecureClass,
                    "getString",
                    "android.content.ContentResolver",
                    String::class.java
                ),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[1] as? String
                        // Chỉ can thiệp nếu ứng dụng đang hỏi đúng ANDROID_ID
                        if (key == Settings.Secure.ANDROID_ID) {
                            param.result = profile.androidId
                            Logger.log("Spoofed Settings.Secure.getString(ANDROID_ID) -> ${profile.androidId}")
                        }
                    }
                }
            )

            // Một số hệ thống hoặc ứng dụng có thể gọi phương thức getInt, getLong
            // với một key khác để lấy Android ID. Nếu cần, bạn có thể thêm hook ở đây.
            // Tuy nhiên, getString là phổ biến nhất.

            Logger.log("AndroidIdHook applied successfully.")
        } catch (e: Exception) {
            Logger.error("Failed to apply AndroidIdHook", e)
        }
    }
}