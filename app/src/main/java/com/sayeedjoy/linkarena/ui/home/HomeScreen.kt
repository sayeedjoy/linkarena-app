package com.sayeedjoy.linkarena.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sayeedjoy.linkarena.R
import com.sayeedjoy.linkarena.ads.NativeAdCard
import com.sayeedjoy.linkarena.domain.model.Bookmark
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.ErrorMessage
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator
import com.sayeedjoy.linkarena.ui.components.MoveToGroupSheet
import com.sayeedjoy.linkarena.ui.components.OpenLinkDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

private val BrandNavy = Color(0xFF1E3A8A)
private val BrandBlue = Color(0xFF2563EB)
private val BrandSky = Color(0xFF60A5FA)

private enum class ContentState { Initializing, Loading, Error, Empty, Content }

private sealed interface HomeFeedItem {
    data object NativeAd : HomeFeedItem
    data class BookmarkItem(val bookmark: Bookmark) : HomeFeedItem
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToAddBookmark: () -> Unit,
    onNavigateToBookmarkDetail: (String) -> Unit,
    onNavigateToPremium: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val autoRevalidateIntervalMs = 30.seconds.inWholeMilliseconds
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedBookmarkIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkDeleteDialog by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var bookmarkPendingDelete by remember { mutableStateOf<Bookmark?>(null) }
    var bookmarkPendingMove by remember { mutableStateOf<Bookmark?>(null) }
    var bookmarkPendingOpen by remember { mutableStateOf<Bookmark?>(null) }
    val bookmarkListState = rememberLazyListState()
    val homeFeedItems = remember(uiState.bookmarks) {
        buildList {
            uiState.bookmarks.forEachIndexed { index, bookmark ->
                if (index == 4) add(HomeFeedItem.NativeAd)
                add(HomeFeedItem.BookmarkItem(bookmark))
            }
            if (uiState.bookmarks.size <= 4) add(HomeFeedItem.NativeAd)
        }
    }

