package com.fathiraz.flowbell.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

/**
 * Enhanced Button with microinteractions and animations
 */
@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(150),
        label = "button_elevation"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(scale),
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = elevation
        ),
        interactionSource = interactionSource
    ) {
        Text(text = text)
    }
}

/**
 * Enhanced Card with hover/press states and elevation changes
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_elevation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null, // Using Material3's default ripple
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        content = content
    )
}

/**
 * Animated List Item with slide-in and fade-in effects
 */
@Composable
fun AnimatedListItem(
    visible: Boolean = true,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = animationDelay,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = animationDelay,
                easing = EaseOutCubic
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(
                durationMillis = 200,
                easing = EaseInCubic
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 200,
                easing = EaseInCubic
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Loading skeleton animation component
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    width: Dp = 200.dp,
    cornerRadius: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton_transition")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
    )
}

/**
 * Success animation component with Lottie
 */
@Composable
fun SuccessAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        // For now, using a simple animated checkmark
        // In a real implementation, you'd use a Lottie animation file
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(size / 2)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Error animation component with shake effect
 */
@Composable
fun ErrorAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val shakeOffset by remember { mutableStateOf(Animatable(0f)) }

    LaunchedEffect(visible) {
        if (visible) {
            // Shake animation
            repeat(3) {
                shakeOffset.animateTo(
                    targetValue = 10f,
                    animationSpec = tween(50)
                )
                shakeOffset.animateTo(
                    targetValue = -10f,
                    animationSpec = tween(50)
                )
            }
            shakeOffset.animateTo(0f)
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier.offset(x = shakeOffset.value.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(
                    MaterialTheme.colorScheme.error,
                    RoundedCornerShape(size / 2)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✗",
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

/**
 * Progress indicator with smooth animations
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "progress_animation"
    )

    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color,
        trackColor = backgroundColor
    )
}

/**
 * Floating Action Button with enhanced animations
 */
@Composable
fun AnimatedFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fab_scale"
    )

    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            icon = icon,
            text = text,
            interactionSource = interactionSource
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            interactionSource = interactionSource
        ) {
            icon()
        }
    }
}

/**
 * Page transition wrapper with smooth animations
 */
@Composable
fun PageTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 8 },
            animationSpec = tween(400, easing = EaseOutCubic)
        ) + fadeIn(
            animationSpec = tween(400, easing = EaseOutCubic)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it / 8 },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(
            animationSpec = tween(300, easing = EaseInCubic)
        ),
        modifier = modifier
    ) {
        content()
    }
}