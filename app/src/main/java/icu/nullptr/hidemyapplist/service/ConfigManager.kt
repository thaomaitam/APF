package icu.nullptr.hidemyapplist.service

import com.tsng.hidemyapplist.R
import icu.nullptr.hidemyapplist.common.BuildConfig
import icu.nullptr.hidemyapplist.common.JsonConfig
import icu.nullptr.hidemyapplist.common.JsonConfig.AndroidProfile
import icu.nullptr.hidemyapplist.common.JsonConfig.AppFakerConfig
import icu.nullptr.hidemyapplist.hmaApp
import icu.nullptr.hidemyapplist.ui.util.makeToast

/**
 * Lớp Singleton quản lý cấu hình của ứng dụng phía UI.
 * Nó đọc, ghi và cung cấp quyền truy cập vào file config.json.
 * Mọi thay đổi đều được đồng bộ với CentralService qua ServiceClient.
 */
object ConfigManager {

    private const val TAG = "APF-ConfigManager"
    private lateinit var config: JsonConfig
    val configFile = hmaApp.filesDir.resolve("config.json")

    fun init() {
        // Nếu file config không tồn tại, tạo một file mới với nội dung mặc định.
        if (!configFile.exists()) {
            configFile.writeText(JsonConfig().toString())
        }

        // Đọc và xác thực file config.
        runCatching {
            config = JsonConfig.parse(configFile.readText())
            // Kiểm tra phiên bản config. Nếu quá cũ, báo lỗi và yêu cầu người dùng xóa dữ liệu.
            if (config.configVersion < BuildConfig.MIN_BACKUP_VERSION) {
                throw RuntimeException("Config version too old: ${config.configVersion}")
            }
            // Nếu phiên bản thấp hơn phiên bản hiện tại, cập nhật nó.
            if (config.configVersion < BuildConfig.CONFIG_VERSION) {
                config.configVersion = BuildConfig.CONFIG_VERSION
                saveConfig()
            }
        }.onFailure {
            makeToast(R.string.config_damaged)
            // Gây ra crash để ngăn ứng dụng chạy với cấu hình không hợp lệ.
            throw RuntimeException("Config file is too old or damaged. Please clear app data.", it)
        }
    }

    /**
     * Lưu cấu hình hiện tại vào file và đồng bộ với service.
     * Đây là phương thức cốt lõi, được gọi sau mỗi lần thay đổi.
     */
    private fun saveConfig() {
        val jsonString = config.toString()
        configFile.writeText(jsonString)
        ServiceClient.syncConfig(jsonString)
    }

    //================================================================
    // API cho cài đặt chung
    //================================================================

    var detailLog: Boolean
        get() = config.detailLog
        set(value) {
            if (config.detailLog != value) {
                config.detailLog = value
                saveConfig()
            }
        }

    var maxLogSize: Int
        get() = config.maxLogSize
        set(value) {
            if (config.maxLogSize != value) {
                config.maxLogSize = value
                saveConfig()
            }
        }
    
    /**
     * Ghi đè toàn bộ cấu hình bằng một chuỗi JSON mới (sử dụng khi khôi phục từ backup).
     */
    fun importConfig(json: String) {
        try {
            val newConfig = JsonConfig.parse(json)
            // Thêm các bước kiểm tra phiên bản ở đây nếu cần
            config = newConfig
            config.configVersion = BuildConfig.CONFIG_VERSION // Luôn cập nhật lên phiên bản mới nhất
            saveConfig()
            makeToast(R.string.home_import_successful)
        } catch (e: Exception) {
            makeToast(R.string.home_import_failed)
        }
    }

    //================================================================
    // API cho quản lý Hồ sơ Faker (Profile)
    //================================================================

    /**
     * Lấy tất cả các profile đã lưu.
     * @return Một Map không thể thay đổi của các profile.
     */
    fun getProfiles(): Map<String, AndroidProfile> {
        return config.profiles.toMap()
    }

    /**
     * Lấy một profile cụ thể bằng tên.
     * @param name Tên của profile.
     * @return Đối tượng AndroidProfile hoặc null nếu không tìm thấy.
     */
    fun getProfile(name: String): AndroidProfile? {
        return config.profiles[name]
    }

    /**
     * Lưu một profile mới hoặc cập nhật một profile đã có.
     * @param name Tên của profile.
     * @param profile Đối tượng AndroidProfile chứa dữ liệu.
     */
    fun saveProfile(name: String, profile: AndroidProfile) {
        config.profiles[name] = profile
        saveConfig()
    }

    /**
     * Xóa một profile.
     * Đồng thời xóa áp dụng của profile này khỏi tất cả các ứng dụng.
     * @param name Tên của profile cần xóa.
     */
    fun deleteProfile(name: String) {
        if (config.profiles.remove(name) != null) {
            config.appConfigs.values.forEach { appConfig ->
                if (appConfig.appliedProfileName == name) {
                    appConfig.appliedProfileName = null
                }
            }
            saveConfig()
        }
    }

    /**
     * Đổi tên một profile.
     * Đồng thời cập nhật tên profile được áp dụng trong tất cả các cấu hình ứng dụng.
     * @param oldName Tên cũ.
     * @param newName Tên mới.
     */
    fun renameProfile(oldName: String, newName: String) {
        if (oldName == newName || !config.profiles.containsKey(oldName)) return

        val profile = config.profiles.remove(oldName)!!
        config.profiles[newName] = profile

        config.appConfigs.values.forEach { appConfig ->
            if (appConfig.appliedProfileName == oldName) {
                appConfig.appliedProfileName = newName
            }
        }
        saveConfig()
    }


    //================================================================
    // API cho quản lý Cấu hình Ứng dụng (AppFakerConfig)
    //================================================================
    
    /**
     * Lấy cấu hình faker cho một ứng dụng cụ thể.
     * Nếu ứng dụng chưa có cấu hình, một cấu hình mặc định (vô hiệu hóa) sẽ được tạo và trả về.
     * @param packageName Tên gói của ứng dụng.
     * @return Đối tượng AppFakerConfig.
     */
    fun getAppFakerConfig(packageName: String): AppFakerConfig {
        return config.appConfigs.getOrPut(packageName) { AppFakerConfig() }
    }
    
    /**
     * Kích hoạt hoặc vô hiệu hóa faker cho một ứng dụng.
     * @param packageName Tên gói của ứng dụng.
     * @param isEnabled Trạng thái bật/tắt.
     */
    fun setFakerEnabled(packageName: String, isEnabled: Boolean) {
        val appConfig = getAppFakerConfig(packageName)
        if (appConfig.isEnabled != isEnabled) {
            appConfig.isEnabled = isEnabled
            // Nếu không có profile nào được chọn khi bật, có thể để trống hoặc gán mặc định
            saveConfig()
        }
    }
    
    /**
     * Gán một profile cho một ứng dụng.
     * @param packageName Tên gói của ứng dụng.
     * @param profileName Tên của profile, hoặc null để xóa áp dụng.
     */
    fun setProfileForApp(packageName: String, profileName: String?) {
        val appConfig = getAppFakerConfig(packageName)
        if (appConfig.appliedProfileName != profileName) {
            appConfig.appliedProfileName = profileName
            saveConfig()
        }
    }
    
    /**
     * Kiểm tra xem một ứng dụng có đang được bật faker hay không.
     * @param packageName Tên gói của ứng dụng.
     * @return true nếu được bật, ngược lại là false.
     */
    fun isFakerEnabled(packageName: String): Boolean {
        return config.appConfigs[packageName]?.isEnabled ?: false
    }
}