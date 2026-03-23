package com.sayeedjoy.linkarena.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LinkArena") },
                actions = {
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddBookmark) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Bookmark"
                )
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
                                    onClick = { onNavigateToBookmarkDetail(bookmark.id) },
                                    onDelete = { viewModel.deleteBookmark(bookmark.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
