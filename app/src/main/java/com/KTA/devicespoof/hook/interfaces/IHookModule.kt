package com.KTA.devicespoof.hook.interfaces

import com.KTA.devicespoof.profile.DeviceInfo
import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IHookModule {

    /**
     * Sets the DeviceInfo for this hook module, providing spoofed data from the selected profile.
     * Called by HookManager before initialization.
     * @param deviceInfo The DeviceInfo object containing spoofed data.
     */
    fun setDeviceInfo(deviceInfo: DeviceInfo)

    /**
     * Initializes the hook module with the given load package parameters.
     * Uses the DeviceInfo set by setDeviceInfo to configure hooks.
     * @param lpparam Load package parameters from the Xposed framework.
     */
    fun initialize(lpparam: XC_LoadPackage.LoadPackageParam)

    /**
     * Enables the hook module, applying the configured hooks.
     */
    fun enableHook()

    /**
     * Disables the hook module, stopping any active hooks.
     */
    fun disableHook()

    /**
     * Checks if the hook is currently active.
     * @return True if the hook is active, false otherwise.
     */
    fun isHookActive(): Boolean

    /**
     * Returns the name of the hook module for identification.
     * @return The module name.
     */
    fun getModuleName(): String

    /**
     * Returns a description of the hook module’s purpose.
     * @return The module description.
     */
    fun getDescription(): String

    /**
     * Returns the priority of the hook module (higher means executed first).
     * @return The priority value.
     */
    fun getPriority(): Int

    /**
     * Handles errors that occur during hook initialization or execution.
     * @param error The exception encountered.
     */
    fun onError(error: Exception)

    /**
     * Cleans up resources or state when the hook module is no longer needed.
     */
    fun cleanup()
}