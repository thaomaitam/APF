package com.KTA.devicespoof

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.KTA.devicespoof.hook.HookManager
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.ui.AppMappingScreen
import com.KTA.devicespoof.ui.HomeScreen
import com.KTA.devicespoof.ui.ProfileEditorScreen
import com.KTA.devicespoof.ui.ProfileManagementScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.KTA.devicespoof.utils.Logger

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val profileManager = ProfileManager(this) // Assume ProfileManager constructor
        val hookManager = HookManager() // Assume HookManager constructor
        val installedApps = getInstalledApps()
        
        setContent {
            MaterialTheme {
                Surface {
                    AppNavigation(profileManager, hookManager, installedApps)
                }
            }
        }
    }

    private fun getInstalledApps(): List<String> {
        return try {
            packageManager.getInstalledPackages(0)
                .filter { it.applicationInfo != null && !it.applicationInfo.packageName.startsWith("com.android.") }
                .map { it.packageName }
                .sorted()
        } catch (e: Exception) {
            Logger.error("MainActivity: Failed to get installed apps", e)
            emptyList()
        }
    }
}

@Composable
fun AppNavigation(
    profileManager: ProfileManager,
    hookManager: HookManager,
    installedApps: List<String>
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                profileManager = profileManager,
                hookManager = hookManager,
                onNavigateToProfileManagement = { 
                    try {
                        navController.navigate("profile_management")
                        Logger.log("AppNavigation: Navigated to profile_management")
                    } catch (e: Exception) {
                        Logger.error("AppNavigation: Failed to navigate to profile_management", e)
                    }
                },
                onNavigateToAppMapping = { 
                    try {
                        navController.navigate("app_mapping")
                        Logger.log("AppNavigation: Navigated to app_mapping")
                    } catch (e: Exception) {
                        Logger.error("AppNavigation: Failed to navigate to app_mapping", e)
                    }
                }
            )
        }
        composable("profile_management") {
            ProfileManagementScreen(
                profileManager = profileManager,
                onNavigateToProfileEditor = { profileId ->
                    try {
                        navController.navigate("profile_editor/${profileId ?: "null"}")
                        Logger.log("AppNavigation: Navigated to profile_editor with profileId=$profileId")
                    } catch (e: Exception) {
                        Logger.error("AppNavigation: Failed to navigate to profile_editor", e)
                    }
                }
            )
        }
        composable(
            route = "profile_editor/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getString("profileId")
            ProfileEditorScreen(
                profileManager = profileManager,
                profileId = if (profileId != "null") profileId else null,
                onSave = { 
                    try {
                        navController.popBackStack()
                        Logger.log("AppNavigation: Popped back from profile_editor on save")
                    } catch (e: Exception) {
                        Logger.error("AppNavigation: Failed to pop back from profile_editor", e)
                    }
                },
                onCancel = { 
                    try {
                        navController.popBackStack()
                        Logger.log("AppNavigation: Popped back from profile_editor on cancel")
                    } catch (e: Exception) {
                        Logger.error("AppNavigation: Failed to pop back from profile_editor", e)
                    }
                }
            )
        }
        composable("app_mapping") {
            AppMappingScreen(
                profileManager = profileManager,
                installedApps = installedApps,
                onProfileSelected = { appPackage, profileId ->
                    try {
                        if (profileId != null) {
                            profileManager.addAppMapping(appPackage, profileId)
                            Logger.log("AppMappingScreen: Mapped $appPackage to profile $profileId")
                        } else {
                            profileManager.removeAppMapping(appPackage)
                            Logger.log("AppMappingScreen: Removed mapping for $appPackage")
                        }
                    } catch (e: Exception) {
                        Logger.error("AppMappingScreen: Failed to update mapping for $appPackage", e)
                    }
                }
            )
        }
    }
}