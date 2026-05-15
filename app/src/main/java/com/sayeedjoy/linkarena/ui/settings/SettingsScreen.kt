package com.sayeedjoy.linkarena.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sayeedjoy.linkarena.ads.AdConfigManager
import com.sayeedjoy.linkarena.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToPremium: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val isPremium = AdConfigManager.isPremium

    val context = LocalContext.current
    val packageInfo = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"

    val displayEmail = uiState.userEmail?.takeIf { it.isNotBlank() } ?: "Signed in"
    val displayName = uiState.userName?.takeIf { it.isNotBlank() } ?: "LinkArena User"

    val headerColor = MaterialTheme.colorScheme.primary
    val onHeaderColor = MaterialTheme.colorScheme.onPrimary

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Logout",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = { Text("Are you sure you want to sign out of your account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            selected = uiState.themeMode,
            onSelect = {
                viewModel.onThemeModeChange(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            SettingsHeader(
                displayName = displayName,
                displayEmail = displayEmail,
                photoUrl = uiState.userPhotoUrl,
                headerColor = headerColor,
                onHeaderColor = onHeaderColor
            )
        }

        item {
            PremiumUpgradeCard(
                isPremium = isPremium,
                onClick = onNavigateToPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-22).dp)
            )
        }

        item {
            SettingsSection(
                title = "Preferences",
                modifier = Modifier.offset(y = (-10).dp)
            ) {
                SettingsRow(
                    icon = Icons.Filled.NightsStay,
                    title = "Theme",
                    subtitle = "Choose how LinkArena follows your display",
                    trailing = {
                        Text(
                            text = uiState.themeMode.label(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.size(6.dp))
                        ChevronEnd()
                    },
                    onClick = { showThemeDialog = true }
                )
            }
        }

        item {
            SettingsSection(
                title = "Support",
                modifier = Modifier.offset(y = (-4).dp)
            ) {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "About Link Arena",
                    subtitle = "Version, credits, and app information",
                    trailing = { ChevronEnd() },
                    onClick = onNavigateToAbout
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.HelpOutline,
                    title = "Help & Support",
                    subtitle = "Send feedback or get help by email",
                    trailing = { ChevronEnd() },
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:thesayeedjoy@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "LinkArena - Help & Support")
                        }
                        runCatching { context.startActivity(intent) }
                    }
                )
                RowDivider()
                SettingsRow(
                    icon = Icons.Filled.StarOutline,
                    title = "Rate the App",
                    subtitle = "Share a review on Google Play",
                    trailing = { ChevronEnd() },
                    onClick = {
                        val pkg = context.packageName
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$pkg")
                        )
                        val result = runCatching { context.startActivity(intent) }
                        if (result.isFailure) {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }

        item {
            SettingsSection(title = "Account") {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Sign Out",
                    subtitle = "End this session on the device",
                    tint = MaterialTheme.colorScheme.error,
                    trailing = { ChevronEnd(tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)) },
                    onClick = { showLogoutDialog = true }
                )
            }
        }

        item {
            SettingsFooter(versionName = versionName)
        }
    }
}

@Composable
private fun SettingsHeader(
    displayName: String,
    displayEmail: String,
    photoUrl: String?,
    headerColor: Color,
    onHeaderColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        headerColor,
                        headerColor.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = onHeaderColor.copy(alpha = 0.82f)
            )
            Spacer(Modifier.height(18.dp))
            ProfileAvatar(photoUrl = photoUrl)
            Spacer(Modifier.height(12.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                ),
                color = onHeaderColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = displayEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = onHeaderColor.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp), content = content)
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun PremiumUpgradeCard(
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gradient = Brush.horizontalGradient(listOf(primaryColor, tertiaryColor))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPremium) Icons.Filled.CheckCircle else Icons.Filled.Stars,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isPremium) "You're on Pro" else "Upgrade to Pro",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = if (isPremium) "All features unlocked" else "AI grouping, colors & no ads",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.22f)
                ) {
                    Text(
                        text = if (isPremium) "Manage" else "View",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f))
                .padding(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        // Small edit affordance over the avatar.
        Box(
            modifier = Modifier
                .offset(x = (-4).dp, y = (-4).dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit profile",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = if (tint == MaterialTheme.colorScheme.error) tint else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
    }
}

@Composable
private fun SettingsFooter(versionName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Link Arena v$versionName",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Made with ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f)
            )
            Text(
                text = "<3",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = " for power users",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f)
            )
        }
    }
}

@Composable
private fun ChevronEnd(tint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) {
    Icon(
        imageVector = Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(20.dp)
    )
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    )
}

@Composable
private fun ThemePickerDialog(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Theme",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                ThemeOption(ThemeMode.SYSTEM, "System", Icons.Filled.Settings, selected, onSelect)
                ThemeOption(ThemeMode.LIGHT, "Light", Icons.Filled.LightMode, selected, onSelect)
                ThemeOption(ThemeMode.DARK, "Dark", Icons.Filled.DarkMode, selected, onSelect)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    label: String,
    icon: ImageVector,
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val isSelected = selected == mode
    Surface(
        onClick = { onSelect(mode) },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}
