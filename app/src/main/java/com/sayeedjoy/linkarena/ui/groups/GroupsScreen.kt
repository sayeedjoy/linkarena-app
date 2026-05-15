package com.sayeedjoy.linkarena.ui.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.ads.AdConfigManager
import com.sayeedjoy.linkarena.domain.model.Group
import com.sayeedjoy.linkarena.ui.components.EmptyState
import com.sayeedjoy.linkarena.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onNavigateToGroupDetail: (String) -> Unit,
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
            },
            groupColoringAllowed = AdConfigManager.groupColoringAllowed
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
            },
            groupColoringAllowed = AdConfigManager.groupColoringAllowed
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        var searchQuery by remember { mutableStateOf("") }
        val visibleGroups = remember(uiState.groups, searchQuery) {
            val query = searchQuery.trim()
            if (query.isEmpty()) {
                uiState.groups
            } else {
                uiState.groups.filter { group ->
                    group.name.contains(query, ignoreCase = true)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            GroupsHeader(onCreateGroup = { showCreateDialog = true })
            Spacer(modifier = Modifier.height(10.dp))
            GroupsSearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            uiState.groups.isEmpty() -> {
                EmptyState(
                    title = "No groups yet",
                    message = "Tap the + button to create your first curated collection",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp)
                )
            }
            visibleGroups.isEmpty() -> {
                EmptyState(
                    title = "No matching groups",
                    message = "Try a different search term",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = visibleGroups,
                        key = { it.id }
                    ) { group ->
                        GroupItem(
                            group = group,
                            onClick = { onNavigateToGroupDetail(group.id) }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun GroupsHeader(
    onCreateGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Groups",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onCreateGroup) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Group",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun GroupsSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier
            .height(44.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow, shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f),
                shape = shape
            ),
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Search groups...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun GroupItem(
    group: Group,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val groupAccent = group.color.asGroupColorOrFallback(group.defaultAccentColor())
    val tileColor = groupAccent.copy(alpha = 0.12f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(tileColor, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = group.defaultIcon(),
                contentDescription = null,
                tint = groupAccent,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = group.name,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = group.bookmarkCount.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open ${group.name}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun Group.defaultIcon(): ImageVector {
    val normalizedName = name.trim().lowercase()
    return when {
        normalizedName.contains("favorite") -> Icons.Default.Favorite
        normalizedName.contains("read") || normalizedName.contains("later") -> Icons.Default.AccessTime
        normalizedName.contains("work") -> Icons.Default.Work
        normalizedName.contains("dev") || normalizedName.contains("code") -> Icons.Default.Code
        normalizedName.contains("design") || normalizedName.contains("inspiration") -> Icons.Default.Palette
        normalizedName.contains("finance") || normalizedName.contains("money") -> Icons.Default.Paid
        normalizedName.contains("personal") -> Icons.Default.Person
        else -> Icons.Default.Folder
    }
}

private fun Group.defaultAccentColor(): Color {
    val normalizedName = name.trim().lowercase()
    return when {
        normalizedName.contains("favorite") -> Color(0xFFFF3B55)
        normalizedName.contains("read") || normalizedName.contains("later") -> Color(0xFF7C4DFF)
        normalizedName.contains("work") -> Color(0xFF2F80ED)
        normalizedName.contains("dev") || normalizedName.contains("code") -> Color(0xFF17B26A)
        normalizedName.contains("design") || normalizedName.contains("inspiration") -> Color(0xFFF0449F)
        normalizedName.contains("finance") || normalizedName.contains("money") -> Color(0xFFF59E0B)
        normalizedName.contains("personal") -> Color(0xFF14B8A6)
        else -> Color(0xFF2F80ED)
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
