package com.KTA.devicespoof.hook.impl

import android.os.SystemProperties
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import com.KTA.devicespoof.hook.interfaces.IHookModule
import com.KTA.devicespoof.config.DeviceConfig
import com.KTA.devicespoof.utils.Logger

class SystemPropertiesHook : IHookModule {
    
    private var isActive = false
    private var getMethodHook: XC_MethodHook? = null
    private var getIntMethodHook: XC_MethodHook? = null
    private var getBooleanMethodHook: XC_MethodHook? = null
    private var getLongMethodHook: XC_MethodHook? = null
    
    override fun initialize(lpparam: XC_LoadPackage.LoadPackageParam) {
        Logger.log("Initializing SystemProperties hook...")
        
        try {
            hookGetMethod()
            hookGetIntMethod()
            hookGetBooleanMethod()
            hookGetLongMethod()
            
            enableHook()
            Logger.log("SystemProperties hook initialized successfully")
            
        } catch (e: Exception) {
            Logger.error("Failed to initialize SystemProperties hook", e)
            onError(e)
        }
    }
    
    private fun hookGetMethod() {
        getMethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as? String ?: return
                val defaultValue = if (param.args.size > 1) param.args[1] as? String else null
                
                val spoofedValue = DeviceConfig.getSpoofedProperty(key)
                if (spoofedValue != null) {
                    param.result = spoofedValue
                    Logger.log("Spoofed SystemProperties.get($key) = $spoofedValue")
                }
            }
        }
        
        XposedHelpers.findAndHookMethod(
            SystemProperties::class.java,
            "get",
            String::class.java,
            getMethodHook
        )
        
        XposedHelpers.findAndHookMethod(
            SystemProperties::class.java,
            "get",
            String::class.java,
            String::class.java,
            getMethodHook
        )
    }
    
    private fun hookGetIntMethod() {
        getIntMethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as? String ?: return
                val defaultValue = if (param.args.size > 1) param.args[1] as? Int else 0
                
                val spoofedValue = DeviceConfig.getSpoofedProperty(key)
                if (spoofedValue != null) {
                    try {
                        param.result = spoofedValue.toInt()
                        Logger.log("Spoofed SystemProperties.getInt($key) = ${spoofedValue.toInt()}")
                    } catch (e: NumberFormatException) {
                        Logger.error("Failed to convert spoofed value to int: $spoofedValue", e)
                    }
                }
            }
        }
        
        XposedHelpers.findAndHookMethod(
            SystemProperties::class.java,
            "getInt",
            String::class.java,
            Int::class.java,
            getIntMethodHook
        )
    }
    
    private fun hookGetBooleanMethod() {
        getBooleanMethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as? String ?: return
                val defaultValue = if (param.args.size > 1) param.args[1] as? Boolean else false
                
                val spoofedValue = DeviceConfig.getSpoofedProperty(key)
                if (spoofedValue != null) {
                    try {
                        param.result = spoofedValue.toBoolean()
                        Logger.log("Spoofed SystemProperties.getBoolean($key) = ${spoofedValue.toBoolean()}")
                    } catch (e: Exception) {
                        Logger.error("Failed to convert spoofed value to boolean: $spoofedValue", e)
                    }
                }
            }
        }
        
        XposedHelpers.findAndHookMethod(
            SystemProperties::class.java,
            "getBoolean",
            String::class.java,
            Boolean::class.java,
            getBooleanMethodHook
        )
    }
    
    private fun hookGetLongMethod() {
        getLongMethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val key = param.args[0] as? String ?: return
                val defaultValue = if (param.args.size > 1) param.args[1] as? Long else 0L
                
                val spoofedValue = DeviceConfig.getSpoofedProperty(key)
                if (spoofedValue != null) {
                    try {
                        param.result = spoofedValue.toLong()
                        Logger.log("Spoofed SystemProperties.getLong($key) = ${spoofedValue.toLong()}")
                    } catch (e: NumberFormatException) {
                        Logger.error("Failed to convert spoofed value to long: $spoofedValue", e)
                    }
                }
            }
        }
        
        XposedHelpers.findAndHookMethod(
            SystemProperties::class.java,
            "getLong",
            String::class.java,
            Long::class.java,
            getLongMethodHook
        )
    }
    
    override fun enableHook() {
        isActive = true
        Logger.log("SystemProperties hook enabled")
    }
    
    override fun disableHook() {
        isActive = false
        Logger.log("SystemProperties hook disabled")
    }
    
    override fun isHookActive(): Boolean = isActive
    
    override fun getModuleName(): String = "SystemPropertiesHook"
    
    override fun getDescription(): String = "Hooks SystemProperties class to spoof device properties"
    
    override fun getPriority(): Int = 100
    
    override fun onError(error: Exception) {
        Logger.error("SystemProperties hook encountered an error", error)
        isActive = false
    }
    
    override fun cleanup() {
        getMethodHook = null
        getIntMethodHook = null
        getBooleanMethodHook = null
        getLongMethodHook = null
        isActive = false
        Logger.log("SystemProperties hook cleaned up")
    }
}