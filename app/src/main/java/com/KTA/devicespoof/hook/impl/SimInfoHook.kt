package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SimInfoHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("SimInfoHook: DeviceInfo set with simSerial=${deviceInfo.simSerial}, mobileNumber=${deviceInfo.mobileNumber}, simOperator=${deviceInfo.simOperator}, simSubscriberId=${deviceInfo.simSubscriberId}, simCountry=${deviceInfo.simCountry}, simMnc=${deviceInfo.simMnc}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (deviceInfo?.simSerial.isNullOrBlank() && 
            deviceInfo?.mobileNumber.isNullOrBlank() && 
            deviceInfo?.simOperator.isNullOrBlank() && 
            deviceInfo?.simSubscriberId.isNullOrBlank() && 
            deviceInfo?.simCountry.isNullOrBlank() && 
            deviceInfo?.simMnc.isNullOrBlank()) {
            Logger.warn("SimInfoHook: No SIM information provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook TelephonyManager.getSimSerialNumber
            if (!deviceInfo?.simSerial.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSimSerialNumber",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.simSerial
                                Logger.log("SimInfoHook: Spoofed SIM serial to ${deviceInfo?.simSerial} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook TelephonyManager.getLine1Number
            if (!deviceInfo?.mobileNumber.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getLine1Number",
                    object : XC_MethodHook() {
|                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.mobileNumber
                                Logger.log("SimInfoHook: Spoofed mobile number to ${deviceInfo?.mobileNumber} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook TelephonyManager.getSimOperatorName
            if (!deviceInfo?.simOperator.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSimOperatorName",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.simOperator
                                Logger.log("SimInfoHook: Spoofed SIM operator name to ${deviceInfo?.simOperator} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook TelephonyManager.getSubscriberId
            if (!deviceInfo?.simSubscriberId.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSubscriberId",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.simSubscriberId
                                Logger.log("SimInfoHook: Spoofed subscriber ID to ${deviceInfo?.simSubscriberId} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook TelephonyManager.getSimCountryIso
            if (!deviceInfo?.simCountry.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSimCountryIso",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                param.result = deviceInfo?.simCountry
                                Logger.log("SimInfoHook: Spoofed SIM country to ${deviceInfo?.simCountry} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook TelephonyManager.getSimOperator (returns MCC+MNC)
            if (!deviceInfo?.simMnc.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSimOperator",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                val simMnc = deviceInfo?.simMnc
                                // Check if simMnc is a full MCC+MNC or just MNC
                                val spoofedOperator = if (simMnc?.matches(Regex("\\d{5,6}")) == true) {
                                    // Assume simMnc is a full MCC+MNC (e.g., "310260")
                                    simMnc
                                } else {
                                    // Combine with a default MCC (e.g., "310" for US)
                                    val defaultMcc = "310"
                                    if (simMnc?.matches(Regex("\\d{2,3}")) == true) {
                                        "$defaultMcc$simMnc"
                                    } else {
                                        Logger.warn("SimInfoHook: Invalid simMnc format '$simMnc' for ${lpparam.packageName}. Skipping.")
                                        return
                                    }
                                }
                                param.result = spoofedOperator
                                Logger.log("SimInfoHook: Spoofed SIM operator MCC+MNC to $spoofedOperator for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            Logger.log("SimInfoHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.simSerial.isNullOrBlank() && 
            deviceInfo?.mobileNumber.isNullOrBlank() && 
            deviceInfo?.simOperator.isNullOrBlank() && 
            deviceInfo?.simSubscriberId.isNullOrBlank() && 
            deviceInfo?.simCountry.isNullOrBlank() && 
            deviceInfo?.simMnc.isNullOrBlank()) {
            Logger.warn("SimInfoHook: Cannot enable hook; no SIM information provided.")
            return
        }
        isActive = true
        Logger.log("SimInfoHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("SimInfoHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "SimInfoHook"

    override fun getDescription(): String = "Spoofs SIM-related information (e.g., SIM serial, mobile number, operator, subscriber ID, country, MNC)"

    override fun getPriority(): Int = 90

    override fun onError(error: Exception) {
        Logger.error("SimInfoHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("SimInfoHook: Cleaned up")
    }
}