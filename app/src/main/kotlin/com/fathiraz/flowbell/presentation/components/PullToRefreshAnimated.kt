package com.fathiraz.flowbell.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Enhanced Pull-to-Refresh component with smooth animations
 */
@Composable
fun AnimatedPullToRefreshIndicator(
    refreshing: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )

    val scale by animateFloatAsState(
        targetValue = if (refreshing) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    if (refreshing) {
        Surface(
            modifier = modifier
                .size(40.dp)
                .rotate(rotation),
            shape = CircleShape,
            color = backgroundColor,
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

/**
 * Custom Pull-to-Refresh wrapper with enhanced animations
 * Note: Using the existing Material3 PullToRefreshBox implementation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // For now, we'll use the standard Material3 pull-to-refresh
    // In the future, we can enhance this with custom animations
    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        content()
    }
}