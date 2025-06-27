package com.KTA.devicespoof

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.HookManager
import com.KTA.devicespoof.utils.Logger
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.profile.Profile
import com.KTA.devicespoof.profile.DeviceInfo

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private val hookManager = HookManager()
    private var moduleContext: Context? = null

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            // Create module context using the module's package name and APK path
            val modulePackageName = "com.KTA.devicespoof" // Replace with actual module package name
            val systemContext = XposedHelpers.callMethod(
                XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null),
                    "currentActivityThread"
                ),
                "getSystemContext"
            ) as Context
            moduleContext = systemContext.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)
            ProfileManager.initialize(moduleContext!!)
            Logger.log("MainHook: Module context initialized and ProfileManager initialized.")
        } catch (e: Exception) {
            Logger.error("Failed to initialize module context in initZygote: ${e.message}", e)
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (moduleContext == null) {
            Logger.error("Module context is not initialized. Cannot apply hooks for ${lpparam.packageName}.")
            return
        }

        // Skip system packages or the module itself to avoid conflicts
        if (lpparam.packageName == "android" || lpparam.packageName == "com.KTA.devicespoof") {
            Logger.log("Skipping hooks for system package or module itself: ${lpparam.packageName}")
            return
        }

        // Retrieve the profile for the loaded application
        val appProfile = ProfileManager.getProfileForApp(lpparam.packageName)
        if (appProfile == null || !isValidProfile(appProfile)) {
            Logger.log("No valid profile found for ${lpparam.packageName}. Skipping hooks.")
            return
        }

        Logger.log("Found profile '${appProfile.name}' for ${lpparam.packageName}. Applying hooks...")
        applyHooksWithProfile(lpparam, appProfile.deviceInfo)
    }

    /**
     * Validates if a profile contains sufficient data to apply hooks.
     * @param profile The profile to validate.
     * @return True if the profile is valid, false otherwise.
     */
    private fun isValidProfile(profile: Profile): Boolean {
        val deviceInfo = profile.deviceInfo
        // Consider a profile valid if at least one key field is non-empty
        // Adjust based on which fields are critical for your hooks
        return deviceInfo != null && (
            !deviceInfo.androidId.isNullOrBlank() ||
            !deviceInfo.buildFingerprint.isNullOrBlank() ||
            !deviceInfo.imei1.isNullOrBlank() ||
            !deviceInfo.wifiMac.isNullOrBlank()
        )
    }

    /**
     * Applies hooks using the provided DeviceInfo for the loaded package.
     * @param lpparam LoadPackageParam from Xposed.
     * @param deviceInfo The DeviceInfo from the selected profile.
     */
    private fun applyHooksWithProfile(lpparam: XC_LoadPackage.LoadPackageParam, deviceInfo: DeviceInfo) {
        try {
            // Initialize hooks with DeviceInfo
            hookManager.initializeHooks(lpparam, deviceInfo)
            // Enable all configured hooks
            hookManager.enableAllHooks()
            Logger.log("Successfully applied hooks for ${lpparam.packageName} with profile.")
        } catch (e: Exception) {
            Logger.error("Failed to apply hooks for ${lpparam.packageName}: ${e.message}", e)
        }
    }
}