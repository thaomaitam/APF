package com.KTA.devicespoof.utils

import android.util.Log

object Logger {
    private const val TAG = "DeviceSpoof"

    fun log(message: String) {
        Log.d(TAG, message)
    }

    fun warn(message: String) {
        Log.w(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
}