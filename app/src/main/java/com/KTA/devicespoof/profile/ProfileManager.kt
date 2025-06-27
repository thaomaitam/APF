package com.KTA.devicespoof.profile

import android.content.Context
import com.KTA.devicespoof.utils.Logger
import com.google.protobuf.InvalidProtocolBufferException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

object ProfileManager {

    private const val PROFILE_CONFIG_FILE_NAME = "apf_profiles.bin"
    private lateinit var profileConfigFile: File
    private var profileConfig: ProfileManagerConfig? = null

    /**
     * Initializes the ProfileManager. Must be called once when the Xposed module is loaded.
     * @param context The application context (e.g., Xposed module context).
     */
    fun initialize(context: Context) {
        profileConfigFile = File(context.filesDir, PROFILE_CONFIG_FILE_NAME)
        Logger.log("ProfileManager initialized. Config file path: ${profileConfigFile.absolutePath}")
        loadProfiles()
    }

    private fun loadProfiles() {
        if (!profileConfigFile.exists()) {
            Logger.log("Profile config file not found. Creating a new default config.")
            profileConfig = ProfileManagerConfig.newBuilder().build()
            saveProfiles()
            return
        }

        try {
            FileInputStream(profileConfigFile).use { fis ->
                profileConfig = ProfileManagerConfig.parseFrom(fis)
                Logger.log("Successfully loaded ${profileConfig?.profilesCount ?: 0} profiles and ${profileConfig?.appMappingsCount ?: 0} mappings.")
            }
        } catch (e: InvalidProtocolBufferException) {
            Logger.error("Error loading profile config: ${e.message}. The file might be corrupted. Creating a new default config.")
            profileConfig = ProfileManagerConfig.newBuilder().build()
            if (!profileConfigFile.delete()) {
                Logger.error("Failed to delete corrupted profile config file.")
            }
            saveProfiles()
        } catch (e: Exception) {
            Logger.error("Unexpected error loading profiles: ${e.message}", e)
            profileConfig = ProfileManagerConfig.newBuilder().build()
            saveProfiles()
        }
    }

    private fun saveProfiles() {
        profileConfig?.let { config ->
            try {
                FileOutputStream(profileConfigFile).use { fos ->
                    config.writeTo(fos)
                    Logger.log("Saved ${config.profilesCount} profiles and ${config.appMappingsCount} mappings.")
                }
            } catch (e: Exception) {
                Logger.error("Error saving profile config: ${e.message}", e)
            }
        } ?: Logger.warn("Cannot save profiles: profileConfig is null.")
    }

    // --- Profile Management Methods ---

    /**
     * Returns a list of all profiles.
     * @return List of Profile objects, or empty list if none exist.
     */
    fun getAllProfiles(): List<Profile> {
        return profileConfig?.profilesList?.toList() ?: emptyList()
    }

    /**
     * Retrieves a profile by its ID.
     * @param id The unique ID of the profile.
     * @return The Profile object, or null if not found.
     */
    fun getProfileById(id: String): Profile? {
        return profileConfig?.profilesList?.find { it.id == id }
    }

    /**
     * Adds a new profile. Generates a UUID if the profile's ID is empty.
     * @param profile The profile to add.
     * @return The added Profile with its final ID.
     */
    fun addProfile(profile: Profile): Profile {
        val builder = profileConfig?.toBuilder() ?: ProfileManagerConfig.newBuilder()
        val newProfile = if (profile.id.isEmpty()) {
            profile.toBuilder().setId(UUID.randomUUID().toString()).build()
        } else {
            profile
        }
        builder.addProfiles(newProfile)
        profileConfig = builder.build()
        saveProfiles()
        Logger.log("Added profile with ID: ${newProfile.id}, Name: ${newProfile.name}")
        return newProfile
    }

    /**
     * Updates an existing profile. Replaces the profile with the same ID.
     * @param profile The profile to update (must have a non-empty ID).
     */
    fun updateProfile(profile: Profile) {
        if (profile.id.isEmpty()) {
            Logger.warn("Cannot update profile: ID is empty. Use addProfile instead.")
            return
        }
        val builder = profileConfig?.toBuilder() ?: ProfileManagerConfig.newBuilder()
        val index = builder.profilesList.indexOfFirst { it.id == profile.id }

        if (index != -1) {
            builder.setProfiles(index, profile)
            profileConfig = builder.build()
            saveProfiles()
            Logger.log("Updated profile with ID: ${profile.id}")
        } else {
            Logger.warn("Profile with ID ${profile.id} not found. Adding as new profile.")
            addProfile(profile)
        }
    }

    /**
     * Removes a profile and its associated mappings.
     * @param profileId The ID of the profile to remove.
     */
    fun removeProfile(profileId: String) {
        if (profileId.isEmpty()) {
            Logger.warn("Cannot remove profile: Profile ID is empty.")
            return
        }
        val builder = profileConfig?.toBuilder() ?: ProfileManagerConfig.newBuilder()
        val updatedProfiles = builder.profilesList.filterNot { it.id == profileId }
        val updatedMappings = builder.appMappingsList.filterNot { it.profileId == profileId }

        profileConfig = builder.clearProfiles()
            .addAllProfiles(updatedProfiles)
            .clearAppMappings()
            .addAllAppMappings(updatedMappings)
            .build()
        saveProfiles()
        Logger.log("Removed profile with ID: $profileId and its mappings.")
    }

    // --- App Mapping Methods ---

    /**
     * Retrieves the profile mapped to an application.
     * @param appPackageName The package name of the application.
     * @return The mapped Profile, or null if no mapping exists.
     */
    fun getProfileForApp(appPackageName: String): Profile? {
        val mapping = profileConfig?.appMappingsList?.find { it.appPackageName == appPackageName }
        return mapping?.profileId?.let { getProfileById(it) }
    }

    /**
     * Maps an application to a profile or removes the mapping if profileId is null/empty.
     * @param appPackageName The package name of the application.
     * @param profileId The ID of the profile to map, or null to unmap.
     */
    fun mapAppToProfile(appPackageName: String, profileId: String?) {
        if (appPackageName.isEmpty()) {
            Logger.warn("Cannot map app: Package name is empty.")
            return
        }
        val builder = profileConfig?.toBuilder() ?: ProfileManagerConfig.newBuilder()
        val currentMappings = builder.appMappingsList.filterNot { it.appPackageName == appPackageName }.toMutableList()

        if (!profileId.isNullOrEmpty()) {
            if (getProfileById(profileId) == null) {
                Logger.warn("Cannot map app '$appPackageName': Profile ID '$profileId' does not exist.")
                return
            }
            val newMapping = AppProfileMapping.newBuilder()
                .setAppPackageName(appPackageName)
                .setProfileId(profileId)
                .build()
            currentMappings.add(newMapping)
        }

        profileConfig = builder.clearAppMappings().addAllAppMappings(currentMappings).build()
        saveProfiles()
        Logger.log("Mapped app '$appPackageName' to profile ID '${profileId ?: "none"}'.")
    }

    /**
     * Removes the profile mapping for an application.
     * @param appPackageName The package name of the application.
     */
    fun removeAppMapping(appPackageName: String) {
        mapAppToProfile(appPackageName, null)
    }
}