package com.KTA.devicespoof.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.hook.HookManager

@Composable
fun HomeScreen(
    profileManager: ProfileManager,
    hookManager: HookManager,
    onNavigateToProfileManagement: () -> Unit,
    onNavigateToAppMapping: () -> Unit
) {
    var isModuleEnabled by remember { mutableStateOf(hookManager.isHookActive()) }
    val appMappings by remember { mutableStateOf(profileManager.getAllAppMappings()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Android Profile Faker",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                isModuleEnabled = !isModuleEnabled
                if (isModuleEnabled) {
                    hookManager.enableAllHooks()
                } else {
                    hookManager.disableAllHooks()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isModuleEnabled) "Disable Module" else "Enable Module")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToProfileManagement,
                modifier = Modifier.weight(1f)
            ) {
                Text("Manage Profiles")
            }
            Button(
                onClick = onNavigateToAppMapping,
                modifier = Modifier.weight(1f)
            ) {
                Text("Manage App Mappings")
            }
        }

        Text(
            text = "Mapped Applications",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(appMappings.entries.toList()) { (appPackage, profileId) ->
                val profile = profileManager.getProfileById(profileId)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAppMapping() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = appPackage)
                        Text(text = profile?.name ?: "No Profile")
                    }
                }
            }
        }
    }
}