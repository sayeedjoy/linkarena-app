package com.sayeedjoy.linkarena.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.sayeedjoy.linkarena.ui.theme.LinkArenaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkArenaTopBar(
    title: @Composable () -> Unit,
    onNavigationClick: (() -> Unit)? = null,
    navigationContentDescription: String = "Back",
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    scrolledContainerColor: Color = containerColor,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = title,
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = navigationContentDescription
                    )
                }
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor,
            scrolledContainerColor = scrolledContainerColor,
            titleContentColor = titleContentColor,
            navigationIconContentColor = navigationIconContentColor,
            actionIconContentColor = actionIconContentColor
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkArenaTopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    navigationContentDescription: String = "Back",
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    scrolledContainerColor: Color = containerColor,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    navigationIconContentColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LinkArenaTopBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onNavigationClick = onNavigationClick,
        navigationContentDescription = navigationContentDescription,
        scrollBehavior = scrollBehavior,
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor,
        titleContentColor = titleContentColor,
        navigationIconContentColor = navigationIconContentColor,
        actionIconContentColor = actionIconContentColor,
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LinkArenaTopBarPreview() {
    LinkArenaTheme {
        LinkArenaTopBar(
            title = "Link Arena",
            onNavigationClick = {}
        )
    }
}
