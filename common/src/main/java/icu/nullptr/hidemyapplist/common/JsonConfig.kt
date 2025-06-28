package icu.nullptr.hidemyapplist.common // Hoặc tên gói mới của bạn, ví dụ: com.yourname.apf.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Cấu hình Json cho toàn bộ ứng dụng, sử dụng kotlinx.serialization để tuần tự hóa.
 * - encodeDefaults = true: Đảm bảo các trường có giá trị mặc định vẫn được ghi vào file JSON.
 * - ignoreUnknownKeys = true: Khi đọc file JSON, nếu có trường không xác định, sẽ bỏ qua thay vì gây lỗi.
 *   Điều này hữu ích khi người dùng hạ cấp phiên bản ứng dụng.
 */
private val encoder = Json {
    prettyPrint = true // Làm cho file JSON dễ đọc hơn
    encodeDefaults = true
    ignoreUnknownKeys = true
}

/**
 * Lớp chính chứa toàn bộ cấu hình của ứng dụng.
 *
 * @property configVersion Phiên bản của cấu trúc config, dùng để xử lý khi cập nhật ứng dụng.
 * @property detailLog Bật/tắt log chi tiết cho việc gỡ lỗi.
 * @property maxLogSize Kích thước tối đa của file log (tính bằng KB).
 * @property profiles Một Map chứa tất cả các "Hồ sơ Faker" do người dùng tạo.
 *                    Key là tên duy nhất của profile (String), Value là đối tượng [AndroidProfile].
 * @property appConfigs Một Map chứa cấu hình áp dụng cho từng ứng dụng.
 *                      Key là tên gói của ứng dụng (String), Value là đối tượng [AppFakerConfig].
 */
@Serializable
data class JsonConfig(
    var configVersion: Int = BuildConfig.CONFIG_VERSION,
    var detailLog: Boolean = false,
    var maxLogSize: Int = 512,
    val profiles: MutableMap<String, AndroidProfile> = mutableMapOf(),
    val appConfigs: MutableMap<String, AppFakerConfig> = mutableMapOf()
) {
    companion object {
        /**
         * Phân tích một chuỗi JSON thành đối tượng [JsonConfig].
         */
        fun parse(json: String): JsonConfig = encoder.decodeFromString(json)
    }

    /**
     * Chuyển đổi đối tượng [JsonConfig] thành một chuỗi JSON.
     */
    override fun toString(): String = encoder.encodeToString(this)
}

/**
 * Đại diện cho một "Hồ sơ Faker" (Android Profile), chứa tất cả các giá trị giả mạo.
 * Các trường được để là `var` và nullable (`?`) để linh hoạt, người dùng không cần điền tất cả.
 *
 * @property profileName Tên định danh cho hồ sơ này, ví dụ: "Pixel 8 Pro", "Samsung S24 Custom".
 */
@Serializable
data class AndroidProfile(
    // Thông tin cơ bản
    var imei1: String? = null,
    var imei2: String? = null,
    var androidId: String? = null,
    var hardwareSerial: String? = null,
    var adsId: String? = null,

    // Thông tin mạng
    var wifiMac: String? = null,
    var wifiSsid: String? = null,
    var wifiBssid: String? = null,
    var bluetoothMac: String? = null,

    // Thông tin SIM
    var simSubscriberId: String? = null, // IMSI
    var simSerial: String? = null,       // ICCID
    var mobileNumber: String? = null,
    var simCountry: String? = null,      // e.g., "vn"
    var simOperator: String? = null,     // e.g., "45204" (MNC+MCC)
    var simMnc: String? = null,          // e.g., "04"
    var countryIso: String? = null,      // e.g., "VN"

    // Thông tin hệ thống (Build Properties)
    var buildBoard: String? = null,
    var buildId: String? = null,
    var buildDisplay: String? = null,
    var buildProduct: String? = null,
    var buildDevice: String? = null,
    var buildFingerprint: String? = null,
    var buildBrand: String? = null,
    var buildManufacturer: String? = null,
    var buildModel: String? = null,
    var buildHost: String? = null,
    var buildTags: String? = null,
    var buildType: String? = null,
    var buildUser: String? = null,

    // Thông tin phiên bản Build.VERSION
    var buildVersionRelease: String? = null,
    var buildVersionSdk: Int? = null,
    var buildVersionIncremental: String? = null,
    var buildVersionCodename: String? = null,
    var buildVersionSecurityPatch: String? = null
)

/**
 * Cấu hình Faker cho một ứng dụng cụ thể.
 *
 * @property isEnabled Công tắc chính để bật/tắt chức năng faker cho ứng dụng này.
 * @property appliedProfileName Tên của [AndroidProfile] được áp dụng cho ứng dụng này.
 *                            Giá trị này phải khớp với một key trong `JsonConfig.profiles`.
 *                            Nếu là `null`, sẽ không có profile nào được áp dụng.
 */
@Serializable
data class AppFakerConfig(
    var isEnabled: Boolean = false,
    var appliedProfileName: String? = null
)