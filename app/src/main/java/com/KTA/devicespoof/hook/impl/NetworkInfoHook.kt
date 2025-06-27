package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class NetworkInfoHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("NetworkInfoHook: DeviceInfo set with wifiSsid=${deviceInfo.wifiSsid}, wifiBssid=${deviceInfo.wifiBssid}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (deviceInfo?.wifiSsid.isNullOrBlank() && deviceInfo?.wifiBssid.isNullOrBlank()) {
            Logger.warn("NetworkInfoHook: No Wi-Fi information provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook WifiInfo.getSSID
            if (!deviceInfo?.wifiSsid.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getSSID",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.wifiSsid
                                Logger.log("NetworkInfoHook: Spoofed Wi-Fi SSID to ${deviceInfo?.wifiSsid} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook WifiInfo.getBSSID
            if (!deviceInfo?.wifiBssid.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getBSSID",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.wifiBssid
                                Logger.log("NetworkInfoHook: Spoofed Wi-Fi BSSID to ${deviceInfo?.wifiBssid} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            Logger.log("NetworkInfoHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.wifiSsid.isNullOrBlank() && deviceInfo?.wifiBssid.isNullOrBlank()) {
            Logger.warn("NetworkInfoHook: Cannot enable hook; no Wi-Fi information provided.")
            return
        }
        isActive = true
        Logger.log("NetworkInfoHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("NetworkInfoHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "NetworkInfoHook"

    override fun getDescription(): String = "Spoofs Wi-Fi network information (SSID, BSSID)"

    override fun getPriority(): Int = 85

    override fun onError(error: Exception) {
        Logger.error("NetworkInfoHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("NetworkInfoHook: Cleaned up")
    }
}