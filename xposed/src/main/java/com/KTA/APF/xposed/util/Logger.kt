package com.KTA.APF.xposed.util

import de.robv.android.xposed.XposedBridge

object Logger {
    private const val TAG = "[APF-Hook]"

    /**
     * Ghi một tin nhắn thông thường vào log của Xposed.
     * @param message Nội dung cần ghi.
     */
    fun log(message: String) {
        // Log sẽ có dạng: [APF-Hook] Your message here
        XposedBridge.log("$TAG $message")
    }

    /**
     * Ghi một tin nhắn lỗi vào log của Xposed, có thể kèm theo một exception.
     * @param message Nội dung lỗi.
     * @param throwable (Tùy chọn) Exception để in ra stack trace.
     */
    fun error(message: String, throwable: Throwable? = null) {
        XposedBridge.log("$TAG ERROR: $message")
        // Nếu có exception, in toàn bộ stack trace để dễ dàng gỡ lỗi.
        throwable?.let {
            XposedBridge.log(it)
        }
    }
}