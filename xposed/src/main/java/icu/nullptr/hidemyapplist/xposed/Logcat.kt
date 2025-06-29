// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/Logcat.kt

package icu.nullptr.hidemyapplist.xposed

import android.util.Log
import de.robv.android.xposed.XposedBridge
import java.util.concurrent.Executors

// Tạo một luồng riêng để xử lý việc ghi log, tránh làm chậm luồng chính của ứng dụng bị hook.
private val logExecutor = Executors.newSingleThreadExecutor()

/**
 * Hàm nội bộ để thực hiện việc ghi log.
 * @param level Cấp độ log (Log.DEBUG, Log.INFO, etc.).
 * @param tag Tag cho log.
 * @param msg Nội dung log.
 * @param cause Exception (nếu có).
 */
private fun log(level: Int, tag: String, msg: String, cause: Throwable? = null) {
    // Lấy instance của service. Nếu service chưa chạy, không làm gì cả.
    val service = CentralService.instance ?: return

    // Kiểm tra xem người dùng có bật "log chi tiết" không. Nếu không, chỉ ghi log từ INFO trở lên.
    if (!service.config.detailLog && level < Log.INFO) {
        return
    }
    
    // Đưa việc ghi log vào luồng riêng.
    logExecutor.execute {
        // Gửi log đến CentralService để lưu vào file runtime.log
        service.addLog(level, tag, msg, cause)

        // Đồng thời ghi log vào logcat của Xposed để có thể xem bằng các công cụ như LSPosed Manager
        val xposedLog = "$tag: $msg"
        XposedBridge.log(xposedLog)
        if (cause != null) {
            XposedBridge.log(cause)
        }
    }
}

// Các hàm tiện ích để gọi từ các file khác.
fun logD(tag: String, msg: String, cause: Throwable? = null) = log(Log.DEBUG, tag, msg, cause)
fun logI(tag: String, msg: String, cause: Throwable? = null) = log(Log.INFO, tag, msg, cause)
fun logW(tag: String, msg: String, cause: Throwable? = null) = log(Log.WARN, tag, msg, cause)
fun logE(tag: String, msg: String, cause: Throwable? = null) = log(Log.ERROR, tag, msg, cause)