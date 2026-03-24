package com.sayeedjoy.linkarena.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sayeedjoy.linkarena.domain.model.Bookmark

@Composable
fun BookmarkCard(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onRefetch: () -> Unit,
    onFaviconResolved: (String) -> Unit,
    onGroupSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val faviconSize = 24.dp
    val colorScheme = MaterialTheme.colorScheme
    var hasReportedResolvedFavicon by remember(bookmark.id, bookmark.faviconUrl) {
        mutableStateOf(!bookmark.faviconUrl.isNullOrBlank())
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp), // matched rounded-3xl
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHighest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp) // generous padding
        ) {
            // Header Row: Icon, Title/URL, More button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Favicon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = colorScheme.surfaceContainerLow, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val faviconCandidates = remember(bookmark.faviconUrl, bookmark.url) {
                        val domain = bookmark.url?.extractDomain()
                        buildList {
                            bookmark.faviconUrl
                                ?.resolveAgainstBookmarkUrl(bookmark.url)
                                ?.let { add(it) }
                            domain?.let { add("https://www.google.com/s2/favicons?domain=$it&sz=64") }
                            domain?.let { add("https://icons.duckduckgo.com/ip3/$it.ico") }
                        }.distinct()
                    }
                    var faviconIndex by remember(bookmark.id, faviconCandidates) { mutableStateOf(0) }
                    val iconUrl = faviconCandidates.getOrNull(faviconIndex)

                    if (iconUrl != null) {
                        AsyncImage(
                            model = iconUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            filterQuality = FilterQuality.High,
                            onSuccess = {
                                if (!hasReportedResolvedFavicon && bookmark.faviconUrl != iconUrl) {
                                    hasReportedResolvedFavicon = true
                                    onFaviconResolved(iconUrl)
                                }
                            },
                            onError = {
                                if (faviconIndex < faviconCandidates.lastIndex) {
                                    faviconIndex += 1
                                }
                            }
                        )
                    } else {
                        // Fallback icon
                        Icon(
                            imageVector = Icons.Default.Edit, // matching the pen icon fallback from the image
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and URL
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Text(
                        text = bookmark.title ?: bookmark.url ?: "No title",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    bookmark.url?.let { url ->
                        Text(
                            text = url.extractDomain() ?: url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    // Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        containerColor = colorScheme.surface
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refetch", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                showMenu = false
                                onRefetch()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to group", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                showMenu = false
                                onGroupSelect()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DriveFileMove,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        HorizontalDivider(color = colorScheme.outlineVariant)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Delete",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                        }
                    }
                }
            }

            // Description
            if (bookmark.description != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = bookmark.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Note: If tags/groups properties are added to Bookmark, they would be rendered here
            // using a simple row of text with background. Since `Bookmark` model might not have
            // them yet, we'll wait for the model to include them. (Currently the screenshot shows "INSPO", "DESIGN" etc.)
            
            // Optionally, we could display the URL as a pill if needed, but we already have it at the top.
        }
    }
}

private fun String.extractDomain(): String? {
    return try {
        val url = if (startsWith("http")) this else "https://$this"
        java.net.URL(url).host
    } catch (e: Exception) {
        null
    }
}

private fun String.resolveAgainstBookmarkUrl(bookmarkUrl: String?): String {
    if (startsWith("http://") || startsWith("https://")) return this
    if (startsWith("//")) return "https:$this"
    return try {
        val baseUrl = bookmarkUrl?.let { if (it.startsWith("http")) it else "https://$it" }
        if (baseUrl == null) this else java.net.URI(baseUrl).resolve(this).toString()
    } catch (e: Exception) {
        this
    }
}
