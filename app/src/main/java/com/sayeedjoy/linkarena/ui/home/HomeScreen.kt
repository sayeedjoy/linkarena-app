package com.sayeedjoy.linkarena.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.ui.components.BookmarkCard
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.ErrorMessage
import com.sayeedjoy.linkarena.ui.components.GroupChip
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddBookmark: () -> Unit,
    onNavigateToBookmarkDetail: (String) -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedBookmarkIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }

    if (showBulkDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteDialog = false },
            title = { Text("Delete Bookmarks") },
            text = { Text("Delete ${selectedBookmarkIds.size} selected bookmarks?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idsToDelete = selectedBookmarkIds.toList()
                        idsToDelete.forEach { id ->
                            viewModel.deleteBookmark(id)
                        }
                        showBulkDeleteDialog = false
                        selectedBookmarkIds = emptySet()
                        isSelectionMode = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedBookmarkIds.size} selected")
                    } else {
                        Text("LinkArena")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(
                            onClick = {
                                if (selectedBookmarkIds.isNotEmpty()) {
                                    showBulkDeleteDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(
                            onClick = {
                                isSelectionMode = false
                                selectedBookmarkIds = emptySet()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close selection"
                            )
                        }
                    } else {
                        IconButton(onClick = onNavigateToGroups) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = "Groups"
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = onNavigateToAddBookmark) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Bookmark"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search bookmarks...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true
            )

            // Group filter chips
            if (uiState.groups.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedGroupId == null,
                            onClick = { viewModel.onGroupSelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(uiState.groups) { group ->
                        GroupChip(
                            group = group,
                            isSelected = uiState.selectedGroupId == group.id,
                            onClick = { viewModel.onGroupSelected(group.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null -> {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = viewModel::refresh
                    )
                }
                uiState.bookmarks.isEmpty() -> {
                    EmptyState(
                        title = "No bookmarks yet",
                        message = "Tap the + button to add your first bookmark"
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.bookmarks,
                                key = { it.id }
                            ) { bookmark ->
                                BookmarkCard(
                                    bookmark = bookmark,
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedBookmarkIds = if (selectedBookmarkIds.contains(bookmark.id)) {
                                                selectedBookmarkIds - bookmark.id
                                            } else {
                                                selectedBookmarkIds + bookmark.id
                                            }
                                            if (selectedBookmarkIds.isEmpty()) {
                                                isSelectionMode = false
                                            }
                                        } else {
                                            val isOpened = openBookmarkInBrowser(
                                                context = context,
                                                url = bookmark.url
                                            )
                                            if (!isOpened) {
                                                Toast.makeText(
                                                    context,
                                                    "Invalid link",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    onRefetch = { viewModel.refetchBookmark(bookmark.id) },
                                    onGroupSelect = {
                                        isSelectionMode = true
                                        selectedBookmarkIds = setOf(bookmark.id)
                                    },
                                    onEdit = { onNavigateToBookmarkDetail(bookmark.id) },
                                    onDelete = { viewModel.deleteBookmark(bookmark.id) },
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedBookmarkIds.contains(bookmark.id)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openBookmarkInBrowser(context: android.content.Context, url: String?): Boolean {
    val rawUrl = url?.trim().orEmpty()
    if (rawUrl.isBlank()) return false

    val normalizedUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
        rawUrl
    } else {
        "https://$rawUrl"
    }

    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)))
        true
    } catch (_: Exception) {
        false
    }
}
