package com.KTA.APF.xposed.hook

import android.os.Build
import com.KTA.APF.config.DeviceProfile
import com.KTA.APF.xposed.util.Logger
import de.robv.android.xposed.XposedHelpers

class BuildHook {
    /**
     * Áp dụng các thay đổi vào lớp Build.
     * @param classLoader ClassLoader của ứng dụng mục tiêu.
     * @param profile Thông tin thiết bị cần giả mạo.
     */
    fun apply(classLoader: ClassLoader, profile: DeviceProfile) {
        Logger.log("Applying BuildHook...")

        try {
            // Lấy các lớp Build và Build.VERSION bằng classLoader của ứng dụng mục tiêu
            val buildClass = XposedHelpers.findClass("android.os.Build", classLoader)
            val buildVersionClass = XposedHelpers.findClass("android.os.Build.VERSION", classLoader)

            // Giả mạo các trường trong lớp android.os.Build
            setStaticField(buildClass, "MODEL", profile.model)
            setStaticField(buildClass, "BRAND", profile.brand)
            setStaticField(buildClass, "MANUFACTURER", profile.manufacturer)
            setStaticField(buildClass, "DEVICE", profile.device)
            setStaticField(buildClass, "PRODUCT", profile.product)
            setStaticField(buildClass, "FINGERPRINT", profile.fingerprint)
            setStaticField(buildClass, "ID", profile.buildId)
            setStaticField(buildClass, "TYPE", profile.buildType)
            setStaticField(buildClass, "TAGS", profile.buildTags)
            
            // Giả mạo các trường trong lớp android.os.Build.VERSION
            setStaticField(buildVersionClass, "RELEASE", profile.release)
            setStaticField(buildVersionClass, "SDK", profile.sdk)
            // SDK_INT là kiểu Int, cần xử lý riêng
            setStaticIntField(buildVersionClass, "SDK_INT", profile.sdkInt)

            Logger.log("BuildHook applied successfully for model: ${profile.model}")
        } catch (e: Exception) {
            Logger.error("Failed to apply BuildHook", e)
        }
    }

    /**
     * Hàm tiện ích để đặt giá trị cho một trường tĩnh kiểu String.
     * @param clazz Lớp chứa trường tĩnh.
     * @param fieldName Tên của trường.
     * @param value Giá trị mới.
     */
    private fun setStaticField(clazz: Class<*>, fieldName: String, value: String?) {
        if (value.isNullOrBlank()) {
            // Bỏ qua nếu giá trị trong profile là rỗng hoặc null
            return
        }
        try {
            XposedHelpers.setStaticObjectField(clazz, fieldName, value)
            Logger.log("Spoofed Build.${clazz.simpleName}.$fieldName -> $value")
        } catch (e: Throwable) {
            // Ghi log lỗi nhưng không làm crash toàn bộ quá trình hook
            Logger.error("Failed to spoof Build.${clazz.simpleName}.$fieldName", e)
        }
    }

    /**
     * Hàm tiện ích để đặt giá trị cho một trường tĩnh kiểu Int.
     * @param clazz Lớp chứa trường tĩnh.
     * @param fieldName Tên của trường.
     * @param value Giá trị mới.
     */
    private fun setStaticIntField(clazz: Class<*>, fieldName: String, value: Int) {
        try {
            XposedHelpers.setStaticIntField(clazz, fieldName, value)
            Logger.log("Spoofed Build.${clazz.simpleName}.$fieldName -> $value")
        } catch (e: Throwable) {
            Logger.error("Failed to spoof Build.${clazz.simpleName}.$fieldName", e)
        }
    }
}