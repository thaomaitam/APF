package com.KTA.devicespoof

import android.content.Context
import com.KTA.devicespoof.hook.HookManager
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.profile.Profile
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private lateinit var profileManager: ProfileManager
    private val hookManager = HookManager()

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            // Lấy context của module một cách an toàn
            val modulePackageName = "com.KTA.devicespoof"
            val systemContext = XposedHelpers.callStaticMethod(
                XposedHelpers.findClass("android.app.ActivityThread", null),
                "currentActivityThread"
            ) as? Context
            val moduleContext = systemContext?.createPackageContext(modulePackageName, Context.CONTEXT_INCLUDE_CODE or Context.CONTEXT_IGNORE_SECURITY)

            if (moduleContext != null) {
                profileManager = ProfileManager(moduleContext)
                Logger.log("MainHook: ProfileManager initialized in Zygote.")
            } else {
                Logger.error("MainHook: Failed to create module context in Zygote.")
            }
        } catch (e: Exception) {
            Logger.error("Failed to initialize in initZygote: ${e.message}", e)
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!this::profileManager.isInitialized) {
            Logger.error("ProfileManager not initialized. Cannot apply hooks for ${lpparam.packageName}.")
            return
        }

        // Kiểm tra xem module có được bật toàn cục không
        if (!profileManager.isModuleGloballyEnabled()) {
            Logger.log("Module is disabled globally. Skipping all hooks.")
            return
        }

        if (lpparam.packageName == "android" || lpparam.packageName == "com.KTA.devicespoof") {
            return
        }

        val appProfile = profileManager.getProfileForApp(lpparam.packageName)
        if (appProfile == null) {
            //Logger.log("No profile found for ${lpparam.packageName}. Skipping hooks.")
            return
        }

        Logger.log("Found profile '${appProfile.name}' for ${lpparam.packageName}. Applying hooks...")
        applyHooksWithProfile(lpparam, appProfile.deviceInfo)
    }

    private fun applyHooksWithProfile(lpparam: XC_LoadPackage.LoadPackageParam, deviceInfo: DeviceInfo) {
        try {
            hookManager.initializeHooks(lpparam, deviceInfo)
            hookManager.enableAllHooks()
            Logger.log("Successfully applied hooks for ${lpparam.packageName} with profile.")
        } catch (e: Exception) {
            Logger.error("Failed to apply hooks for ${lpparam.packageName}: ${e.message}", e)
        }
    }
}