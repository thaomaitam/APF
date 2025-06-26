package com.KTA.devicespoof.hook

import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.hook.impl.SystemPropertiesHook
import com.KTA.devicespoof.hook.impl.AndroidIdHook
import com.KTA.devicespoof.hook.impl.BuildHook
import com.KTA.devicespoof.utils.Logger

class HookManager {
    
    private val hookModules = mutableListOf<IHookModule>()
    
    fun initializeHooks(lpparam: XC_LoadPackage.LoadPackageParam) {
        Logger.log("Initializing hook modules...")
        
        // Register all hook modules
        registerHookModule(SystemPropertiesHook())
        registerHookModule(AndroidIdHook())
        registerHookModule(BuildHook())
        
        // Initialize all registered modules
        hookModules.forEach { module ->
            try {
                module.initialize(lpparam)
                Logger.log("Successfully initialized: ${module.getModuleName()}")
            } catch (e: Exception) {
                Logger.error("Failed to initialize ${module.getModuleName()}", e)
            }
        }
        
        Logger.log("Hook manager initialization completed with ${hookModules.size} modules")
    }
    
    private fun registerHookModule(module: IHookModule) {
        hookModules.add(module)
        Logger.log("Registered hook module: ${module.getModuleName()}")
    }
    
    fun enableAllHooks() {
        hookModules.forEach { module ->
            try {
                module.enableHook()
                Logger.log("Enabled hook: ${module.getModuleName()}")
            } catch (e: Exception) {
                Logger.error("Failed to enable ${module.getModuleName()}", e)
            }
        }
    }
    
    fun disableAllHooks() {
        hookModules.forEach { module ->
            try {
                module.disableHook()
                Logger.log("Disabled hook: ${module.getModuleName()}")
            } catch (e: Exception) {
                Logger.error("Failed to disable ${module.getModuleName()}", e)
            }
        }
    }
    
    fun getHookModule(name: String): IHookModule? {
        return hookModules.find { it.getModuleName() == name }
    }
    
    fun getActiveHookCount(): Int {
        return hookModules.count { it.isHookActive() }
    }
    
    fun getAllHookModules(): List<IHookModule> {
        return hookModules.toList()
    }
}