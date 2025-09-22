package com.fathiraz.flowbell.presentation.components

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Component for displaying app package icons
 */
@Composable
fun PackageIcon(
    packageName: String,
    appName: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var appIcon by remember(packageName) { mutableStateOf<Drawable?>(null) }
    var isLoading by remember(packageName) { mutableStateOf(true) }

    LaunchedEffect(packageName) {
        try {
            isLoading = true
            val icon = withContext(Dispatchers.IO) {
                try {
                    val packageManager = context.packageManager
                    val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                    packageManager.getApplicationIcon(applicationInfo)
                } catch (e: PackageManager.NameNotFoundException) {
                    Timber.w("Package not found: $packageName")
                    null
                } catch (e: Exception) {
                    Timber.e(e, "Error loading icon for package: $packageName")
                    null
                }
            }
            appIcon = icon
        } catch (e: Exception) {
            Timber.e(e, "Error loading package icon for $packageName")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                // Loading placeholder
                Box(
                    modifier = Modifier
                        .size(size * 0.6f)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            appIcon != null -> {
                val currentIcon = appIcon
                val bitmap = remember(currentIcon) {
                    try {
                        currentIcon?.toBitmap(
                            width = (size.value * 2).toInt(), // Higher resolution for crisp display
                            height = (size.value * 2).toInt(),
                            config = android.graphics.Bitmap.Config.ARGB_8888
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error converting drawable to bitmap for $packageName")
                        null
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "$appName app icon",
                        modifier = Modifier.size(size * 0.8f)
                    )
                } else {
                    DefaultAppIcon(appName, size)
                }
            }
            else -> {
                DefaultAppIcon(appName, size)
            }
        }
    }
}

/**
 * Default icon when app icon cannot be loaded
 */
@Composable
private fun DefaultAppIcon(
    appName: String,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Show first letter of app name or Android icon
        if (appName.isNotEmpty()) {
            androidx.compose.material3.Text(
                text = appName.first().uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = "Default app icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/**
 * Small package icon for compact display
 */
@Composable
fun SmallPackageIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    PackageIcon(
        packageName = packageName,
        appName = appName,
        size = 24.dp,
        modifier = modifier
    )
}

/**
 * Medium package icon for standard list items
 */
@Composable
fun MediumPackageIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    PackageIcon(
        packageName = packageName,
        appName = appName,
        size = 40.dp,
        modifier = modifier
    )
}

/**
 * Large package icon for detailed views
 */
@Composable
fun LargePackageIcon(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier
) {
    PackageIcon(
        packageName = packageName,
        appName = appName,
        size = 56.dp,
        modifier = modifier
    )
}