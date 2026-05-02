package com.jorge.chargegridapp.core.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.jorge.chargegridapp.core.ui.screens.theme.ElectricGreen
import com.jorge.chargegridapp.core.ui.screens.theme.ElectricGreenDark

/**
 * A premium, high-contrast button with a sleek gradient and haptic feedback.
 * Designed to replace standard Material buttons for primary actions.
 */
@Composable
fun ChargeGridButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color>? = null,
    contentColor: Color? = null // Nullable to use theme default
) {
    val view = LocalView.current
    
    // Dynamic content color based on the theme if not provided
    val isDarkTheme = MaterialTheme.colorScheme.primary == ElectricGreen
    val finalContentColor = contentColor ?: if (isDarkTheme) Color.Black else Color.White

    val brush = if (enabled) {
        Brush.horizontalGradient(
            colors = gradientColors ?: listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primaryContainer
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
            .clickable(enabled = enabled) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) finalContentColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
