package com.KTA.devicespoof.hook.impl

import android.provider.Settings
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.config.DeviceConfig
import com.KTA.devicespoof.utils.Logger

class AndroidIdHook : IHookModule {
    
    private var isActive = false
    private var settingsSecureHook: XC_MethodHook? = null
    
    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        Logger.log("Initializing Android ID hook...")
        
        try {
            hookSettingsSecure()
            enableHook()
            Logger.log("Android ID hook initialized successfully")
            
        } catch (e: Exception) {
            Logger.error("Failed to initialize Android ID hook", e)
            onError(e)
        }
    }
    
    private fun hookSettingsSecure() {
        settingsSecureHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val name = param.args[1] as? String ?: return
                
                if (name == Settings.Secure.ANDROID_ID) {
                    param.result = DeviceConfig.getAndroidId()
                    Logger.log("Spoofed Settings.Secure.getString(ANDROID_ID) = ${DeviceConfig.getAndroidId()}")
                }
            }
        }
        
        // Hook Settings.Secure.getString method
        XposedHelpers.findAndHookMethod(
            Settings.Secure::class.java,
            "getString",
            android.content.ContentResolver::class.java,
            String::class.java,
            settingsSecureHook
        )
        
        // Also hook the getStringForUser method which might be used internally
        try {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getStringForUser",
                android.content.ContentResolver::class.java,
                String::class.java,
                Int::class.java,
                settingsSecureHook
            )
        } catch (e: Exception) {
            Logger.log("getStringForUser method not found, skipping...")
        }
    }
    
    override fun enableHook() {
        isActive = true
        Logger.log("Android ID hook enabled")
    }
    
    override fun disableHook() {
        isActive = false
        Logger.log("Android ID hook disabled")
    }
    
    override fun isHookActive(): Boolean = isActive
    
    override fun getModuleName(): String = "AndroidIdHook"
    
    override fun getDescription(): String = "Hooks Settings.Secure to spoof Android ID (SSAID)"
    
    override fun getPriority(): Int = 90
    
    override fun onError(error: Exception) {
        Logger.error("Android ID hook encountered an error", error)
        isActive = false
    }
    
    override fun cleanup() {
        settingsSecureHook = null
        isActive = false
        Logger.log("Android ID hook cleaned up")
    }
}