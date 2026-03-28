package com.sayeedjoy.linkarena.ui.groups

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.ui.components.BookmarkCard
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.LinkArenaTopBar
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator
import com.sayeedjoy.linkarena.ui.components.MoveToGroupSheet
import com.sayeedjoy.linkarena.ui.components.OpenLinkDialog

private val colorOptions = listOf(
    "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
    "#FFEAA7", "#DDA0DD", "#98D8C8", "#F7DC6F",
    "#BB8FCE", "#85C1E9", "#F8B500", "#00CED1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBookmarkDetail: (String) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var bookmarkPendingDelete by remember { mutableStateOf<Bookmark?>(null) }
    var bookmarkPendingMove by remember { mutableStateOf<Bookmark?>(null) }
    var bookmarkPendingOpen by remember { mutableStateOf<Bookmark?>(null) }
    val moveSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    bookmarkPendingOpen?.let { bookmark ->
        OpenLinkDialog(
            url = bookmark.url ?: "",
            title = bookmark.title,
            faviconUrl = bookmark.faviconUrl,
            onConfirm = {
                openBookmarkInBrowser(context, bookmark.url)
                bookmarkPendingOpen = null
            },
            onDismiss = { bookmarkPendingOpen = null }
        )
    }

    if (showEditDialog && uiState.group != null) {
        EditGroupNameColorDialog(
            group = uiState.group!!,
            onDismiss = { showEditDialog = false },
            onSave = { name, color ->
                viewModel.updateGroup(name, color)
                showEditDialog = false
            }
        )
    }

    bookmarkPendingDelete?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { bookmarkPendingDelete = null },
            title = { Text("Delete Bookmark") },
            text = { Text("Delete \"${bookmark.title ?: bookmark.url}\"?") },
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

    bookmarkPendingMove?.let { bookmark ->
        MoveToGroupSheet(
            groups = uiState.groups.filter { it.id != uiState.group?.id },
            currentGroupId = uiState.group?.id,
            onDismiss = { bookmarkPendingMove = null },
            onMoveToGroup = { groupId ->
                viewModel.moveBookmarkToGroup(bookmark.id, groupId)
                bookmarkPendingMove = null
            },
            onCreateAndMove = { name, color ->
                viewModel.createGroupAndMoveBookmark(bookmark.id, name, color)
                bookmarkPendingMove = null
            },
            sheetState = moveSheetState
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LinkArenaTopBar(
                title = {
                    uiState.group?.let { group ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val groupColor = group.color.asGroupColorOrFallback(MaterialTheme.colorScheme.primary)
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(groupColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    } ?: Text("Group")
                },
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Group"
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.bookmarks.isEmpty() -> {
                EmptyState(
                    title = "No bookmarks yet",
                    message = "Add bookmarks to this group from the bookmark detail page",
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
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.bookmarks,
                        key = { it.id }
                    ) { bookmark ->
                        BookmarkCard(
                            bookmark = bookmark,
                            onClick = {
                                bookmarkPendingOpen = bookmark
                            },
                            onLongClick = { },
                            onRefetch = { viewModel.refetchBookmark(bookmark.id) },
                            onFaviconResolved = { faviconUrl ->
                                viewModel.cacheBookmarkFavicon(bookmark.id, faviconUrl)
                            },
                            onGroupSelect = { bookmarkPendingMove = bookmark },
                            onSelect = { },
                            onEdit = { onNavigateToBookmarkDetail(bookmark.id) },
                            onDelete = { bookmarkPendingDelete = bookmark }
                        )
                    }
                }
            }
        }
    }
}

private fun String?.asGroupColorOrFallback(fallback: Color): Color {
    if (this.isNullOrBlank()) return fallback
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (_: Exception) {
        fallback
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

@Composable
private fun EditGroupNameColorDialog(
    group: Group,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String?) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var selectedColor by remember { mutableStateOf(group.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Group") },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colorOptions) { color ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.background(Color.White.copy(alpha = 0.3f), CircleShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == color) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
