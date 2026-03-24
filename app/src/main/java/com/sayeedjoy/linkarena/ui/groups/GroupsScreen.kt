package com.sayeedjoy.linkarena.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.ui.components.ColorDot
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroupsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<Group?>(null) }

    if (showCreateDialog) {
        CreateGroupDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, color ->
                viewModel.createGroup(name, color)
                showCreateDialog = false
            }
        )
    }

    editingGroup?.let { group ->
        EditGroupDialog(
            group = group,
            onDismiss = { editingGroup = null },
            onSave = { name, color ->
                viewModel.updateGroup(group.id, name, color, null)
                editingGroup = null
            },
            onDelete = {
                viewModel.deleteGroup(group.id)
                editingGroup = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group"
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.groups.isEmpty() -> {
                EmptyState(
                    title = "No groups yet",
                    message = "Tap the + button to create your first group",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.groups,
                        key = { it.id }
                    ) { group ->
                        GroupItem(
                            group = group,
                            modifier = Modifier.animateItem(),
                            onClick = { editingGroup = group }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: Group,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            group.color?.let { color ->
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(color))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(parsedColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } ?: run {
                ColorDot(
                    color = "#808080",
                    size = 40
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${group.bookmarkCount} bookmarks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
