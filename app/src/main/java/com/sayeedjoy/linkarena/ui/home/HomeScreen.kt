package com.sayeedjoy.linkarena.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.ui.components.BookmarkCard
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.ErrorMessage
import com.sayeedjoy.linkarena.ui.components.GroupChip
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator
import com.sayeedjoy.linkarena.ui.components.MoveToGroupSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

/** Describes the type of content to show — transitions only fire on enum changes, not data updates. */
private enum class ContentState { Loading, Error, Empty, Content }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddBookmark: () -> Unit,
    onNavigateToBookmarkDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val autoRevalidateIntervalMs = 30.seconds.inWholeMilliseconds
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedBookmarkIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var bookmarkPendingDelete by remember { mutableStateOf<Bookmark?>(null) }
    var bookmarkPendingMove by remember { mutableStateOf<Bookmark?>(null) }
    val bookmarkListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val topBarColor = MaterialTheme.colorScheme.background

    // Derive a stable content-state enum so AnimatedContent only transitions on type changes
    val contentState by remember {
        derivedStateOf {
            when {
                uiState.bookmarks.isNotEmpty() -> ContentState.Content
                uiState.isLoading -> ContentState.Loading
                uiState.error != null -> ContentState.Error
                else -> ContentState.Empty
            }
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(autoRevalidateIntervalMs)
            viewModel.revalidate()
        }
    }

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

    bookmarkPendingDelete?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { bookmarkPendingDelete = null },
            title = { Text("Delete Bookmark") },
            text = {
                Text(
                    "Are you sure you want to delete \"${
                        bookmark.title ?: bookmark.url ?: "this bookmark"
                    }\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBookmark(bookmark.id)
                        bookmarkPendingDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move to group bottom sheet
    bookmarkPendingMove?.let { bookmark ->
        MoveToGroupSheet(
            groups = uiState.groups,
            currentGroupId = bookmark.groupId,
            onDismiss = { bookmarkPendingMove = null },
            onMoveToGroup = { groupId ->
                val actualGroupId = groupId.ifBlank { null }
                viewModel.moveBookmarkToGroup(bookmark.id, actualGroupId)
                bookmarkPendingMove = null
            },
            onCreateAndMove = { name, color ->
                viewModel.createGroupAndMoveBookmark(bookmark.id, name, color)
                bookmarkPendingMove = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = topBarColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedBookmarkIds.size} selected")
                    } else {
                        Text(
                            text = "LinkArena",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarColor,
                    scrolledContainerColor = topBarColor
                ),
                scrollBehavior = scrollBehavior,
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
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(end = 16.dp, start = 8.dp)
                                .size(40.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                val gradientBrush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.inversePrimary
                    )
                )

                androidx.compose.material3.Surface(
                    onClick = onNavigateToAddBookmark,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(64.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bookmark",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
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
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { 
                    Text(
                        text = "Search your library...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ) 
                },
                singleLine = true,
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            // Group filter chips
            if (uiState.groups.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val isAllSelected = uiState.selectedGroupId == null
                        androidx.compose.material3.Surface(
                            onClick = { viewModel.onGroupSelected(null) },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50),
                            color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "All",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isAllSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            )
                        }
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

            // Content — transition only fires when contentState enum changes
            AnimatedContent(
                targetState = contentState,
                label = "home_content",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                }
            ) { state ->
                when (state) {
                    ContentState.Content -> {
                        PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = viewModel::refresh,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                state = bookmarkListState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = uiState.bookmarks,
                                    key = { it.id }
                                ) { bookmark ->
                                    BookmarkCard(
                                        bookmark = bookmark,
                                        modifier = Modifier.animateItem(),
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
                                        onLongClick = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedBookmarkIds = setOf(bookmark.id)
                                            }
                                        },
                                        onRefetch = { viewModel.refetchBookmark(bookmark.id) },
                                        onFaviconResolved = { resolvedFaviconUrl ->
                                            viewModel.cacheBookmarkFavicon(
                                                bookmarkId = bookmark.id,
                                                faviconUrl = resolvedFaviconUrl
                                            )
                                        },
                                        onGroupSelect = {
                                            bookmarkPendingMove = bookmark
                                        },
                                        onSelect = {
                                            isSelectionMode = true
                                            selectedBookmarkIds = setOf(bookmark.id)
                                        },
                                        onEdit = { onNavigateToBookmarkDetail(bookmark.id) },
                                        onDelete = { bookmarkPendingDelete = bookmark },
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedBookmarkIds.contains(bookmark.id)
                                    )
                                }
                            }
                        }
                    }
                    ContentState.Loading -> {
                        LoadingIndicator(modifier = Modifier.fillMaxSize())
                    }
                    ContentState.Error -> {
                        ErrorMessage(
                            message = uiState.error ?: "Unknown error",
                            onRetry = viewModel::refresh,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ContentState.Empty -> {
                        EmptyState(
                            title = "No bookmarks yet",
                            message = "Tap the + button to add your first bookmark",
                            modifier = Modifier.fillMaxSize()
                        )
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
