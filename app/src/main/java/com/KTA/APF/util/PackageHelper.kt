package com.KTA.APF.util

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.KTA.APF.MyApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.*

object PackageHelper {

    // Lớp data để chứa thông tin cần thiết về một ứng dụng.
    data class AppInfo(
        val packageName: String,
        val label: String,
        val applicationInfo: ApplicationInfo, // Giữ lại để AppIconLoader sử dụng
        val installTime: Long,
        val updateTime: Long
    )

    // Scope riêng để thực hiện các tác vụ nặng trên background thread.
    private val scope = CoroutineScope(Dispatchers.IO)

    private val packageManager: PackageManager = MyApp.instance.packageManager

    // Dùng StateFlow để cache và thông báo khi danh sách app đã được tải xong.
    private val _appList = MutableStateFlow<List<AppInfo>>(emptyList())
    val appList = _appList.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        // Tải danh sách ứng dụng ngay khi PackageHelper được khởi tạo.
        refreshAppList()
    }

    /**
     * Tải lại danh sách ứng dụng từ hệ thống.
     * Đây là một tác vụ nặng, cần được chạy trên IO thread.
     */
    @SuppressLint("QueryPermissionsNeeded")
    fun refreshAppList() {
        if (_isRefreshing.value) return // Tránh gọi nhiều lần khi đang tải

        scope.launch {
            _isRefreshing.value = true
            AppLogger.d("Starting to refresh app list...")

            val apps = try {
                packageManager.getInstalledPackages(0)
            } catch (e: Exception) {
                AppLogger.e("Failed to get installed packages", e)
                emptyList<PackageInfo>()
            }

            val appInfoList = apps.mapNotNull { packageInfo ->
                // Bỏ qua chính ứng dụng APF
                if (packageInfo.packageName == MyApp.instance.packageName) {
                    return@mapNotNull null
                }
                
                // Lấy ApplicationInfo để tải label và icon
                val appInfo = packageInfo.applicationInfo ?: return@mapNotNull null
                
                AppInfo(
                    packageName = packageInfo.packageName,
                    label = appInfo.loadLabel(packageManager).toString(),
                    applicationInfo = appInfo,
                    installTime = packageInfo.firstInstallTime,
                    updateTime = packageInfo.lastUpdateTime
                )
            }.sortedWith(
                // Sắp xếp mặc định theo tên ứng dụng
                compareBy(Collator.getInstance(Locale.getDefault())) { it.label.lowercase() }
            )

            // Cập nhật StateFlow trên Main thread để UI có thể lắng nghe
            withContext(Dispatchers.Main) {
                _appList.value = appInfoList
                _isRefreshing.value = false
                AppLogger.d("App list refreshed with ${appInfoList.size} apps.")
            }
        }
    }

    /**
     * Lấy thông tin của một ứng dụng cụ thể từ danh sách đã cache.
     * @param packageName Tên package của ứng dụng cần tìm.
     * @return AppInfo nếu tìm thấy, ngược lại là null.
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return _appList.value.find { it.packageName == packageName }
    }
    
    /**
     * Kiểm tra một ứng dụng có phải là ứng dụng hệ thống hay không.
     * @param appInfo Đối tượng AppInfo của ứng dụng.
     * @return true nếu là ứng dụng hệ thống.
     */
    fun isSystemApp(appInfo: AppInfo): Boolean {
        return (appInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
    }
}