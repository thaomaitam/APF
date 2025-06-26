package com.KTA.devicespoof

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.HookManager
import com.KTA.devicespoof.config.DeviceConfig
import com.KTA.devicespoof.utils.Logger

class MainHook : IXposedHookLoadPackage {
    
    companion object {
        private const val TIKTOK_PACKAGE = "com.alphabetlabs.deviceinfo"
        private const val TIKTOK_LITE_PACKAGE = "flar2.devcheck"
    }
    
    private val hookManager = HookManager()
    
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        // Only hook TikTok applications
        if (!isTikTokApp(lpparam.packageName)) {
            return
        }
        
        Logger.log("Device spoof module loaded for: ${lpparam.packageName}")
        
        try {
            // Initialize device configuration
            DeviceConfig.initialize()
            
            // Start hooking process
            hookManager.initializeHooks(lpparam)
            
            Logger.log("All hooks initialized successfully")
            
        } catch (e: Exception) {
            Logger.error("Failed to initialize hooks", e)
        }
    }
    
    private fun isTikTokApp(packageName: String): Boolean {
        return packageName == TIKTOK_PACKAGE || packageName == TIKTOK_LITE_PACKAGE
    }
}