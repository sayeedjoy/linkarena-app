package com.sayeedjoy.linkarena.ui.premium

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sayeedjoy.linkarena.R
import com.sayeedjoy.linkarena.ads.AdConfigManager
import com.sayeedjoy.linkarena.ui.components.LinkArenaTopBar
import com.sayeedjoy.linkarena.ui.theme.LinkArenaTheme

private data class PremiumBenefit(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isPremium = remember { AdConfigManager.isPremium }

    PremiumContent(
        isPremium = isPremium,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onGetPro = { openPlayStorePurchase(context) },
        onRestore = {}
    )
}

@Composable
private fun PremiumContent(
    isPremium: Boolean,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onGetPro: () -> Unit,
    onRestore: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val headerColor = colorScheme.primaryContainer
    PremiumSystemBars(
        statusBarColor = headerColor,
        navigationBarColor = colorScheme.surface
    )

    Scaffold(
        containerColor = colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            val compact = maxHeight < 760.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                PremiumHeader(
                    isPremium = isPremium,
                    compact = compact,
                    onNavigateBack = onNavigateBack
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            horizontal = if (compact) 14.dp else 18.dp,
                            vertical = if (compact) 8.dp else 12.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
                ) {
                    Text(
                        text = "What Pro unlocks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onBackground
                    )

                    PremiumBenefitGrid(
                        benefits = buildBenefitList(),
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )

                    PremiumNote(isPremium = isPremium, compact = compact)
                }

                PremiumActions(
                    isPremium = isPremium,
                    isLoading = isLoading,
                    compact = compact,
                    onGetPro = onGetPro,
                    onRestore = onRestore
                )
            }
        }
    }
}

