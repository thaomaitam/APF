package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ImeiHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("ImeiHook: DeviceInfo set with imei1=${deviceInfo.imei1}, imei2=${deviceInfo.imei2}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (deviceInfo?.imei1.isNullOrBlank() && deviceInfo?.imei2.isNullOrBlank()) {
            Logger.warn("ImeiHook: No IMEI provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook TelephonyManager.getImei(int slotIndex)
            XposedHelpers.findAndHookMethod(
                "android.telephony.TelephonyManager",
                lpparam.classLoader,
                "getImei",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val slotIndex = param.args[0] as Int
                            val imei = when (slotIndex) {
                                0 -> deviceInfo?.imei1
                                1 -> deviceInfo?.imei2
                                else -> null
                            }
                            if (!imei.isNullOrBlank()) {
                                param.result = imei
                                Logger.log("ImeiHook: Spoofed IMEI for slot $slotIndex to $imei for ${lpparam.packageName}")
                            }
                        }
                    }
                }
            )

            // Hook TelephonyManager.getDeviceId()
            XposedHelpers.findAndHookMethod(
                "android.telephony.TelephonyManager",
                lpparam.classLoader,
                "getDeviceId",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val imei = deviceInfo?.imei1.takeIf { !it.isNullOrBlank() } ?: deviceInfo?.imei2
                            if (!imei.isNullOrBlank()) {
                                param.result = imei
                                Logger.log("ImeiHook: Spoofed getDeviceId to $imei for ${lpparam.packageName}")
                            }
                        }
                    }
                }
            )

            Logger.log("ImeiHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.imei1.isNullOrBlank() && deviceInfo?.imei2.isNullOrBlank()) {
            Logger.warn("ImeiHook: Cannot enable hook; no IMEI provided.")
            return
        }
        isActive = true
        Logger.log("ImeiHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("ImeiHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "ImeiHook"

    override fun getDescription(): String = "Spoofs IMEI values for TelephonyManager"

    override fun getPriority(): Int = 95

    override fun onError(error: Exception) {
        Logger.error("ImeiHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("ImeiHook: Cleaned up")
    }
}