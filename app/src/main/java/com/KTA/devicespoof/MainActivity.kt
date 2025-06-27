package com.KTA.devicespoof

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.ui.AppMappingScreen
import com.KTA.devicespoof.ui.HomeScreen
import com.KTA.devicespoof.ui.ProfileEditorScreen
import com.KTA.devicespoof.ui.ProfileManagementScreen
import com.KTA.devicespoof.utils.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileManager = ProfileManager(this)

        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation(profileManager)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(profileManager: ProfileManager) {
    val navController = rememberNavController()

    // State for installed apps
    val installedApps = remember {
        try {
            profileManager.packageManager.getInstalledPackages(0)
                .filter { it.applicationInfo != null && !it.applicationInfo.packageName.startsWith("com.android.") }
                .map { it.packageName }
                .sorted()
        } catch (e: Exception) {
            Logger.error("MainActivity: Failed to get installed apps", e)
            emptyList()
        }
    }

    // This state will hold the source of truth for UI data
    // When it changes, all relevant screens will recompose.
    var uiStateTrigger by remember { mutableStateOf(0) }
    val forceRecompose: () -> Unit = { uiStateTrigger++ }

    // Read the data inside the composable that depends on the trigger
    val isModuleEnabled by remember(uiStateTrigger) { mutableStateOf(profileManager.isModuleGloballyEnabled()) }
    val profiles by remember(uiStateTrigger) { mutableStateOf(profileManager.getAllProfiles()) }
    val appMappings by remember(uiStateTrigger) { mutableStateOf(profileManager.getAllAppMappings()) }


    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                isModuleEnabled = isModuleEnabled,
                appMappings = appMappings,
                getProfileName = { profileId -> profileManager.getProfileById(profileId)?.name ?: "N/A" },
                onToggleModule = {
                    profileManager.setModuleEnabled(it)
                    forceRecompose()
                },
                onNavigateToProfileManagement = { navController.navigate("profile_management") },
                onNavigateToAppMapping = { navController.navigate("app_mapping") }
            )
        }
        composable("profile_management") {
            ProfileManagementScreen(
                profiles = profiles,
                onNavigateToProfileEditor = { profileId ->
                    navController.navigate("profile_editor/${profileId ?: "new"}")
                },
                onDeleteProfile = { profileId ->
                    profileManager.deleteProfile(profileId)
                    forceRecompose()
                }
            )
        }
        composable(
            route = "profile_editor/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            val isNewProfile = profileId == "new"

            ProfileEditorScreen(
                profileManager = profileManager,
                profileId = if (isNewProfile) null else profileId,
                onSave = {
                    forceRecompose()
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() },
                onDelete = {
                    forceRecompose()
                    navController.popBackStack()
                }
            )
        }
        composable("app_mapping") {
            AppMappingScreen(
                profiles = profiles,
                installedApps = installedApps,
                appMappings = appMappings,
                onMapAppToProfile = { appPackage, profileId ->
                    if (profileId != null) {
                        profileManager.addAppMapping(appPackage, profileId)
                    } else {
                        profileManager.removeAppMapping(appPackage)
                    }
                    forceRecompose()
                }
            )
        }
    }
}