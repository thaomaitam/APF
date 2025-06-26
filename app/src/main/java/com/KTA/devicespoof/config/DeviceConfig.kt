package com.KTA.devicespoof.config

import com.KTA.devicespoof.utils.Logger

object DeviceConfig {
    
    private lateinit var spoofedProperties: Map<String, String>
    
    // Device spoofing configuration for Pixel 6
    private val pixelSixConfig = mapOf(
        "ro.product.model" to "Pixel 6",
        "ro.build.fingerprint" to "google/oriole/oriole:14/UPB1.230816.019/10343536:user/release-keys",
        "ro.product.brand" to "google",
        "ro.product.name" to "Pixel 6",
        "ro.product.device" to "Pixel 6",
        "ro.build.version.release" to "14",
        "ro.build.id" to "UPB1.230816.019",
        "ro.build.version.incremental" to "10343536",
        "ro.build.type" to "user",
        "ro.build.tags" to "release-keys",
        "ro.build.version.security_patch" to "2024-12-05",
        "ro.product.first_api_level" to "32",
        
        // Additional related properties for consistency
        "ro.product.manufacturer" to "Google",
        "ro.build.product" to "Pixel 6",
        "ro.build.device" to "Pixel 6",
        "ro.build.board" to "oriole",
        "ro.build.hardware" to "oriole",
        "ro.vendor.build.fingerprint" to "google/oriole/oriole:14/UPB1.230816.019/10343536:user/release-keys",
        "ro.bootimage.build.fingerprint" to "google/oriole/oriole:14/UPB1.230816.019/10343536:user/release-keys",
        "ro.build.display.id" to "UPB1.230816.019",
        "ro.build.host" to "abfarm-release-2024",
        "ro.build.user" to "android-build",
        "ro.build.version.codename" to "REL",
        "ro.build.version.sdk" to "34"
    )
    
    // Android ID (SSAID) configuration
    private val androidId = "6cf311b92c5ea554"
    
    fun initialize() {
        spoofedProperties = pixelSixConfig
        Logger.log("Device configuration initialized with ${spoofedProperties.size} properties")
    }
    
    fun getSpoofedProperty(key: String): String? {
        return spoofedProperties[key]
    }
    
    fun getAllSpoofedProperties(): Map<String, String> {
        return spoofedProperties
    }
    
    fun getAndroidId(): String {
        return androidId
    }
    
    fun shouldSpoofProperty(key: String): Boolean {
        return spoofedProperties.containsKey(key)
    }
    
    // Property mapping for different access patterns
    fun getPropertyMappings(): Map<String, String> {
        return mapOf(
            "ro.product.model" to spoofedProperties["ro.product.model"]!!,
            "ro.product.brand" to spoofedProperties["ro.product.brand"]!!,
            "ro.product.name" to spoofedProperties["ro.product.name"]!!,
            "ro.product.device" to spoofedProperties["ro.product.device"]!!,
            "ro.product.manufacturer" to spoofedProperties["ro.product.manufacturer"]!!,
            "ro.build.fingerprint" to spoofedProperties["ro.build.fingerprint"]!!,
            "ro.build.version.release" to spoofedProperties["ro.build.version.release"]!!,
            "ro.build.id" to spoofedProperties["ro.build.id"]!!,
            "ro.build.version.incremental" to spoofedProperties["ro.build.version.incremental"]!!,
            "ro.build.type" to spoofedProperties["ro.build.type"]!!,
            "ro.build.tags" to spoofedProperties["ro.build.tags"]!!,
            "ro.build.version.security_patch" to spoofedProperties["ro.build.version.security_patch"]!!,
            "ro.product.first_api_level" to spoofedProperties["ro.product.first_api_level"]!!
        )
    }
}