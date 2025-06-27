package com.KTA.devicespoof.profile

import android.content.Context
import com.KTA.devicespoof.utils.Logger
import com.google.protobuf.InvalidProtocolBufferException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

// ProfileManager không còn là object nữa mà là một class để có thể khởi tạo trong UI
// và truyền đi. Điều này giúp Compose dễ dàng theo dõi sự thay đổi.
class ProfileManager(context: Context) {

    private val profileConfigFile: File = File(context.filesDir, "apf_profiles.bin")
    private var profileConfig: ProfileManagerConfig

    init {
        profileConfig = loadProfiles()
        Logger.log("ProfileManager initialized. Config file path: ${profileConfigFile.absolutePath}")
    }

    private fun loadProfiles(): ProfileManagerConfig {
        if (!profileConfigFile.exists()) {
            Logger.log("Profile config file not found. Creating a new default config.")
            // Mặc định bật module
            val defaultConfig = ProfileManagerConfig.newBuilder().setIsModuleEnabled(true).build()
            saveProfiles(defaultConfig)
            return defaultConfig
        }

        return try {
            FileInputStream(profileConfigFile).use { fis ->
                val config = ProfileManagerConfig.parseFrom(fis)
                Logger.log("Successfully loaded ${config.profilesCount} profiles and ${config.appMappingsCount} mappings.")
                config
            }
        } catch (e: Exception) {
            Logger.error("Error loading profile config: ${e.message}. The file might be corrupted. Creating a new default config.", e)
            if (profileConfigFile.exists()) profileConfigFile.delete()
            val defaultConfig = ProfileManagerConfig.newBuilder().setIsModuleEnabled(true).build()
            saveProfiles(defaultConfig)
            defaultConfig
        }
    }

    private fun saveProfiles(config: ProfileManagerConfig) {
        try {
            FileOutputStream(profileConfigFile).use { fos ->
                config.writeTo(fos)
                Logger.log("Saved ${config.profilesCount} profiles and ${config.appMappingsCount} mappings. Module enabled: ${config.isModuleEnabled}")
            }
            // Cập nhật state nội bộ sau khi lưu
            this.profileConfig = config
        } catch (e: Exception) {
            Logger.error("Error saving profile config: ${e.message}", e)
        }
    }

    // --- Global Settings ---

    fun isModuleGloballyEnabled(): Boolean {
        return profileConfig.isModuleEnabled
    }

    fun setModuleEnabled(isEnabled: Boolean) {
        val newConfig = profileConfig.toBuilder().setIsModuleEnabled(isEnabled).build()
        saveProfiles(newConfig)
    }


    // --- Profile Management ---

    fun getAllProfiles(): List<Profile> {
        return profileConfig.profilesList.sortedBy { it.name }
    }

    fun getProfileById(id: String): Profile? {
        return profileConfig.profilesList.find { it.id == id }
    }

    fun addProfile(profile: Profile): Profile {
        val newProfile = profile.toBuilder().setId(UUID.randomUUID().toString()).build()
        val newConfig = profileConfig.toBuilder().addProfiles(newProfile).build()
        saveProfiles(newConfig)
        Logger.log("Added profile with ID: ${newProfile.id}, Name: ${newProfile.name}")
        return newProfile
    }

    fun updateProfile(updatedProfile: Profile) {
        if (updatedProfile.id.isEmpty()) {
            Logger.warn("Cannot update profile: ID is empty.")
            return
        }
        val builder = profileConfig.toBuilder()
        val index = builder.profilesList.indexOfFirst { it.id == updatedProfile.id }

        if (index != -1) {
            builder.setProfiles(index, updatedProfile)
            saveProfiles(builder.build())
            Logger.log("Updated profile with ID: ${updatedProfile.id}")
        } else {
            Logger.warn("Profile with ID ${updatedProfile.id} not found for update.")
        }
    }

    fun deleteProfile(profileId: String) {
        if (profileId.isEmpty()) return

        val builder = profileConfig.toBuilder()
        val updatedProfiles = builder.profilesList.filterNot { it.id == profileId }
        val updatedMappings = builder.appMappingsList.filterNot { it.profileId == profileId }

        val newConfig = builder.clearProfiles()
            .addAllProfiles(updatedProfiles)
            .clearAppMappings()
            .addAllAppMappings(updatedMappings)
            .build()
        saveProfiles(newConfig)
        Logger.log("Removed profile with ID: $profileId and its mappings.")
    }

    // --- App Mapping ---

    fun getProfileForApp(appPackageName: String): Profile? {
        val mapping = profileConfig.appMappingsList.find { it.appPackageName == appPackageName }
        return mapping?.profileId?.let { getProfileById(it) }
    }

    fun getAllAppMappings(): Map<String, String> {
        return profileConfig.appMappingsList.associate { it.appPackageName to it.profileId }
    }

    fun addAppMapping(appPackageName: String, profileId: String) {
        val builder = profileConfig.toBuilder()
        // Xóa mapping cũ nếu có
        val currentMappings = builder.appMappingsList.filterNot { it.appPackageName == appPackageName }.toMutableList()

        // Thêm mapping mới
        val newMapping = AppProfileMapping.newBuilder()
            .setAppPackageName(appPackageName)
            .setProfileId(profileId)
            .build()
        currentMappings.add(newMapping)

        val newConfig = builder.clearAppMappings().addAllAppMappings(currentMappings).build()
        saveProfiles(newConfig)
        Logger.log("Mapped app '$appPackageName' to profile ID '$profileId'.")
    }

    fun removeAppMapping(appPackageName: String) {
        val builder = profileConfig.toBuilder()
        val updatedMappings = builder.appMappingsList.filterNot { it.appPackageName == appPackageName }
        val newConfig = builder.clearAppMappings().addAllAppMappings(updatedMappings).build()
        saveProfiles(newConfig)
        Logger.log("Removed mapping for app '$appPackageName'.")
    }
}