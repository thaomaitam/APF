// file: xposed/src/main/java/icu/nullptr/hidemyapplist/xposed/CentralService.kt

package icu.nullptr.hidemyapplist.xposed

import android.content.pm.IPackageManager
import android.util.Log
import icu.nullptr.hidemyapplist.common.BuildConfig
import icu.nullptr.hidemyapplist.common.Constants
import icu.nullptr.hidemyapplist.common.IAPFService
import icu.nullptr.hidemyapplist.common.JsonConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.full.memberProperties

/**
 * Service trung tâm chạy trong system_server.
 * Đây là một Singleton quản lý toàn bộ trạng thái và cấu hình của module.
 * @param pms Một tham chiếu đến IPackageManager, được lấy khi system_server khởi động.
 */
class CentralService(val pms: IPackageManager) : IAPFService.Stub() {

    companion object {
        private const val TAG = "APF-CentralService"

        // Biến static để các tiến trình khác có thể truy cập vào Service
        @Volatile
        var instance: CentralService? = null
            private set
    }

    private lateinit var dataDir: String
    private lateinit var configFile: File
    private lateinit var logFile: File
    
    // Khóa đồng bộ hóa để đảm bảo an toàn luồng
    private val configLock = Any()
    private val loggerLock = Any()

    // Cấu hình hiện tại của module, được nạp vào bộ nhớ
    var config: JsonConfig = JsonConfig()
        private set

    init {
        // Đảm bảo chỉ có một instance duy nhất được tạo
        if (instance == null) {
            instance = this
            initializeService()
        }
    }

    private fun initializeService() {
        // Các bước khởi tạo service
        setupDataDirectory()
        loadConfigFromFile()
        setupLogger()
        logI(TAG, "APF Central Service initialized. Version: ${BuildConfig.SERVICE_VERSION}")
    }

    private fun setupDataDirectory() {
        // Tạo một thư mục dữ liệu ngẫu nhiên trong /data/misc để lưu trữ config và log
        // Điều này làm cho việc tìm kiếm thư mục của module khó hơn một chút
        dataDir = "/data/misc/apf_service_" + Utils.generateRandomString(16)
        val dir = File(dataDir)
        if (!dir.exists()) {
            dir.mkdirs()
            // Có thể cần set quyền cho thư mục này nếu các tiến trình khác cần truy cập trực tiếp
            // Tuy nhiên, với mô hình hiện tại, chỉ system_server (uid 1000) cần đọc/ghi.
        }
        configFile = File(dataDir, "config.json")
        logFile = File(dataDir, "runtime.log")
        logI(TAG, "Data directory set to: $dataDir")
    }

    private fun loadConfigFromFile() {
        synchronized(configLock) {
            if (!configFile.exists()) {
                logI(TAG, "Config file not found. Creating a new one.")
                saveConfigToFile() // Tạo file mới với config mặc định
                return
            }
            try {
                val json = configFile.readText()
                val loadedConfig = JsonConfig.parse(json)

                if (loadedConfig.configVersion < BuildConfig.CONFIG_VERSION) {
                    logW(TAG, "Config version mismatch. Old: ${loadedConfig.configVersion}, New: ${BuildConfig.CONFIG_VERSION}. Re-creating config.")
                    // Ở đây bạn có thể thêm logic để di chuyển dữ liệu từ cấu hình cũ sang mới
                    // Hiện tại, chúng ta sẽ reset về mặc định để đảm bảo tính ổn định
                    config = JsonConfig()
                    saveConfigToFile()
                } else {
                    config = loadedConfig
                    logI(TAG, "Config loaded successfully.")
                }
            } catch (e: Exception) {
                logE(TAG, "Failed to parse config.json, it might be corrupted. Using default config.", e)
                config = JsonConfig()
            }
        }
    }

    private fun saveConfigToFile() {
        synchronized(configLock) {
            try {
                configFile.writeText(config.toString())
            } catch (e: Exception) {
                logE(TAG, "Failed to write config to file.", e)
            }
        }
    }
    
    private fun setupLogger() {
        synchronized(loggerLock) {
            if (logFile.exists()) {
                logFile.delete()
            }
            logFile.createNewFile()
        }
    }

    // --- API cho các Module Hook ---

    fun isFakerEnabledFor(packageName: String): Boolean {
        if (packageName == Constants.APP_PACKAGE_NAME || packageName == "android") {
            return false
        }
        synchronized(configLock) {
            return config.appConfigs[packageName]?.isEnabled == true
        }
    }

    fun getFakedValue(callerPackage: String, key: String): Any? {
        synchronized(configLock) {
            val appConfig = config.appConfigs[callerPackage]
            if (appConfig?.isEnabled != true || appConfig.appliedProfileName == null) {
                return null
            }
            
            val profile = config.profiles[appConfig.appliedProfileName] ?: return null
            
            return try {
                profile::class.memberProperties.find { it.name == key }?.get(profile)
            } catch (e: Throwable) {
                logE(TAG, "Reflection failed for key: $key", e)
                null
            }
        }
    }

    // --- Triển khai các phương thức từ IAPFService.aidl ---

    override fun getServiceVersion(): Int = BuildConfig.SERVICE_VERSION
    
    override fun syncConfig(json: String) {
        logI(TAG, "Received new config from UI")
        synchronized(configLock) {
            try {
                val newConfig = JsonConfig.parse(json)
                if (newConfig.configVersion != config.configVersion) {
                     logW(TAG, "Config version mismatch during sync. UI may need to be restarted.")
                }
                config = newConfig
                saveConfigToFile()
                logI(TAG, "Config synced and saved successfully.")
            } catch (e: Exception) {
                logE(TAG, "Failed to sync and parse new config.", e)
            }
        }
    }

    override fun getLogs(): String {
        synchronized(loggerLock) {
            return try {
                if (logFile.exists()) logFile.readText() else "Log file not found."
            } catch (e: IOException) {
                "Error reading log file: ${e.message}"
            }
        }
    }

    override fun clearLogs() {
        synchronized(loggerLock) {
            if (logFile.exists()) {
                logFile.writeText("")
                logI(TAG, "Logs cleared by UI request.")
            }
        }
    }
    
    override fun stopService() {
        logW(TAG, "Stop service requested. Module will be inactive until next reboot.")
        // Vô hiệu hóa instance, các hook sẽ không hoạt động nữa
        instance = null
        // TODO: Gọi hookManager.cleanup() nếu cần thiết để unhook động
    }

    // --- Hệ thống ghi log nội bộ ---

    fun addLog(level: Int, tag: String, msg: String, cause: Throwable? = null) {
        if (!config.detailLog && level < Log.INFO) return

        synchronized(loggerLock) {
            try {
                if (logFile.length() / 1024 > config.maxLogSize) {
                    clearLogs()
                    addLog(Log.WARN, TAG, "Log file reached size limit and was cleared.", null)
                }

                val levelChar = when (level) {
                    Log.DEBUG -> "D"
                    Log.INFO -> "I"
                    Log.WARN -> "W"
                    Log.ERROR -> "E"
                    else -> "V"
                }
                // Định dạng thời gian rõ ràng hơn
                val timestamp = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                val logEntry = "$timestamp $levelChar/$tag: $msg\n"

                logFile.appendText(logEntry)
                if (cause != null) {
                    logFile.appendText(Log.getStackTraceString(cause) + "\n")
                }
            } catch (e: IOException) {
                // Không thể làm gì nhiều nếu ghi log bị lỗi
            }
        }
    }
}