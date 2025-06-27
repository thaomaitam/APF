package com.KTA.devicespoof.hook

import com.KTA.devicespoof.hook.impl.AndroidIdHook
import com.KTA.devicespoof.hook.impl.BuildHook
import com.KTA.devicespoof.hook.impl.ImeiHook
import com.KTA.devicespoof.hook.impl.MacAddressHook
import com.KTA.devicespoof.hook.impl.SystemPropertiesHook
import com.KTA.devicespoof.hook.impl.SimInfoHook
import com.KTA.devicespoof.hook.impl.NetworkInfoHook
import com.KTA.devicespoof.hook.impl.AdditionalIdsHook
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.profile.DeviceInfo
import com.KTA.devicespoof.utils.Logger
import de.robv.android.xposed.callbacks.XC_LoadPackage

class HookManager {

    private val hookModules: MutableList<IHookModule> = mutableListOf()

    /**
     * Initializes hooks for the given package and DeviceInfo.
     * Only modules with relevant data in DeviceInfo are initialized.
     * @param lpparam Load package parameters from Xposed.
     * @param deviceInfo The DeviceInfo containing spoofed data.
     */
    fun initializeHooks(lpparam: XC_LoadPackage.LoadPackageParam, deviceInfo: DeviceInfo) {
        hookModules.clear() // Clear previous hooks for this package
        addRelevantHooks(deviceInfo)
        
        hookModules.forEach { module ->
            try {
                Logger.log("HookManager: Initializing ${module.getModuleName()} for ${lpparam.packageName}")
                module.setDeviceInfo(deviceInfo)
                module.initialize(lpparam)
            } catch (e: Exception) {
                module.onError(e)
            }
        }
        Logger.log("HookManager: Initialized ${hookModules.size} hooks for ${lpparam.packageName}")
    }

    /**
     * Adds hook modules based on non-empty DeviceInfo fields.
     * @param deviceInfo The DeviceInfo to check for available data.
     */
    private fun addRelevantHooks(deviceInfo: DeviceInfo) {
        if (!deviceInfo.androidId.isNullOrBlank()) {
            hookModules.add(AndroidIdHook())
        }
        if (!deviceInfo.buildFingerprint.isNullOrBlank() || 
            !deviceInfo.buildDevice.isNullOrBlank() || 
            !deviceInfo.buildProduct.isNullOrBlank() || 
            !deviceInfo.buildBrand.isNullOrBlank() || 
            !deviceInfo.buildHardware.isNullOrBlank() || 
            !deviceInfo.buildBoard.isNullOrBlank() || 
            !deviceInfo.buildId.isNullOrBlank() || 
            !deviceInfo.buildDisplay.isNullOrBlank() || 
            !deviceInfo.buildType.isNullOrBlank() || 
            !deviceInfo.buildTags.isNullOrBlank() || 
            !deviceInfo.buildVersionRelease.isNullOrBlank() || 
            !deviceInfo.buildVersionIncremental.isNullOrBlank() || 
            !deviceInfo.buildVersionCodename.isNullOrBlank() || 
            !deviceInfo.buildVersionSecurityPatch.isNullOrBlank()) {
            hookModules.add(BuildHook())
            hookModules.add(SystemPropertiesHook())
        }
        if (!deviceInfo.imei1.isNullOrBlank() || !deviceInfo.imei2.isNullOrBlank()) {
            hookModules.add(ImeiHook())
        }
        if (!deviceInfo.wifiMac.isNullOrBlank() || !deviceInfo.bluetoothMac.isNullOrBlank()) {
            hookModules.add(MacAddressHook())
        }
        if (!deviceInfo.wifiSsid.isNullOrBlank() || !deviceInfo.wifiBssid.isNullOrBlank()) {
            hookModules.add(NetworkInfoHook())
        }
        if (!deviceInfo.simSerial.isNullOrBlank() || 
            !deviceInfo.mobileNumber.isNullOrBlank() || 
            !deviceInfo.simOperator.isNullOrBlank() || 
            !deviceInfo.simSubscriberId.isNullOrBlank() || 
            !deviceInfo.simCountry.isNullOrBlank() || 
            !deviceInfo.simMnc.isNullOrBlank()) {
            hookModules.add(SimInfoHook())
        }
        if (!deviceInfo.adsId.isNullOrBlank() || 
            !deviceInfo.mediaDrmId.isNullOrBlank() || 
            !deviceInfo.gsfId.isNullOrBlank()) {
            hookModules.add(AdditionalIdsHook())
        }
    }

    /**
     * Enables all initialized hooks.
     */
    fun enableAllHooks() {
        hookModules.sortedByDescending { it.getPriority() }.forEach { module ->
            try {
                if (!module.isHookActive()) {
                    module.enableHook()
                    Logger.log("HookManager: Enabled ${module.getModuleName()}")
                }
            } catch (e: Exception) {
                module.onError(e)
            }
        }
    }

    /**
     * Disables all hooks and cleans up resources.
     */
    fun disableAllHooks() {
        hookModules.forEach { module ->
            try {
                if (module.isHookActive()) {
                    module.disableHook()
                    Logger.log("HookManager: Disabled ${module.getModuleName()}")
                }
            } catch (e: Exception) {
                module.onError(e)
            }
        }
    }

    /**
     * Cleans up all hook modules.
     */
    fun cleanup() {
        hookModules.forEach { module ->
            try {
                module.cleanup()
                Logger.log("HookManager: Cleaned up ${module.getModuleName()}")
            } catch (e: Exception) {
                module.onError(e)
            }
        }
        hookModules.clear()
    }
}