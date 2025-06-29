// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/UserService.kt

package icu.nullptr.hidemyapplist.xposed

import android.app.ActivityManagerHidden
import android.content.AttributionSource
import android.os.Build
import android.os.Bundle
import android.os.ServiceManager
import icu.nullptr.hidemyapplist.common.Constants
import rikka.hidden.compat.ActivityManagerApis
import rikka.hidden.compat.adapter.UidObserverAdapter

/**
 * Lớp này chịu trách nhiệm thiết lập cầu nối giữa CentralService (chạy trong system_server)
 * và ứng dụng UI (chạy trong tiến trình riêng).
 * Nó sử dụng một UidObserver để phát hiện khi nào ứng dụng UI được mở.
 */
object UserService {

    private const val TAG = "APF-UserService"
    private var appUid = 0

    private val uidObserver = object : UidObserverAdapter() {
        /**
         * Được gọi khi một UID trở nên active (ví dụ: ứng dụng được mở ra foreground).
         */
        override fun onUidActive(uid: Int) {
            // Chỉ quan tâm đến UID của ứng dụng APF
            if (uid != appUid) return

            logI(TAG, "APF UI is now active. Attempting to send binder...")

            try {
                // Lấy ContentProvider của ứng dụng UI.
                val provider = ActivityManagerApis.getContentProviderExternal(Constants.PROVIDER_AUTHORITY, 0, null, null)
                if (provider == null) {
                    logE(TAG, "Failed to get ServiceProvider. Is the app installed?")
                    return
                }

                // Đóng gói Binder của CentralService vào một Bundle.
                val extras = Bundle()
                extras.putBinder("binder", CentralService.instance)

                // Gọi đến ContentProvider để gửi binder.
                // Logic gọi khác nhau tùy phiên bản Android.
                val reply = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val attr = AttributionSource.Builder(Constants.UID_SYSTEM).setPackageName("android").build()
                    provider.call(attr, Constants.PROVIDER_AUTHORITY, "", null, extras)
                } else {
                    @Suppress("DEPRECATION") // Cảnh báo về việc dùng API cũ
                    provider.call("android", Constants.PROVIDER_AUTHORITY, "", extras)
                }

                if (reply == null) {
                    logE(TAG, "Failed to send binder to app. Call to provider returned null.")
                } else {
                    logI(TAG, "Successfully sent binder to app.")
                }

            } catch (e: Throwable) {
                logE(TAG, "Error occurred in onUidActive.", e)
            }
        }
    }

    /**
     * Hàm này được gọi từ XposedEntry để bắt đầu quá trình.
     */
    fun start() {
        logI(TAG, "Starting UserService...")

        // Chờ các dịch vụ hệ thống cần thiết sẵn sàng.
        waitSystemService("activity")
        
        val service = CentralService.instance
        if (service == null) {
            logE(TAG, "CentralService is not running. Cannot register UserService.")
            return
        }

        // Lấy UID của ứng dụng UI
        appUid = Utils.getPackageUidCompat(service.pms, Constants.APP_PACKAGE_NAME, 0, 0)
        
        // Kiểm tra chữ ký của ứng dụng UI để đảm bảo an toàn
        val appPackage = Utils.getPackageInfoCompat(service.pms, Constants.APP_PACKAGE_NAME, 0, 0)
        val sourceDir = appPackage?.applicationInfo?.sourceDir
        if (sourceDir == null || !Utils.verifyAppSignature(sourceDir)) {
            logE(TAG, "FATAL: App signature mismatch or app not found. Service will not function correctly.")
            return
        }
        
        logD(TAG, "APF UI client UID: $appUid")

        // Đăng ký UidObserver để theo dõi khi ứng dụng UI được mở.
        ActivityManagerApis.registerUidObserver(
            uidObserver,
            ActivityManagerHidden.UID_OBSERVER_ACTIVE,
            ActivityManagerHidden.PROCESS_STATE_UNKNOWN,
            null
        )
        logI(TAG, "UidObserver registered.")
    }

    private fun waitSystemService(name: String) {
        while (ServiceManager.getService(name) == null) {
            logI(TAG, "Waiting for system service: $name...")
            Thread.sleep(1000)
        }
    }
}