package com.KTA.APF.util

import android.util.Log
import com.KTA.APF.BuildConfig

/**
 * Lớp logger tiện ích cho module app.
 * Ghi log vào Logcat của Android.
 */
object AppLogger {
    private const val TAG = "APF-App"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.w(TAG, message)
        } else {
            Log.w(TAG, message, throwable)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable == null) {
            Log.e(TAG, message)
        } else {
            Log.e(TAG, message, throwable)
        }
    }
}