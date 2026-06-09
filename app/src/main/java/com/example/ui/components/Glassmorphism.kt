package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicDeepSpace
import com.example.ui.theme.CosmicNavyOutline
import com.example.ui.theme.FrostWhite
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonMagenta

/**
 * Renders a premium, cinematic blue-to-purple-to-dark backdrop gradient background.
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val bgBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF03050C), // Ultra deep navy
                Color(0xFF0A0F1E), // Deep cosmic violet
                Color(0xFF060914)  // Pitch black slate
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEFF3F9), // Ice blue frost
                Color(0xFFFFFFFF), // Pure white
                Color(0xFFF3E8FF)  // Warm soft lavender
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .drawBehind {
                // Ambient glow in corner of the screen
                if (isDark) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonBlue.copy(alpha = 0.12f), Color.Transparent)
                        ),
                        radius = size.width * 0.8f,
                        center = Offset(size.width * 0.1f, size.height * 0.2f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonPurple.copy(alpha = 0.10f), Color.Transparent)
                        ),
                        radius = size.width * 0.9f,
                        center = Offset(size.width * 0.9f, size.height * 0.8f)
                    )
                } else {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFC7D2FE).copy(alpha = 0.25f), Color.Transparent)
                        ),
                        radius = size.width * 0.7f,
                        center = Offset(size.width * 0.8f, size.height * 0.1f)
                    )
                }
            },
        content = content
    )
}

/**
 * Creates an elegant semi-transparent Glass-morphic card.
 */
@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    paddingValue: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    val glassBgColor = if (isDark) {
        Color(0xFF0E1324).copy(alpha = 0.65f) // Carbon glass dark
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.75f) // Frost glass light
    }

    val glassBorderBrush = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.02f),
                NeonPurple.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color.White.copy(alpha = 0.8f),
                Color.Black.copy(alpha = 0.03f)
            )
        )
    }

    val modifierWithClick = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
    } else {
        modifier.clip(RoundedCornerShape(cornerRadius))
    }

    Column(
        modifier = modifierWithClick
            .background(glassBgColor)
            .border(borderWidth, glassBorderBrush, RoundedCornerShape(cornerRadius))
            .padding(paddingValue),
        content = content
    )
}

/**
 * Glassy premium action button.
 */
@Composable
fun GlassyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    primaryColor: Color = NeonBlue,
    height: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor,
                        NeonPurple
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}
