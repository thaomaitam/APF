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
import com.KTA.devicespoof.profile.Profile

@Composable
fun ProfileManagementScreen(
    profiles: List<Profile>,
    onNavigateToProfileEditor: (String?) -> Unit,
    onDeleteProfile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Profile Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { onNavigateToProfileEditor(null) }, // null ID means new profile
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create New Profile")
        }

        if (profiles.isEmpty()) {
            Text("No profiles created yet. Create one to get started.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProfileEditor(profile.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = profile.name, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { onDeleteProfile(profile.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}