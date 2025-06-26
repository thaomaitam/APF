package com.KTA.devicespoof.hook.impl

import android.os.Build
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.config.DeviceConfig
import com.KTA.devicespoof.utils.Logger

class BuildHook : IHookModule {
    
    private var isActive = false
    private val fieldHooks = mutableMapOf<String, Any>()
    
    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        Logger.log("Initializing Build properties hook...")
        
        try {
            hookBuildFields()
            enableHook()
            Logger.log("Build properties hook initialized successfully")
            
        } catch (e: Exception) {
            Logger.error("Failed to initialize Build properties hook", e)
            onError(e)
        }
    }
    
    private fun hookBuildFields() {
        // Hook Build class static fields
        hookBuildField("MODEL", "ro.product.model")
        hookBuildField("BRAND", "ro.product.brand")
        hookBuildField("DEVICE", "ro.product.device")
        hookBuildField("PRODUCT", "ro.product.name")
        hookBuildField("MANUFACTURER", "ro.product.manufacturer")
        hookBuildField("FINGERPRINT", "ro.build.fingerprint")
        hookBuildField("ID", "ro.build.id")
        hookBuildField("TYPE", "ro.build.type")
        hookBuildField("TAGS", "ro.build.tags")
        hookBuildField("BOARD", "ro.build.board")
        hookBuildField("HARDWARE", "ro.build.hardware")
        hookBuildField("DISPLAY", "ro.build.display.id")
        hookBuildField("HOST", "ro.build.host")
        hookBuildField("USER", "ro.build.user")
        
        // Hook Build.VERSION fields
        hookVersionField("RELEASE", "ro.build.version.release")
        hookVersionField("INCREMENTAL", "ro.build.version.incremental")
        hookVersionField("CODENAME", "ro.build.version.codename")
        hookVersionField("SECURITY_PATCH", "ro.build.version.security_patch")
        hookVersionField("SDK", "ro.build.version.sdk")
        
        Logger.log("Hooked ${fieldHooks.size} Build fields")
    }
    
    private fun hookBuildField(fieldName: String, propertyKey: String) {
        try {
            val spoofedValue = DeviceConfig.getSpoofedProperty(propertyKey)
            if (spoofedValue != null) {
                XposedHelpers.setStaticObjectField(Build::class.java, fieldName, spoofedValue)
                fieldHooks[fieldName] = spoofedValue
                Logger.log("Spoofed Build.$fieldName = $spoofedValue")
            }
        } catch (e: Exception) {
            Logger.error("Failed to hook Build.$fieldName", e)
        }
    }
    
    private fun hookVersionField(fieldName: String, propertyKey: String) {
        try {
            val spoofedValue = DeviceConfig.getSpoofedProperty(propertyKey)
            if (spoofedValue != null) {
                when (fieldName) {
                    "SDK" -> {
                        try {
                            val sdkInt = spoofedValue.toInt()
                            XposedHelpers.setStaticIntField(Build.VERSION::class.java, "SDK_INT", sdkInt)
                            XposedHelpers.setStaticObjectField(Build.VERSION::class.java, "SDK", spoofedValue)
                            fieldHooks["VERSION.$fieldName"] = spoofedValue
                            Logger.log("Spoofed Build.VERSION.$fieldName = $spoofedValue")
                        } catch (e: NumberFormatException) {
                            Logger.error("Invalid SDK version: $spoofedValue", e)
                        }
                    }
                    else -> {
                        XposedHelpers.setStaticObjectField(Build.VERSION::class.java, fieldName, spoofedValue)
                        fieldHooks["VERSION.$fieldName"] = spoofedValue
                        Logger.log("Spoofed Build.VERSION.$fieldName = $spoofedValue")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error("Failed to hook Build.VERSION.$fieldName", e)
        }
    }
    
    override fun enableHook() {
        isActive = true
        Logger.log("Build properties hook enabled")
    }
    
    override fun disableHook() {
        isActive = false
        Logger.log("Build properties hook disabled")
    }
    
    override fun isHookActive(): Boolean = isActive
    
    override fun getModuleName(): String = "BuildHook"
    
    override fun getDescription(): String = "Hooks Build class static fields to spoof device information"
    
    override fun getPriority(): Int = 80
    
    override fun onError(error: Exception) {
        Logger.error("Build properties hook encountered an error", error)
        isActive = false
    }
    
    override fun cleanup() {
        fieldHooks.clear()
        isActive = false
        Logger.log("Build properties hook cleaned up")
    }
}