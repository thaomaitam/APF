package com.KTA.devicespoof.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.KTA.devicespoof.profile.ProfileManager
import com.KTA.devicespoof.profile.Profile

@Composable
fun ProfileManagementScreen(
    profileManager: ProfileManager,
    onNavigateToProfileEditor: (String?) -> Unit
) {
    val profiles by remember { mutableStateOf(profileManager.getAllProfiles()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Profile Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { onNavigateToProfileEditor(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Profile")
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(profiles) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProfileEditor(profile.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = profile.name)
                        OutlinedButton(
                            onClick = {
                                profileManager.deleteProfile(profile.id)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}