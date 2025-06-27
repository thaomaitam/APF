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
import com.KTA.devicespoof.utils.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMappingScreen(
    profiles: List<Profile>,
    installedApps: List<String>,
    appMappings: Map<String, String>,
    onMapAppToProfile: (appPackage: String, profileId: String?) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = installedApps.filter { packageName ->
        if (searchQuery.isBlank()) true
        else {
            try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                val appLabel = context.packageManager.getApplicationLabel(appInfo).toString()
                packageName.contains(searchQuery, ignoreCase = true) ||
                        appLabel.contains(searchQuery, ignoreCase = true)
            } catch (e: PackageManager.NameNotFoundException) {
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
            items(filteredApps, key = { it }) { packageName ->
                AppMappingItem(
                    packageName = packageName,
                    profiles = profiles,
                    currentProfileId = appMappings[packageName],
                    onProfileSelected = onMapAppToProfile
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMappingItem(
    packageName: String,
    profiles: List<Profile>,
    currentProfileId: String?,
    onProfileSelected: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val appLabel by remember(packageName) {
        mutableStateOf(
            try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                context.packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                packageName
            }
        )
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
                OutlinedTextField(
                    value = profiles.find { it.id == currentProfileId }?.name ?: "None",
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
                            onProfileSelected(packageName, null)
                            expanded = false
                        }
                    )
                    profiles.forEach { profile ->
                        DropdownMenuItem(
                            text = { Text(profile.name) },
                            onClick = {
                                onProfileSelected(packageName, profile.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}