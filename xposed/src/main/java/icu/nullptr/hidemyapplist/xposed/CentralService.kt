package icu.nullptr.hidemyapplist.xposed // Hoặc tên gói mới của bạn

import android.content.pm.IPackageManager
import android.util.Log
import icu.nullptr.hidemyapplist.common.BuildConfig
import icu.nullptr.hidemyapplist.common.JsonConfig
import icu.nullptr.hidemyapplist.common.IAPFService // Đã đổi tên từ IHMAService
import java.io.File
import kotlin.reflect.full.memberProperties

/**
 * Service trung tâm chạy trong system_server.
 * Đây là một Singleton quản lý toàn bộ trạng thái và cấu hình.
 */
class CentralService(val pms: IPackageManager) : IAPFService.Stub() {

    companion object {
        private const val TAG = "APF-CentralService"

        @Volatile
        var instance: CentralService? = null
            private set
    }

    private lateinit var dataDir: String
    private lateinit var configFile: File
    private lateinit var logFile: File
    
    // Khóa để đảm bảo an toàn luồng khi truy cập config và log
    private val configLock = Any()
    private val loggerLock = Any()

    // Biến chứa cấu hình được nạp vào bộ nhớ
    var config: JsonConfig = JsonConfig()
        private set

    init {
        // Chỉ khởi tạo một lần duy nhất
        if (instance == null) {
            instance = this
            initializeService()
            logI(TAG, "APF Central Service initialized. Version: ${BuildConfig.SERVICE_VERSION}")
        }
    }

    private fun initializeService() {
        // Tìm hoặc tạo thư mục dữ liệu
        setupDataDirectory()
        // Tải cấu hình từ file
        loadConfigFromFile()
        // Thiết lập hệ thống log
        setupLogger()
    }

    private fun setupDataDirectory() {
        // Logic tìm thư mục dữ liệu cũ của HMA để di chuyển/xóa có thể giữ lại nếu muốn tương thích ngược
        // Ở đây, chúng ta tạo mới
        dataDir = "/data/misc/apf_service_" + Utils.generateRandomString(16)
        val dir = File(dataDir)
        if (!dir.exists()) {
            dir.mkdirs()
            // Thiết lập quyền truy cập nếu cần, ví dụ: 771
            // Runtime.getRuntime().exec("chmod 771 $dataDir")
        }
        configFile = File(dataDir, "config.json")
        logFile = File(dataDir, "runtime.log")
        logI(TAG, "Data directory set to: $dataDir")
    }

