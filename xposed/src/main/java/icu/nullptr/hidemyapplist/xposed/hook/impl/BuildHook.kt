// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/hook/impl/BuildHook.kt

package icu.nullptr.hidemyapplist.xposed.hook.impl

import android.os.Build
import icu.nullptr.hidemyapplist.xposed.logD
import icu.nullptr.hidemyapplist.xposed.logE
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import icu.nullptr.hidemyapplist.xposed.CentralService
import icu.nullptr.hidemyapplist.xposed.hook.interfaces.IHookModule

class BuildHook(private val service: CentralService) : IHookModule {
    
    private var isActive = false
    
    private val buildFieldToProfileKey = mapOf(
        "FINGERPRINT" to "buildFingerprint",
        "MODEL" to "buildModel",
        "MANUFACTURER" to "buildManufacturer",
        "BRAND" to "buildBrand",
        "DEVICE" to "buildDevice",
        "PRODUCT" to "buildProduct",
        "ID" to "buildId",
        "DISPLAY" to "buildDisplay",
        "BOARD" to "buildBoard",
        "HOST" to "buildHost",
        "TAGS" to "buildTags",
        "TYPE" to "buildType",
        "USER" to "buildUser"
    )

    private val versionFieldToProfileKey = mapOf(
        "RELEASE" to "buildVersionRelease",
        "SDK_INT" to "buildVersionSdk", // Key cho SDK_INT
        "INCREMENTAL" to "buildVersionIncremental",
        "CODENAME" to "buildVersionCodename",
        "SECURITY_PATCH" to "buildVersionSecurityPatch"
    )

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!isActive) return

        val callerPackage = lpparam.packageName

        // Hook các trường của lớp Build
        buildFieldToProfileKey.forEach { (fieldName, profileKey) ->
            val spoofedValue = service.getFakedValue(callerPackage, profileKey) as? String
            if (spoofedValue != null) {
                try {
                    XposedHelpers.setStaticObjectField(Build::class.java, fieldName, spoofedValue)
                    logD(getModuleName(), "Spoofed Build.$fieldName for $callerPackage")
                } catch (t: Throwable) {
                    logE(getModuleName(), "Failed to spoof Build.$fieldName", t)
                }
            }
        }
        
        // Hook các trường của lớp Build.VERSION
        versionFieldToProfileKey.forEach { (fieldName, profileKey) ->
            val spoofedValue = service.getFakedValue(callerPackage, profileKey)
            if (spoofedValue != null) {
                try {
                    if (fieldName == "SDK_INT" && spoofedValue is Int) {
                        XposedHelpers.setStaticIntField(Build.VERSION::class.java, "SDK_INT", spoofedValue)
                        // Cũng cập nhật cả SDK string
                        XposedHelpers.setStaticObjectField(Build.VERSION::class.java, "SDK", spoofedValue.toString())
                    } else if (spoofedValue is String) {
                        XposedHelpers.setStaticObjectField(Build.VERSION::class.java, fieldName, spoofedValue)
                    }
                    logD(getModuleName(), "Spoofed Build.VERSION.$fieldName for $callerPackage")
                } catch (t: Throwable) {
                    logE(getModuleName(), "Failed to spoof Build.VERSION.$fieldName", t)
                }
            }
        }
    }
    
    override fun enableHook() { isActive = true }
    override fun disableHook() { isActive = false }
    override fun isHookActive(): Boolean = isActive
    override fun getModuleName(): String = "BuildHook"
    override fun getDescription(): String = "Hooks Build class static fields to spoof device information"
    override fun getPriority(): Int = 100 // Ưu tiên cao để chạy trước
    override fun onError(error: Exception) { isActive = false }
    override fun cleanup() { isActive = false }
}