@Composable
private fun PremiumBenefitGrid(
    benefits: List<PremiumBenefit>,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
    ) {
        benefits.chunked(2).forEach { rowBenefits ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
            ) {
                rowBenefits.forEach { benefit ->
                    PremiumBenefitTile(
                        benefit = benefit,
                        compact = compact,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowBenefits.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumHeader(
    isPremium: Boolean,
    compact: Boolean,
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val iconSize = if (compact) 64.dp else 88.dp
    val iconCardSize = if (compact) 50.dp else 68.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer,
                        colorScheme.primaryContainer.copy(alpha = 0.72f),
                        colorScheme.background
                    )
                )
            )
    ) {
        Column {
            LinkArenaTopBar(
                title = {},
                onNavigationClick = onNavigateBack,
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = colorScheme.onPrimaryContainer
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (compact) 16.dp else 20.dp)
                    .padding(top = if (compact) 2.dp else 6.dp, bottom = if (compact) 12.dp else 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
            ) {
                PremiumIcon(
                    isPremium = isPremium,
                    iconSize = iconSize,
                    cardSize = iconCardSize
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isPremium) "LinkArena Pro is active" else "Upgrade to LinkArena Pro",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp
                        ),
                        color = colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isPremium) {
                            "Ad-free organizing, AI grouping, and priority sync are enabled on this account."
                        } else {
                            "A cleaner bookmark workspace with no ads, AI grouping, color labels, and faster sync."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        maxLines = if (compact) 2 else 3
                    )
                }

                if (!compact) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeaderStat(
                            value = "No ads",
                            label = "clean browsing",
                            compact = compact,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStat(
                            value = "AI",
                            label = "auto grouping",
                            compact = compact,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderStat(
                            value = "Sync+",
                            label = "priority access",
                            compact = compact,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumIcon(
    isPremium: Boolean,
    iconSize: Dp,
    cardSize: Dp
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.size(iconSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(colorScheme.surface.copy(alpha = 0.35f))
        )
        Surface(
            modifier = Modifier.size(cardSize),
            shape = RoundedCornerShape(if (cardSize < 64.dp) 22.dp else 26.dp),
            color = colorScheme.surface,
            shadowElevation = 10.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_premium_foreground),
                    contentDescription = "LinkArena Pro",
                    modifier = Modifier.size(cardSize - 18.dp)
                )
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            shape = CircleShape,
            color = if (isPremium) colorScheme.primary else colorScheme.tertiary
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Filled.Check else Icons.Filled.Stars,
                contentDescription = if (isPremium) "Premium active" else "Premium upgrade",
                tint = if (isPremium) colorScheme.onPrimary else colorScheme.onTertiary,
                modifier = Modifier
                    .padding(6.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
private fun HeaderStat(
    value: String,
    label: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colorScheme.surface.copy(alpha = 0.44f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = if (compact) 8.dp else 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = colorScheme.onPrimaryContainer,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onPrimaryContainer.copy(alpha = 0.68f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PremiumBenefitTile(
    benefit: PremiumBenefit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 34.dp else 38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.primaryContainer.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = benefit.icon,
                        contentDescription = null,
                        tint = colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(if (compact) 18.dp else 20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = benefit.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = colorScheme.onSurface,
                maxLines = 2
            )
            Text(
                text = benefit.description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
                maxLines = if (compact) 1 else 3
            )
        }
    }
}

@Composable
private fun PremiumNote(
    isPremium: Boolean,
    compact: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (isPremium) {
            colorScheme.primaryContainer.copy(alpha = 0.56f)
        } else {
            colorScheme.surfaceVariant.copy(alpha = 0.7f)
        }
    ) {
        Row(
            modifier = Modifier.padding(if (compact) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (isPremium) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = if (isPremium) {
                    "Your subscription is active. Thanks for supporting LinkArena."
                } else {
                    "Your existing bookmarks stay safe. Pro only unlocks more control and a cleaner workspace."
                },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isPremium) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                lineHeight = 17.sp,
                maxLines = if (compact) 2 else 3
            )
        }
    }
}

@Composable
private fun PremiumActions(
    isPremium: Boolean,
    isLoading: Boolean,
    compact: Boolean,
    onGetPro: () -> Unit,
    onRestore: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
        ) {
            Button(
                onClick = onGetPro,
                enabled = !isLoading && !isPremium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 50.dp else 54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = colorScheme.primaryContainer,
                    disabledContentColor = colorScheme.onPrimaryContainer
                )
            ) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )

                    isPremium -> {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pro active",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    else -> {
                        Text(
                            text = "Continue with Pro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!isPremium) {
                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 42.dp else 46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = "Restore purchase",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
@Suppress("DEPRECATION")
private fun PremiumSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color
) {
    val view = LocalView.current
    val activity = view.context.findActivity() ?: return
    val background = MaterialTheme.colorScheme.background

    DisposableEffect(activity, statusBarColor, navigationBarColor, background) {
        val window = activity.window
        val previousStatusBar = window.statusBarColor
        val previousNavigationBar = window.navigationBarColor
        val controller = WindowCompat.getInsetsController(window, view)

        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true

        onDispose {
            window.statusBarColor = previousStatusBar.takeUnless { it == Color.Transparent.toArgb() }
                ?: background.toArgb()
            window.navigationBarColor = previousNavigationBar.takeUnless { it == Color.Transparent.toArgb() }
                ?: background.toArgb()
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun buildBenefitList(): List<PremiumBenefit> = listOf(
    PremiumBenefit(
        icon = Icons.Filled.AutoAwesome,
        title = "AI-powered grouping",
        description = "Automatically organize new bookmarks into useful groups."
    ),
    PremiumBenefit(
        icon = Icons.Filled.ColorLens,
        title = "Color labels",
        description = "Give each group a clear visual identity for faster scanning."
    ),
    PremiumBenefit(
        icon = Icons.Filled.Block,
        title = "Ad-free app",
        description = "Browse and organize without ad interruptions."
    ),
    PremiumBenefit(
        icon = Icons.Filled.CloudSync,
        title = "Priority sync",
        description = "Keep your saved links updated faster across devices."
    )
)

private fun openPlayStorePurchase(context: Context) {
    val packageName = context.packageName
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PremiumContentPreview() {
    LinkArenaTheme {
        PremiumContent(
            isPremium = false,
            isLoading = false,
            onNavigateBack = {},
            onGetPro = {},
            onRestore = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PremiumContentActivePreview() {
    LinkArenaTheme {
        PremiumContent(
            isPremium = true,
            isLoading = false,
            onNavigateBack = {},
            onGetPro = {},
            onRestore = {}
        )
    }
}
