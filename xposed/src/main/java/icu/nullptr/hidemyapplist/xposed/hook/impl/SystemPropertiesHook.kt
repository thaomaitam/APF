// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/hook/impl/SystemPropertiesHook.kt

package icu.nullptr.hidemyapplist.xposed.hook.impl

import com.github.kyuubiran.ezxhelper.utils.*
import icu.nullptr.hidemyapplist.xposed.logD
import icu.nullptr.hidemyapplist.xposed.logE
import icu.nullptr.hidemyapplist.xposed.logI
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import icu.nullptr.hidemyapplist.xposed.CentralService
import icu.nullptr.hidemyapplist.xposed.hook.interfaces.IHookModule
import java.lang.Exception

class SystemPropertiesHook(private val service: CentralService) : IHookModule {
    
    private var isActive = false
    private val unhooks = mutableListOf<XC_MethodHook.Unhook>()

    // Map từ thuộc tính build sang key trong profile
    // Dùng để đối chiếu, vì SystemProperties dùng key như "ro.build.fingerprint"
    // còn BuildHook dùng key "buildFingerprint"
    private val propertyToProfileKeyMap = mapOf(
        "ro.build.fingerprint" to "buildFingerprint",
        "ro.product.model" to "buildModel",
        "ro.product.manufacturer" to "buildManufacturer",
        "ro.product.brand" to "buildBrand",
        "ro.product.device" to "buildDevice",
        "ro.product.name" to "buildProduct",
        "ro.build.id" to "buildId",
        "ro.build.display.id" to "buildDisplay",
        "ro.board.platform" to "buildBoard", // thường giống ro.product.board
        "ro.product.board" to "buildBoard",
        "ro.build.host" to "buildHost",
        "ro.build.tags" to "buildTags",
        "ro.build.type" to "buildType",
        "ro.build.user" to "buildUser",
        "ro.build.version.release" to "buildVersionRelease",
        "ro.build.version.sdk" to "buildVersionSdk",
        "ro.build.version.incremental" to "buildVersionIncremental",
        "ro.build.version.codename" to "buildVersionCodename",
        "ro.build.version.security_patch" to "buildVersionSecurityPatch"
        // Thêm các thuộc tính khác bạn muốn giả mạo ở đây
    )

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        logD(getModuleName(), "Initializing for ${lpparam.packageName}")
        
        try {
            val systemPropertiesClass = "android.os.SystemProperties"

            // Hook ::get(String) và ::get(String, String)
            findAllMethods(systemPropertiesClass) {
                name == "get" && (paramCount == 1 || paramCount == 2) && parameterTypes[0] == String::class.java
            }.hookBefore { param ->
                if (!isActive) return@hookBefore
                handleGetString(lpparam.packageName, param)
            }.also { unhooks.addAll(it) }

            // Hook ::getInt(String, Int)
            findMethodOrNull(systemPropertiesClass) {
                name == "getInt" && paramCount == 2
            }?.hookBefore { param ->
                if (!isActive) return@hookBefore
                handleGetInt(lpparam.packageName, param)
            }?.let { unhooks.add(it) }

            // Hook ::getLong(String, Long)
            findMethodOrNull(systemPropertiesClass) {
                name == "getLong" && paramCount == 2
            }?.hookBefore { param ->
                if (!isActive) return@hookBefore
                handleGetLong(lpparam.packageName, param)
            }?.let { unhooks.add(it) }

            // Hook ::getBoolean(String, Boolean)
            findMethodOrNull(systemPropertiesClass) {
                name == "getBoolean" && paramCount == 2
            }?.hookBefore { param ->
                if (!isActive) return@hookBefore
                handleGetBoolean(lpparam.packageName, param)
            }?.let { unhooks.add(it) }

        } catch (e: Throwable) {
            logE(getModuleName(), "Failed to initialize", e)
            onError(e as Exception)
        }
    }

    private fun handleGetString(caller: String, param: XC_MethodHook.MethodHookParam) {
        val key = param.args[0] as? String ?: return
        val profileKey = propertyToProfileKeyMap[key] ?: return // Chỉ xử lý các key đã định nghĩa

        val spoofedValue = service.getFakedValue(caller, profileKey) as? String
        if (spoofedValue != null) {
            param.result = spoofedValue
            logD(getModuleName(), "Spoofed SystemProperties.get($key) for $caller")
        }
    }

    private fun handleGetInt(caller: String, param: XC_MethodHook.MethodHookParam) {
        val key = param.args[0] as? String ?: return
        val profileKey = propertyToProfileKeyMap[key] ?: return

        val spoofedValue = service.getFakedValue(caller, profileKey)
        if (spoofedValue != null) {
            // SDK là một số nguyên
            val intValue = (spoofedValue as? Int) ?: (spoofedValue.toString().toIntOrNull())
            if (intValue != null) {
                param.result = intValue
                logD(getModuleName(), "Spoofed SystemProperties.getInt($key) for $caller")
            }
        }
    }

    private fun handleGetLong(caller: String, param: XC_MethodHook.MethodHookParam) {
        // Tương tự handleGetInt nếu có thuộc tính long cần giả mạo
    }

    private fun handleGetBoolean(caller: String, param: XC_MethodHook.MethodHookParam) {
        // Tương tự handleGetInt nếu có thuộc tính boolean cần giả mạo
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

    override fun getModuleName(): String = "SystemPropertiesHook"

    override fun getDescription(): String = "Hooks SystemProperties class to spoof ro.* properties"

    override fun getPriority(): Int = 80 // Chạy sau BuildHook để đảm bảo tính nhất quán

    override fun onError(error: Exception) {
        logE(getModuleName(), "An error occurred", error)
        disableHook()
    }

    override fun cleanup() {
        unhooks.unhookAll()
        unhooks.clear()
        disableHook()
        logI(getModuleName(), "Cleaned up")
    }
}