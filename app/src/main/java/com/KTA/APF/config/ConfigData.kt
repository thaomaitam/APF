package com.KTA.APF.config

import kotlinx.serialization.Serializable

// Các hằng số để đảm bảo tên key và tên file SharedPreferences nhất quán
const val PREFS_NAME = "app_prefs"
const val CONFIG_KEY = "master_config"

/**
 * Đại diện cho một profile thiết bị có thể được giả mạo.
 * @param name Tên hiển thị cho người dùng (ví dụ: "Google Pixel 6"). Đây cũng là key trong map `profiles`.
 */
@Serializable
data class DeviceProfile(
    val name: String,
    val model: String,
    val brand: String,
    val manufacturer: String,
    val device: String,
    val product: String,
    val fingerprint: String,
    val buildId: String,
    val buildType: String,
    val buildTags: String,
    val release: String, // Ví dụ: "13"
    val sdk: String,     // Ví dụ: "33"
    val sdkInt: Int,     // Ví dụ: 33
    val androidId: String
)

/**
 * Đại diện cho một quy tắc do người dùng tạo ra.
 * @param targetPackage Tên package của ứng dụng sẽ bị áp dụng hook.
 * @param profileName Tên của DeviceProfile sẽ được sử dụng để giả mạo.
 */
@Serializable
data class Rule(
    val targetPackage: String,
    var profileName: String // `var` để có thể chỉnh sửa
)

/**
 * Lớp chính chứa toàn bộ cấu hình của ứng dụng.
 * Đối tượng này sẽ được serialize thành JSON và lưu vào SharedPreferences.
 */
@Serializable
data class MasterConfig(
    // Map chứa tất cả các profile thiết bị có sẵn, key là tên profile.
    val profiles: Map<String, DeviceProfile>,
    // Danh sách các quy tắc mà người dùng đã tạo.
    var rules: List<Rule>
)