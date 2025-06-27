package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MacAddressHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("MacAddressHook: DeviceInfo set with wifiMac=${deviceInfo.wifiMac}, bluetoothMac=${deviceInfo.bluetoothMac}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (deviceInfo?.wifiMac.isNullOrBlank() && deviceInfo?.bluetoothMac.isNullOrBlank()) {
            Logger.warn("MacAddressHook: No MAC addresses provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook WifiInfo.getMacAddress
            if (!deviceInfo?.wifiMac.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getMacAddress",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.wifiMac
                                Logger.log("MacAddressHook: Spoofed Wi-Fi MAC to ${deviceInfo?.wifiMac} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook BluetoothAdapter.getAddress
            if (!deviceInfo?.bluetoothMac.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.bluetooth.BluetoothAdapter",
                    lpparam.classLoader,
                    "getAddress",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.bluetoothMac
                                Logger.log("MacAddressHook: Spoofed Bluetooth MAC to ${deviceInfo?.bluetoothMac} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            Logger.log("MacAddressHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.wifiMac.isNullOrBlank() && deviceInfo?.bluetoothMac.isNullOrBlank()) {
            Logger.warn("MacAddressHook: Cannot enable hook; no MAC addresses provided.")
            return
        }
        isActive = true
        Logger.log("MacAddressHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("MacAddressHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "MacAddressHook"

    override fun getDescription(): String = "Spoofs Wi-Fi and Bluetooth MAC addresses"

    override fun getPriority(): Int = 85

    override fun onError(error: Exception) {
        Logger.error("MacAddressHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("MacAddressHook: Cleaned up")
    }
}