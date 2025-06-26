package com.KTA.devicespoof.hook.interfaces

import de.robv.android.xposed.callbacks.XC_LoadPackage

interface IHookModule {
    
    /**
     * Initialize the hook module with the given load package parameters
     * @param lpparam Load package parameters from Xposed framework
     */
    fun initialize(lpparam: XC_LoadPackage.LoadPackageParam)
    
    /**
     * Enable the hook functionality
     */
    fun enableHook()
    
    /**
     * Disable the hook functionality
     */
    fun disableHook()
    
    /**
     * Check if the hook is currently active
     * @return true if hook is active, false otherwise
     */
    fun isHookActive(): Boolean
    
    /**
     * Get the name of this hook module
     * @return Module name as string
     */
    fun getModuleName(): String
    
    /**
     * Get description of what this hook module does
     * @return Description as string
     */
    fun getDescription(): String
    
    /**
     * Get the priority of this hook module (higher priority modules are initialized first)
     * @return Priority as integer
     */
    fun getPriority(): Int
    
    /**
     * Called when the hook module encounters an error
     * @param error The exception that occurred
     */
    fun onError(error: Exception)
    
    /**
     * Clean up resources when the module is being destroyed
     */
    fun cleanup()
}