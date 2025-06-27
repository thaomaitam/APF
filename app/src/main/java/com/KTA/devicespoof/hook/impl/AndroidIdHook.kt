package com.KTA.devicespoof.hook.impl

import android.provider.Settings
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AndroidIdHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("AndroidIdHook: DeviceInfo set with android_id=${deviceInfo.androidId}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        val androidIdToSpoof = deviceInfo?.androidId
        if (androidIdToSpoof.isNullOrBlank()) {
            Logger.warn("AndroidIdHook: No android_id provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.provider.Settings.Secure",
                lpparam.classLoader,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val settingName = param.args[1] as? String
                        if (settingName == Settings.Secure.ANDROID_ID && isActive) {
                            param.result = androidIdToSpoof
                            Logger.log("AndroidIdHook: Spoofed android_id to $androidIdToSpoof for ${lpparam.packageName}")
                        }
                    }
                }
            )
            Logger.log("AndroidIdHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.androidId.isNullOrBlank()) {
            Logger.warn("AndroidIdHook: Cannot enable hook; android_id is empty.")
            return
        }
        isActive = true
        Logger.log("AndroidIdHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("AndroidIdHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "AndroidIdHook"

    override fun getDescription(): String = "Spoofs the Android ID (Settings.Secure.ANDROID_ID)"

    override fun getPriority(): Int = 100

    override fun onError(error: Exception) {
        Logger.error("AndroidIdHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("AndroidIdHook: Cleaned up")
    }
}