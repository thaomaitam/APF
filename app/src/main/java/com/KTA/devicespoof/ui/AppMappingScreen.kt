package com.KTA.devicespoof.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.KTA.devicespoof.profile.Profile
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.utils.Logger

@Composable
fun AppMappingScreen(
    profileManager: ProfileManager,
    installedApps: List<String>,
    onProfileSelected: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val profiles by remember { mutableStateOf(profileManager.getAllProfiles()) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter apps based on search query
    val filteredApps = installedApps.filter { packageName ->
        if (searchQuery.isBlank()) true
        else {
            try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                val appLabel = context.packageManager.getApplicationLabel(appInfo).toString()
                packageName.contains(searchQuery, ignoreCase = true) ||
                        appLabel.contains(searchQuery, ignoreCase = true)
            } catch (e: PackageManager.NameNotFoundException) {
                Logger.error("AppMappingScreen: Failed to get app info for $packageName", e)
                false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "App to Profile Mapping",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search apps") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps) { packageName ->
                AppMappingItem(
                    packageName = packageName,
                    profiles = profiles,
                    profileManager = profileManager,
                    onProfileSelected = onProfileSelected
                )
            }
        }
    }
}

@Composable
fun AppMappingItem(
    packageName: String,
    profiles: List<Profile>,
    profileManager: ProfileManager,
    onProfileSelected: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedProfileId by remember { mutableStateOf(profileManager.getAllAppMappings()[packageName]) }
    var appLabel by remember { mutableStateOf(packageName) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            appLabel = context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.error("AppMappingItem: Failed to get app label for $packageName", e)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = appLabel,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = profiles.find { it.id == selectedProfileId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Profile") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedProfileId = null
                            onProfileSelected(packageName, null)
                            Logger.log("AppMappingItem: Removed mapping for $packageName")
                            expanded = false
                        }
                    )
                    profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.name) },
                            onClick = {
                                selectedProfileId = profile.id
                                onProfileSelected(packageName, profile.id)
                                Logger.log("AppMappingItem: Mapped $packageName to profile ${profile.name} (ID: ${profile.id})")
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}