    val contentState by remember {
        derivedStateOf {
            when {
                !uiState.isInitialized -> ContentState.Initializing
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
                        selectedBookmarkIds.forEach(viewModel::deleteBookmark)
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
                Text("Are you sure you want to delete \"${bookmark.title ?: bookmark.url ?: "this bookmark"}\"?")
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

    bookmarkPendingMove?.let { bookmark ->
        MoveToGroupSheet(
            groups = uiState.groups,
            currentGroupId = bookmark.groupId,
            onDismiss = { bookmarkPendingMove = null },
            onMoveToGroup = { groupId ->
                viewModel.moveBookmarkToGroup(bookmark.id, groupId.ifBlank { null })
                bookmarkPendingMove = null
            },
            onCreateAndMove = { name, color ->
                viewModel.createGroupAndMoveBookmark(bookmark.id, name, color)
                bookmarkPendingMove = null
            }
        )
    }

    Scaffold(
        containerColor = colorScheme.surfaceContainerLow,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (!isSelectionMode) {
                Surface(
                    onClick = onNavigateToAddBookmark,
                    shape = CircleShape,
                    color = BrandBlue,
                    contentColor = colorScheme.onPrimary,
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(58.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Bookmark",
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
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            HomeHeader(
                isSelectionMode = isSelectionMode,
                selectedCount = selectedBookmarkIds.size,
                onSearchClick = { isSearchVisible = !isSearchVisible },
                onPremiumClick = onNavigateToPremium,
                onDeleteSelected = {
                    if (selectedBookmarkIds.isNotEmpty()) showBulkDeleteDialog = true
                },
                onCloseSelection = {
                    isSelectionMode = false
                    selectedBookmarkIds = emptySet()
                }
            )

            if (isSearchVisible || uiState.searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HomeSearchField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HomeFilterRow(
                groups = uiState.groups,
                selectedGroupId = uiState.selectedGroupId,
                onGroupSelected = viewModel::onGroupSelected
            )
            Spacer(modifier = Modifier.height(12.dp))

            AnimatedContent(
                targetState = contentState,
                label = "home_content",
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
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
                                contentPadding = PaddingValues(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    items = homeFeedItems,
                                    key = { _, item ->
                                        when (item) {
                                            HomeFeedItem.NativeAd -> "native-ad"
                                            is HomeFeedItem.BookmarkItem -> item.bookmark.id
                                        }
                                    },
                                    contentType = { _, item ->
                                        when (item) {
                                            HomeFeedItem.NativeAd -> "native-ad"
                                            is HomeFeedItem.BookmarkItem -> "bookmark"
                                        }
                                    }
                                ) { _, item ->
                                    when (item) {
                                        HomeFeedItem.NativeAd -> NativeAdCard()
                                        is HomeFeedItem.BookmarkItem -> {
                                            val bookmark = item.bookmark
                                            HomeBookmarkRow(
                                                bookmark = bookmark,
                                                isSelectionMode = isSelectionMode,
                                                isSelected = selectedBookmarkIds.contains(bookmark.id),
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
                                                        bookmarkPendingOpen = bookmark
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        isSelectionMode = true
                                                        selectedBookmarkIds = setOf(bookmark.id)
                                                    }
                                                },
                                                onRefetch = { viewModel.refetchBookmark(bookmark.id) },
                                                onFaviconResolved = { faviconUrl ->
                                                    viewModel.cacheBookmarkFavicon(bookmark.id, faviconUrl)
                                                },
                                                onGroupSelect = { bookmarkPendingMove = bookmark },
                                                onSelect = {
                                                    isSelectionMode = true
                                                    selectedBookmarkIds = setOf(bookmark.id)
                                                },
                                                onEdit = { onNavigateToBookmarkDetail(bookmark.id) },
                                                onDelete = { bookmarkPendingDelete = bookmark }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ContentState.Initializing -> Box(modifier = Modifier.fillMaxSize())
                    ContentState.Loading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
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

@Composable
private fun HomeHeader(
    isSelectionMode: Boolean,
    selectedCount: Int,
    onSearchClick: () -> Unit,
    onPremiumClick: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCloseSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            IconButton(onClick = onDeleteSelected) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onCloseSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close selection",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(BrandBlue, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append("Link ") }
                        withStyle(SpanStyle(color = BrandBlue)) { append("Arena") }
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search bookmarks",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = onPremiumClick) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_premium_foreground),
                    contentDescription = "Premium",
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val colorScheme = MaterialTheme.colorScheme
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = colorScheme.onSurface,
            fontSize = 14.sp
        ),
        singleLine = true,
        cursorBrush = SolidColor(BrandBlue),
        modifier = modifier
            .height(44.dp)
            .background(colorScheme.surfaceContainerLowest, shape)
            .border(1.dp, colorScheme.outlineVariant, shape),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "Search your library...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun HomeFilterRow(
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 8.dp)
    ) {
        item {
            HomeFilterChip(
                label = "All",
                selected = selectedGroupId == null,
                onClick = { onGroupSelected(null) }
            )
        }
        items(groups, key = { it.id }) { group ->
            val groupColor = group.accentColor()
            HomeFilterChip(
                label = group.name,
                selected = selectedGroupId == group.id,
                color = groupColor,
                onClick = { onGroupSelected(group.id) }
            )
        }
    }
}

@Composable
private fun HomeFilterChip(
    label: String,
    selected: Boolean,
    color: Color = BrandBlue,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = if (selected) color else color.copy(alpha = 0.12f),
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else color,
        shadowElevation = 0.dp,
        modifier = Modifier.height(34.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeBookmarkRow(
    bookmark: Bookmark,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRefetch: () -> Unit,
    onFaviconResolved: (String) -> Unit,
    onGroupSelect: () -> Unit,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val rowShape = RoundedCornerShape(13.dp)
    val colorScheme = MaterialTheme.colorScheme
    val domain = remember(bookmark.url) { bookmark.url.extractDomain() ?: bookmark.url.orEmpty() }
    val faviconCandidates = remember(bookmark.faviconUrl, bookmark.url) {
        buildList {
            bookmark.faviconUrl?.resolveAgainstBookmarkUrl(bookmark.url)?.let { add(it) }
            domain.takeIf { it.isNotBlank() }?.let {
                add("https://www.google.com/s2/favicons?domain=$it&sz=64")
                add("https://icons.duckduckgo.com/ip3/$it.ico")
            }
        }.distinct()
    }
    var faviconIndex by remember(bookmark.id, faviconCandidates) { mutableStateOf(0) }
    var hasReportedResolvedFavicon by remember(bookmark.id, bookmark.faviconUrl) {
        mutableStateOf(!bookmark.faviconUrl.isNullOrBlank())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(colorScheme.surfaceContainerLowest, rowShape)
            .border(1.dp, colorScheme.outlineVariant, rowShape)
            .clip(rowShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(start = 12.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookmarkFavicon(
            iconUrl = faviconCandidates.getOrNull(faviconIndex),
            fallbackIcon = Icons.Default.Link,
            fallbackColor = BrandBlue,
            onLoadSuccess = { resolvedUrl ->
                if (!hasReportedResolvedFavicon && bookmark.faviconUrl != resolvedUrl) {
                    hasReportedResolvedFavicon = true
                    onFaviconResolved(resolvedUrl)
                }
            },
            onLoadError = {
                if (faviconIndex < faviconCandidates.lastIndex) {
                    faviconIndex += 1
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bookmark.title?.takeIf { it.isNotBlank() } ?: domain.ifBlank { "Untitled bookmark" },
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 18.sp
                ),
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = domain,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 15.sp
                ),
                color = colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                modifier = Modifier.size(40.dp)
            )
        } else {
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "More options",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp,
                    containerColor = colorScheme.surface,
                    offset = DpOffset(x = 0.dp, y = 4.dp)
                ) {
                    BookmarkMenuItem("Refetch", Icons.Default.Refresh) {
                        showMenu = false
                        onRefetch()
                    }
                    BookmarkMenuItem("Move to group", Icons.Default.Folder) {
                        showMenu = false
                        onGroupSelect()
                    }
                    BookmarkMenuItem("Edit", Icons.Default.Edit) {
                        showMenu = false
                        onEdit()
                    }
                    BookmarkMenuItem("Select", Icons.Default.CheckBox) {
                        showMenu = false
                        onSelect()
                    }
                    BookmarkMenuItem(
                        label = "Delete",
                        icon = Icons.Default.Delete,
                        tint = MaterialTheme.colorScheme.error
                    ) {
                        showMenu = false
                        onDelete()
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkFavicon(
    iconUrl: String?,
    fallbackIcon: ImageVector,
    fallbackColor: Color,
    onLoadSuccess: (String) -> Unit,
    onLoadError: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(fallbackColor.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low,
                onSuccess = { onLoadSuccess(iconUrl) },
                onError = { onLoadError() }
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = fallbackColor,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun BookmarkMenuItem(
    label: String,
    icon: ImageVector,
    tint: Color? = null,
    onClick: () -> Unit
) {
    val iconTint = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        onClick = onClick
    )
}

private fun Group.accentColor(): Color {
    color?.asColorOrNull()?.let { return it }
    val normalizedName = name.trim().lowercase()
    return when {
        normalizedName.contains("design") || normalizedName.contains("inspiration") -> Color(0xFFE84AA5)
        normalizedName.contains("finance") || normalizedName.contains("money") -> Color(0xFF17B26A)
        normalizedName.contains("work") -> Color(0xFF7C4DFF)
        normalizedName.contains("product") -> Color(0xFFFF5A3D)
        normalizedName.contains("entertainment") -> Color(0xFFFF1E1E)
        normalizedName.contains("dev") || normalizedName.contains("code") -> BrandBlue
        else -> BrandSky
    }
}

private fun String?.asColorOrNull(): Color? {
    if (isNullOrBlank()) return null
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (_: Exception) {
        null
    }
}

private fun String?.extractDomain(): String? {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return null
    return try {
        val normalized = if (raw.startsWith("http://") || raw.startsWith("https://")) raw else "https://$raw"
        java.net.URL(normalized).host.removePrefix("www.")
    } catch (_: Exception) {
        raw.removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("/")
            .takeIf { it.isNotBlank() }
    }
}

private fun String.resolveAgainstBookmarkUrl(bookmarkUrl: String?): String {
    if (startsWith("http://") || startsWith("https://")) return this
    if (startsWith("//")) return "https:$this"
    return try {
        val baseUrl = bookmarkUrl?.let { if (it.startsWith("http")) it else "https://$it" }
        if (baseUrl == null) this else java.net.URI(baseUrl).resolve(this).toString()
    } catch (_: Exception) {
        this
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
