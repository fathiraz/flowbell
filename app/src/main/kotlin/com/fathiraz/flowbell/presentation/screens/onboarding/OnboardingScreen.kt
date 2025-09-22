package com.fathiraz.flowbell.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fathiraz.flowbell.R
import kotlinx.coroutines.delay

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableStateOf(0) }
    var showRevealAnimation by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to FlowBell",
            description = "Forward your notifications to any webhook endpoint securely and reliably",
            icon = Icons.Default.Notifications
        ),
        OnboardingPage(
            title = "Monitor Your Apps",
            description = "Select which applications you want to monitor and forward notifications from",
            icon = Icons.Default.Apps
        ),
        OnboardingPage(
            title = "Secure Webhooks",
            description = "Configure your webhook endpoints with built-in security and retry mechanisms",
            icon = Icons.Default.Webhook
        ),
        OnboardingPage(
            title = "Privacy First",
            description = "Your notification data stays on your device. We never store or transmit personal information",
            icon = Icons.Default.Security
        )
    )

    LaunchedEffect(Unit) {
        delay(500)
        showRevealAnimation = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (showRevealAnimation) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(1000, easing = EaseOutCubic)
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(1000, easing = EaseOutCubic)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo and app name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 48.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "FlowBell Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "FlowBell",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Onboarding content
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                        },
                        modifier = Modifier.weight(1f),
                        label = "onboarding_content"
                    ) { page ->
                        OnboardingPageContent(
                            page = pages[page],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Page indicators and navigation
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        // Page indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 32.dp)
                        ) {
                            pages.forEachIndexed { index, _ ->
                                Box(
                                    modifier = Modifier
                                        .size(
                                            width = if (index == currentPage) 24.dp else 8.dp,
                                            height = 8.dp
                                        )
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (index == currentPage)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .animateContentSize()
                                )
                            }
                        }

                        // Navigation buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Skip button
                            if (currentPage < pages.size - 1) {
                                TextButton(
                                    onClick = { onComplete() }
                                ) {
                                    Text(
                                        "Skip",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(64.dp))
                            }

                            // Next/Get Started button
                            Button(
                                onClick = {
                                    if (currentPage < pages.size - 1) {
                                        currentPage++
                                    } else {
                                        onComplete()
                                    }
                                },
                                modifier = Modifier.widthIn(min = 120.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (currentPage < pages.size - 1) "Next" else "Get Started",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    var iconVisible by remember { mutableStateOf(false) }

    LaunchedEffect(page) {
        iconVisible = false
        delay(100)
        iconVisible = true
    }

    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        AnimatedVisibility(
            visible = iconVisible,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(400))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title with slide-in animation
        AnimatedVisibility(
            visible = iconVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(600, delayMillis = 200)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description with slide-in animation
        AnimatedVisibility(
            visible = iconVisible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(600, delayMillis = 400)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
        ) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}