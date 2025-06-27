package com.KTA.devicespoof.hook.impl

import android.content.ContentResolver
import android.net.Uri
import android.provider.Settings
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class AdditionalIdsHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("AdditionalIdsHook: DeviceInfo set with adsId=${deviceInfo.adsId}, mediaDrmId=${deviceInfo.mediaDrmId}, gsfId=${deviceInfo.gsfId}")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (deviceInfo?.adsId.isNullOrBlank() && 
            deviceInfo?.mediaDrmId.isNullOrBlank() && 
            deviceInfo?.gsfId.isNullOrBlank()) {
            Logger.warn("AdditionalIdsHook: No additional IDs provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            // Hook AdvertisingIdClient.getAdvertisingIdInfo
            if (!deviceInfo?.adsId.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                    lpparam.classLoader,
                    "getAdvertisingIdInfo",
                    android.content.Context::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                val advertisingIdInfoClass = XposedHelpers.findClass(
                                    "com.google.android.gms.ads.identifier.AdvertisingIdInfo",
                                    lpparam.classLoader
                                )
                                val mockAdIdInfo = XposedHelpers.newInstance(
                                    advertisingIdInfoClass,
                                    deviceInfo?.adsId,
                                    false // isLimitAdTrackingEnabled
                                )
                                param.result = mockAdIdInfo
                                Logger.log("AdditionalIdsHook: Spoofed Advertising ID to ${deviceInfo?.adsId} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook MediaDrm.getPropertyString
            if (!deviceInfo?.mediaDrmId.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.media.MediaDrm",
                    lpparam.classLoader,
                    "getPropertyString",
                    String::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive && param.args[0] == "deviceUniqueId") {
                                param.result = deviceInfo?.mediaDrmId
                                Logger.log("AdditionalIdsHook: Spoofed MediaDrm ID to ${deviceInfo?.mediaDrmId} for ${lpparam.packageName}")
                            }
                        }
                    }
                )
            }

            // Hook ContentResolver.query for GSF ID
            if (!deviceInfo?.gsfId.isNullOrBlank()) {
                XposedHelpers.findAndHookMethod(
                    "android.content.ContentResolver",
                    lpparam.classLoader,
                    "query",
                    Uri::class.java,
                    Array<String>::class.java,
                    String::class.java,
                    Array<String>::class.java,
                    String::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive) {
                                val uri = param.args[0] as? Uri
                                val selection = param.args[2] as? String
                                if (uri?.authority == "com.google.android.gsf.gservices" && 
                                    (selection == "android_id" || selection?.contains("android_id") == true)) {
                                    val cursor = XposedHelpers.newInstance(
                                        android.database.MatrixCursor::class.java,
                                        arrayOf("name", "value"),
                                        1
                                    )
                                    XposedHelpers.callMethod(cursor, "addRow", arrayOf("android_id", deviceInfo?.gsfId))
                                    param.result = cursor
                                    Logger.log("AdditionalIdsHook: Spoofed GSF ID to ${deviceInfo?.gsfId} for ${lpparam.packageName} via ContentResolver.query")
                                }
                            }
                        }
                    }
                )

                // Hook Settings.Secure.getString as fallback
                XposedHelpers.findAndHookMethod(
                    "android.provider.Settings.Secure",
                    lpparam.classLoader,
                    "getString",
                    ContentResolver::class.java,
                    String::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if (isActive && param.args[1] == "android_id") {
                                param.result = deviceInfo?.gsfId
                                Logger.log("AdditionalIdsHook: Spoofed Settings.Secure android_id to ${deviceInfo?.gsfId} for ${lpparam.packageName}")
                            }
                        }
                    }
                )

                // Hook InstanceID.getId for GSF ID
                try {
                    XposedHelpers.findAndHookMethod(
                        "com.google.android.gms.iid.InstanceID",
                        lpparam.classLoader,
                        "getId",
                        object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                if (isActive) {
                                    param.result = deviceInfo?.gsfId
                                    Logger.log("AdditionalIdsHook: Spoofed InstanceID.getId to ${deviceInfo?.gsfId} for ${lpparam.packageName}")
                                }
                            }
                        }
                    )
                } catch (e: Exception) {
                    Logger.warn("AdditionalIdsHook: Failed to hook InstanceID.getId", e)
                }
            }

            Logger.log("AdditionalIdsHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (deviceInfo?.adsId.isNullOrBlank() && 
            deviceInfo?.mediaDrmId.isNullOrBlank() && 
            deviceInfo?.gsfId.isNullOrBlank()) {
            Logger.warn("AdditionalIdsHook: Cannot enable hook; no additional IDs provided.")
            return
        }
        isActive = true
        Logger.log("AdditionalIdsHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("AdditionalIdsHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "AdditionalIdsHook"

    override fun getDescription(): String = "Spoofs additional IDs (Advertising ID, MediaDrm ID, GSF ID)"

    override fun getPriority(): Int = 80

    override fun onError(error: Exception) {
        Logger.error("AdditionalIdsHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        Logger.log("AdditionalIdsHook: Cleaned up")
    }
}