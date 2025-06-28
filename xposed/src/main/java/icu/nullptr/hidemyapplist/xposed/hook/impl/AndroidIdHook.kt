// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/hook/impl/AndroidIdHook.kt

package icu.nullptr.hidemyapplist.xposed.hook.impl

import android.provider.Settings
import com.github.kyuubiran.ezxhelper.utils.*
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import icu.nullptr.hidemyapplist.xposed.CentralService
import icu.nullptr.hidemyapplist.xposed.hook.interfaces.IHookModule
import java.lang.Exception

class AndroidIdHook(private val service: CentralService) : IHookModule {

    private var isActive = false
    private val unhooks = mutableListOf<XC_MethodHook.Unhook>()

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        logD(getModuleName(), "Initializing for ${lpparam.packageName}")
        try {
            // Hook vào Settings.Secure.getString(ContentResolver, String)
            findMethod(Settings.Secure::class.java) {
                name == "getString" &&
                parameterTypes.size == 2 &&
                parameterTypes[0] == android.content.ContentResolver::class.java &&
                parameterTypes[1] == String::class.java
            }.hookBefore { param ->
                // Chỉ chạy hook khi đã được enable
                if (!isActive) return@hookBefore
                
                // Tham số thứ hai (index 1) là key (ví dụ: "android_id")
                val key = param.args[1] as? String
                if (key == Settings.Secure.ANDROID_ID) {
                    val fakedId = service.getFakedValue(lpparam.packageName, "androidId") as? String
                    if (fakedId != null) {
                        param.result = fakedId
                        logD(getModuleName(), "Spoofed ANDROID_ID for ${lpparam.packageName}")
                    }
                }
            }.let { unhooks.add(it) }

            // Hook vào Settings.Secure.getStringForUser(ContentResolver, String, Int)
            // Một số ứng dụng hoặc hệ thống có thể gọi phương thức này
            findMethodOrNull(Settings.Secure::class.java) {
                name == "getStringForUser" &&
                parameterTypes.size == 3 &&
                parameterTypes[0] == android.content.ContentResolver::class.java &&
                parameterTypes[1] == String::class.java &&
                parameterTypes[2] == Int::class.java
            }?.hookBefore { param ->
                if (!isActive) return@hookBefore
                
                val key = param.args[1] as? String
                if (key == Settings.Secure.ANDROID_ID) {
                    val fakedId = service.getFakedValue(lpparam.packageName, "androidId") as? String
                    if (fakedId != null) {
                        param.result = fakedId
                        logD(getModuleName(), "Spoofed ANDROID_ID (for user) for ${lpparam.packageName}")
                    }
                }
            }?.let { unhooks.add(it) }

        } catch (e: Throwable) {
            logE(getModuleName(), "Failed to initialize", e)
            onError(e as Exception)
        }
    }

    override fun enableHook() {
        if (!isActive) {
            isActive = true
            logI(getModuleName(), "Hook enabled")
        }
    }

    override fun disableHook() {
        if (isActive) {
            isActive = false
            logI(getModuleName(), "Hook disabled")
        }
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "AndroidIdHook"

    override fun getDescription(): String = "Hooks Settings.Secure to spoof Android ID (SSAID)"

    override fun getPriority(): Int = 90

    override fun onError(error: Exception) {
        logE(getModuleName(), "An error occurred", error)
        disableHook() // Tự động vô hiệu hóa nếu có lỗi nghiêm trọng
    }

    override fun cleanup() {
        unhooks.unhookAll()
        unhooks.clear()
        disableHook()
        logI(getModuleName(), "Cleaned up")
    }
}