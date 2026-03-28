package com.sayeedjoy.linkarena.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.sayeedjoy.linkarena.domain.model.Group

@Composable
fun GroupChip(
    group: Group,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = group.color?.let {
        try {
            Color(android.graphics.Color.parseColor(it))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }
    } ?: MaterialTheme.colorScheme.primary
    val isLightChip = chipColor.luminance() > 0.5f
    val selectedContainerColor = chipColor
    val unselectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
    val unselectedContentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(percent = 50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else unselectedContainerColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (group.color != null) {
                ColorDot(color = group.color, size = 8)
            }
            Text(
                text = group.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) selectedContentColor else unselectedContentColor
            )
        }
    }
}

@Composable
fun ColorDot(
    color: String,
    size: Int = 12,
    modifier: Modifier = Modifier
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(parsedColor)
    )
}

@Composable
fun SelectableColorDot(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
