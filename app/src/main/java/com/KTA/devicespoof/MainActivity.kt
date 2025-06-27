package com.KTA.devicespoof

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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

        // Khởi tạo ProfileManager một lần duy nhất
        val profileManager = ProfileManager(this)

        setContent {
            MaterialTheme {
                Surface {
                    // Truyền ProfileManager vào composable điều hướng chính
                    AppNavigation(profileManager)
                }
            }
        }
    }
}

/**
 * Composable chính quản lý toàn bộ luồng điều hướng và trạng thái UI.
 * Đây là "source of truth" cho các dữ liệu được hiển thị trên các màn hình.
 */
@Composable
fun AppNavigation(profileManager: ProfileManager) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Nạp danh sách ứng dụng đã cài đặt một lần và ghi nhớ nó.
    val installedApps = remember {
        getInstalledApps(context)
    }

    // Cơ chế kích hoạt recomposition cho toàn bộ UI khi dữ liệu thay đổi.
    // Bất cứ khi nào dữ liệu trong ProfileManager thay đổi (ví dụ: xóa profile),
    // chúng ta sẽ gọi `forceRecompose()` để cập nhật lại giao diện.
    var uiStateTrigger by remember { mutableStateOf(0) }
    val forceRecompose: () -> Unit = { uiStateTrigger++ }

    // Các trạng thái này sẽ được nạp lại mỗi khi `uiStateTrigger` thay đổi.
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
                getProfileName = { profileId -> profileManager.getProfileById(profileId)?.name ?: "Not Found" },
                onToggleModule = { isEnabled ->
                    profileManager.setModuleEnabled(isEnabled)
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
                    // Sử dụng "new" làm id cho profile mới để phân biệt
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
            val profileIdArg = backStackEntry.arguments?.getString("profileId")
            val isNewProfile = profileIdArg == "new"

            ProfileEditorScreen(
                profileManager = profileManager,
                profileId = if (isNewProfile) null else profileIdArg,
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

private fun getInstalledApps(context: Context): List<String> {
    return try {
        context.packageManager.getInstalledPackages(0)
            .filter { it.applicationInfo != null && !it.applicationInfo.packageName.startsWith("com.android.") }
            .map { it.packageName }
            .sorted()
    } catch (e: Exception) {
        Logger.error("MainActivity: Failed to get installed apps", e)
        emptyList()
    }
}