    private fun loadConfigFromFile() {
        synchronized(configLock) {
            if (!configFile.exists()) {
                logI(TAG, "Config file not found. Creating a new one.")
                // Tạo file config rỗng nếu chưa có
                saveConfigToFile()
                return
            }
            try {
                val json = configFile.readText()
                val loadedConfig = JsonConfig.parse(json)

                // Kiểm tra phiên bản config để xử lý di chuyển dữ liệu nếu cần
                if (loadedConfig.configVersion < BuildConfig.CONFIG_VERSION) {
                    logW(TAG, "Config version mismatch (file: ${loadedConfig.configVersion}, app: ${BuildConfig.CONFIG_VERSION}). Re-creating config.")
                    // TODO: Thêm logic di chuyển dữ liệu từ phiên bản cũ sang mới nếu cần
                    // Nếu không, chỉ cần tạo lại config mới
                    config = JsonConfig() // Reset to default
                    saveConfigToFile()
                } else {
                    config = loadedConfig
                    logI(TAG, "Config loaded successfully.")
                }
            } catch (e: Exception) {
                logE(TAG, "Failed to parse config.json. Using default config.", e)
                config = JsonConfig() // Sử dụng config mặc định nếu có lỗi
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
        // Xoá log cũ khi khởi động để giữ file log gọn gàng
        synchronized(loggerLock) {
            if (logFile.exists()) {
                logFile.delete()
            }
            logFile.createNewFile()
        }
    }

    // --- Các phương thức public cho các module Hook ---

    /**
     * Kiểm tra xem chức năng Faker có được bật cho một package cụ thể không.
     */
    fun isFakerEnabledFor(packageName: String): Boolean {
        // Không áp dụng faker cho chính nó hoặc các ứng dụng hệ thống quan trọng
        if (packageName == Constants.APP_PACKAGE_NAME || packageName == "android") {
            return false
        }
        synchronized(configLock) {
            return config.appConfigs[packageName]?.isEnabled == true
        }
    }

    /**
     * Lấy giá trị giả mạo cho một thông số cụ thể của một ứng dụng.
     * Đây là phương thức cốt lõi được các hook gọi đến.
     * @param callerPackage Tên gói của ứng dụng đang gọi.
     * @param key Tên của thông số cần giả mạo (ví dụ: "buildFingerprint", "imei1").
     * @return Giá trị giả mạo (String), hoặc null nếu không có giá trị nào được cấu hình.
     */
    fun getFakedValue(callerPackage: String, key: String): Any? {
        synchronized(configLock) {
            val appConfig = config.appConfigs[callerPackage]
            if (appConfig?.isEnabled != true || appConfig.appliedProfileName == null) {
                return null
            }
            
            val profile = config.profiles[appConfig.appliedProfileName] ?: return null
            
            // Sử dụng Kotlin Reflection để lấy giá trị từ profile dựa trên key.
            // Điều này linh hoạt hơn việc dùng when/case lớn.
            return try {
                // Tìm thuộc tính trong lớp AndroidProfile có tên khớp với key
                profile::class.memberProperties.find { it.name == key }?.get(profile)
            } catch (e: Exception) {
                logE(TAG, "Reflection failed for key: $key", e)
                null
            }
        }
    }

    // --- Triển khai các phương thức từ IAPFService.aidl ---

    override fun getServiceVersion(): Int {
        return BuildConfig.SERVICE_VERSION
    }
    
    override fun syncConfig(json: String) {
        logI(TAG, "Received new config from UI.")
        synchronized(configLock) {
            try {
                val newConfig = JsonConfig.parse(json)
                // Thực hiện một số kiểm tra cơ bản
                if (newConfig.configVersion != BuildConfig.CONFIG_VERSION) {
                    logW(TAG, "Sync config: version mismatch, config will not be fully applied until reboot.")
                }
                config = newConfig
                saveConfigToFile()
                logI(TAG, "Config synced and saved successfully.")
            } catch (e: Exception) {
                logE(TAG, "Failed to sync config.", e)
            }
        }
    }

    override fun getLogs(): String {
        synchronized(loggerLock) {
            return if (logFile.exists()) logFile.readText() else "Log file not found."
        }
    }

    override fun clearLogs() {
        synchronized(loggerLock) {
            if (logFile.exists()) {
                logFile.writeText("") // Xóa nội dung file
                logI(TAG, "Logs cleared by UI request.")
            }
        }
    }
    
    // Giữ lại phương thức stopService để có thể tắt module mà không cần reboot (hữu ích khi gỡ lỗi)
    override fun stopService() {
        logI(TAG, "Stop service requested.")
        // TODO: Cần có một cơ chế để unhook tất cả các hook đã được tạo
        // HookManager sẽ chịu trách nhiệm này.
        // hookManager.disableAllHooks()
        // hookManager.cleanup()
        
        instance = null
    }

    // --- Các phương thức nội bộ cho việc ghi log ---
    // Chúng ta sẽ dùng hệ thống log riêng để có thể xem từ UI

    private fun addLog(level: Int, tag: String, msg: String, cause: Throwable?) {
        if (!config.detailLog && level < Log.INFO) return

        synchronized(loggerLock) {
            // Kiểm tra kích thước file log
            if (logFile.length() / 1024 > config.maxLogSize) {
                clearLogs()
                addLog(Log.WARN, TAG, "Log file reached size limit and was cleared.", null)
            }

            val levelStr = when (level) {
                Log.DEBUG -> "D"
                Log.INFO -> "I"
                Log.WARN -> "W"
                Log.ERROR -> "E"
                else -> "V"
            }
            val logEntry = "[${System.currentTimeMillis()}] [$levelStr] $tag: $msg\n"
            logFile.appendText(logEntry)

            if (cause != null) {
                logFile.appendText(Log.getStackTraceString(cause) + "\n")
            }
        }
    }

    fun logD(tag: String, msg: String) = addLog(Log.DEBUG, tag, msg, null)
    fun logI(tag: String, msg: String) = addLog(Log.INFO, tag, msg, null)
    fun logW(tag: String, msg: String, cause: Throwable? = null) = addLog(Log.WARN, tag, msg, cause)
    fun logE(tag: String, msg: String, cause: Throwable? = null) = addLog(Log.ERROR, tag, msg, cause)
}