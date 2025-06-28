package com.KTA.devicespoof.utils

import de.robv.android.xposed.XposedBridge

object Logger {
    
    private const val TAG = "[DeviceSpoof]"
    private var debugEnabled = true
    
    fun log(message: String) {
        if (debugEnabled) {
            XposedBridge.log("$TAG $message")
        }
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        XposedBridge.log("$TAG ERROR: $message")
        throwable?.let {
            XposedBridge.log("$TAG ERROR: ${it.stackTraceToString()}")
        }
    }
    
    fun debug(message: String) {
        if (debugEnabled) {
            XposedBridge.log("$TAG DEBUG: $message")
        }
    }
    
    fun info(message: String) {
        XposedBridge.log("$TAG INFO: $message")
    }
    
    fun warn(message: String) {
        XposedBridge.log("$TAG WARN: $message")
    }
    
    fun setDebugEnabled(enabled: Boolean) {
        debugEnabled = enabled
        log("Debug logging ${if (enabled) "enabled" else "disabled"}")
    }
}