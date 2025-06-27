package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class SystemPropertiesHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false
    private lateinit var propertyMap: Map<String, String>

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("SystemPropertiesHook: DeviceInfo set")
        propertyMap = buildMap {
            deviceInfo.buildFingerprint.takeIf { !it.isNullOrBlank() }?.let { put("ro.build.fingerprint", it) }
            deviceInfo.buildDevice.takeIf { !it.isNullOrBlank() }?.let { put("ro.product.device", it) }
            deviceInfo.buildProduct.takeIf { !it.isNullOrBlank() }?.let { put("ro.product.name", it) }
            deviceInfo.buildBrand.takeIf { !it.isNullOrBlank() }?.let { put("ro.product.brand", it) }
            deviceInfo.buildHardware.takeIf { !it.isNullOrBlank() }?.let { put("ro.hardware", it) }
            deviceInfo.buildBoard.takeIf { !it.isNullOrBlank() }?.let { put("ro.product.board", it) }
            deviceInfo.buildVersionRelease.takeIf { !it.isNullOrBlank() }?.let { put("ro.build.version.release", it) }
        }
        Logger.log("SystemPropertiesHook: Prepared ${propertyMap.size} system properties to spoof.")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!this::propertyMap.isInitialized || propertyMap.isEmpty()) {
            Logger.warn("SystemPropertiesHook: No valid system properties provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook SystemProperties.get(String)
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val key = param.args[0] as String
                            propertyMap[key]?.let {
                                param.result = it
                                Logger.log("SystemPropertiesHook: Spoofed $key to $it for ${lpparam.packageName}")
                            }
                        }
                    }
                }
            )

            // Hook SystemProperties.get(String, String)
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "get",
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val key = param.args[0] as String
                            propertyMap[key]?.let {
                                param.result = it
                                Logger.log("SystemPropertiesHook: Spoofed $key to $it for ${lpparam.packageName}")
                            }
                        }
                    }
                }
            )

            // Hook SystemProperties.getInt
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "getInt",
                String::class.java,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val key = param.args[0] as String
                            propertyMap[key]?.let {
                                try {
                                    param.result = it.toInt()
                                    Logger.log("SystemPropertiesHook: Spoofed $key to $it (int) for ${lpparam.packageName}")
                                } catch (e: NumberFormatException) {
                                    Logger.warn("SystemPropertiesHook: Cannot convert $key value '$it' to int for ${lpparam.packageName}")
                                }
                            }
                        }
                    }
                }
            )

            // Hook SystemProperties.getLong
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "getLong",
                String::class.java,
                Long::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val key = param.args[0] as String
                            propertyMap[key]?.let {
                                try {
                                    param.result = it.toLong()
                                    Logger.log("SystemPropertiesHook: Spoofed $key to $it (long) for ${lpparam.packageName}")
                                } catch (e: NumberFormatException) {
                                    Logger.warn("SystemPropertiesHook: Cannot convert $key value '$it' to long for ${lpparam.packageName}")
                                }
                            }
                        }
                    }
                }
            )

            // Hook SystemProperties.getBoolean
            XposedHelpers.findAndHookMethod(
                "android.os.SystemProperties",
                lpparam.classLoader,
                "getBoolean",
                String::class.java,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (isActive) {
                            val key = param.args[0] as String
                            propertyMap[key]?.let {
                                try {
                                    param.result = it.toBoolean()
                                    Logger.log("SystemPropertiesHook: Spoofed $key to $it (boolean) for ${lpparam.packageName}")
                                } catch (e: Exception) {
                                    Logger.warn("SystemPropertiesHook: Cannot convert $key value '$it' to boolean for ${lpparam.packageName}")
                                }
                            }
                        }
                    }
                }
            )

            Logger.log("SystemPropertiesHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (!this::propertyMap.isInitialized || propertyMap.isEmpty()) {
            Logger.warn("SystemPropertiesHook: Cannot enable hook; no valid system properties provided.")
            return
        }
        isActive = true
        Logger.log("SystemPropertiesHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("SystemPropertiesHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "SystemPropertiesHook"

    override fun getDescription(): String = "Spoofs Android system properties (e.g., ro.build.fingerprint)"

    override fun getPriority(): Int = 80

    override fun onError(error: Exception) {
        Logger.error("SystemPropertiesHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        if (this::propertyMap.isInitialized) {
            propertyMap = emptyMap()
        }
        Logger.log("SystemPropertiesHook: Cleaned up")
    }
}