package com.KTA.devicespoof.hook.impl

import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class BuildHook : IHookModule {

    private var deviceInfo: DeviceInfo? = null
    private var isActive = false
    private lateinit var staticFields: Map<String, String>

    override fun setDeviceInfo(deviceInfo: DeviceInfo) {
        this.deviceInfo = deviceInfo
        Logger.log("BuildHook: DeviceInfo set")
        staticFields = buildMap {
            deviceInfo.buildFingerprint.takeIf { !it.isNullOrBlank() }?.let { put("FINGERPRINT", it) }
            deviceInfo.buildDevice.takeIf { !it.isNullOrBlank() }?.let { put("DEVICE", it) }
            deviceInfo.buildProduct.takeIf { !it.isNullOrBlank() }?.let { put("PRODUCT", it) }
            deviceInfo.buildBrand.takeIf { !it.isNullOrBlank() }?.let { put("BRAND", it) }
            deviceInfo.buildHardware.takeIf { !it.isNullOrBlank() }?.let { put("HARDWARE", it) }
            deviceInfo.buildBoard.takeIf { !it.isNullOrBlank() }?.let { put("BOARD", it) }
            deviceInfo.buildId.takeIf { !it.isNullOrBlank() }?.let { put("ID", it) }
            deviceInfo.buildDisplay.takeIf { !it.isNullOrBlank() }?.let { put("DISPLAY", it) }
            deviceInfo.buildType.takeIf { !it.isNullOrBlank() }?.let { put("TYPE", it) }
            deviceInfo.buildTags.takeIf { !it.isNullOrBlank() }?.let { put("TAGS", it) }
            deviceInfo.buildVersionRelease.takeIf { !it.isNullOrBlank() }?.let { put("VERSION.RELEASE", it) }
            deviceInfo.buildVersionIncremental.takeIf { !it.isNullOrBlank() }?.let { put("VERSION.INCREMENTAL", it) }
            deviceInfo.buildVersionCodename.takeIf { !it.isNullOrBlank() }?.let { put("VERSION.CODENAME", it) }
            deviceInfo.buildVersionSecurityPatch.takeIf { !it.isNullOrBlank() }?.let { put("VERSION.SECURITY_PATCH", it) }
        }
        Logger.log("BuildHook: Prepared ${staticFields.size} static fields to spoof.")
    }

    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (!this::staticFields.isInitialized || staticFields.isEmpty()) {
            Logger.warn("BuildHook: No valid Build fields provided in DeviceInfo. Skipping initialization.")
            return
        }

        try {
            staticFields.forEach { (fieldName, value) ->
                XposedHelpers.setStaticObjectField(
                    android.os.Build::class.java,
                    fieldName,
                    value
                )
                Logger.log("BuildHook: Set Build.$fieldName to $value for ${lpparam.packageName}")
            }
            isActive = true
            Logger.log("BuildHook: Initialized for ${lpparam.packageName}")
        } catch (e: Exception) {
            onError(e)
        }
    }

    override fun enableHook() {
        if (!this::staticFields.isInitialized || staticFields.isEmpty()) {
            Logger.warn("BuildHook: Cannot enable hook; no valid Build fields provided.")
            return
        }
        isActive = true
        Logger.log("BuildHook: Enabled")
    }

    override fun disableHook() {
        isActive = false
        Logger.log("BuildHook: Disabled")
    }

    override fun isHookActive(): Boolean = isActive

    override fun getModuleName(): String = "BuildHook"

    override fun getDescription(): String = "Spoofs Android Build class fields (e.g., FINGERPRINT, DEVICE)"

    override fun getPriority(): Int = 90

    override fun onError(error: Exception) {
        Logger.error("BuildHook: Error occurred: ${error.message}", error)
        isActive = false
    }

    override fun cleanup() {
        isActive = false
        deviceInfo = null
        if (this::staticFields.isInitialized) {
            staticFields = emptyMap()
        }
        Logger.log("BuildHook: Cleaned up")
    }
}