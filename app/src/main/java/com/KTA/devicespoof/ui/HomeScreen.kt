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

@Composable
fun HomeScreen(
    isModuleEnabled: Boolean,
    appMappings: Map<String, String>,
    getProfileName: (String) -> String,
    onToggleModule: (Boolean) -> Unit,
    onNavigateToProfileManagement: () -> Unit,
    onNavigateToAppMapping: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Android Profile Faker",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { onToggleModule(!isModuleEnabled) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isModuleEnabled) "Disable Module Globally" else "Enable Module Globally")
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

        if (appMappings.isEmpty()) {
            Text("No applications are mapped to a profile yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(appMappings.entries.toList()) { (appPackage, profileId) ->
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
                            Text(text = appPackage, modifier = Modifier.weight(1f))
                            Text(text = getProfileName(profileId))
                        }
                    }
                }
            }
        }
    }
}