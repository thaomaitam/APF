package com.KTA.APF.xposed.hook

import com.KTA.APF.config.DeviceProfile
import com.KTA.APF.xposed.util.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

class SystemPropertiesHook {

    // Tạo một map để tra cứu nhanh, hiệu quả hơn là dùng when-case nhiều lần.
    // Map này sẽ được khởi tạo một lần khi hook được áp dụng.
    private lateinit var spoofProps: Map<String, String>

    /**
     * Áp dụng hook vào lớp SystemProperties.
     * @param classLoader ClassLoader của ứng dụng mục tiêu.
     * @param profile Thông tin thiết bị cần giả mạo.
     */
    fun apply(classLoader: ClassLoader, profile: DeviceProfile) {
        Logger.log("Applying SystemPropertiesHook...")

        // Khởi tạo map chứa các giá trị cần giả mạo
        initializeSpoofMap(profile)

        try {
            val systemPropertiesClass = XposedHelpers.findClass("android.os.SystemProperties", classLoader)

            // Hook vào phương thức get(key: String, def: String)
            // Đây là phương thức được gọi nhiều nhất.
            XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(systemPropertiesClass, "get", String::class.java, String::class.java),
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args[0] as? String ?: return

                        // Kiểm tra xem key có trong danh sách cần giả mạo không
                        if (spoofProps.containsKey(key)) {
                            val spoofedValue = spoofProps[key]
                            // Ghi đè kết quả trả về của phương thức
                            param.result = spoofedValue
                            Logger.log("Spoofed SystemProperties.get('$key') -> '$spoofedValue'")
                        }
                    }
                }
            )

            // Cũng có thể hook các phương thức getInt, getLong, getBoolean nếu cần,
            // nhưng hầu hết các app đều gọi get(String) rồi tự chuyển đổi kiểu.
            // Hook vào get(String) thường là đủ.

            Logger.log("SystemProperties.get(String, String) hooked successfully.")
        } catch (e: Exception) {
            Logger.error("Failed to apply SystemPropertiesHook", e)
        }
    }

    /**
     * Khởi tạo map từ đối tượng DeviceProfile.
     * Chỉ những giá trị không rỗng mới được thêm vào map.
     */
    private fun initializeSpoofMap(profile: DeviceProfile) {
        val map = mutableMapOf<String, String>()
        
        // Build properties
        addIfNotEmpty(map, "ro.product.model", profile.model)
        addIfNotEmpty(map, "ro.product.brand", profile.brand)
        addIfNotEmpty(map, "ro.product.manufacturer", profile.manufacturer)
        addIfNotEmpty(map, "ro.product.device", profile.device)
        addIfNotEmpty(map, "ro.product.name", profile.product)
        addIfNotEmpty(map, "ro.build.fingerprint", profile.fingerprint)
        addIfNotEmpty(map, "ro.build.id", profile.buildId)
        addIfNotEmpty(map, "ro.build.type", profile.buildType)
        addIfNotEmpty(map, "ro.build.tags", profile.buildTags)
        
        // Version properties
        addIfNotEmpty(map, "ro.build.version.release", profile.release)
        addIfNotEmpty(map, "ro.build.version.sdk", profile.sdk)

        // TODO: Thêm các thuộc tính khác từ DeviceProfile nếu bạn mở rộng nó
        // Ví dụ: security_patch, first_api_level,...
        // addIfNotEmpty(map, "ro.build.version.security_patch", profile.securityPatch)

        spoofProps = map
    }
    
    /**
     * Hàm tiện ích để thêm vào map nếu giá trị không rỗng.
     */
    private fun addIfNotEmpty(map: MutableMap<String, String>, key: String, value: String?) {
        if (!value.isNullOrBlank()) {
            map[key] = value
        }
    }
}