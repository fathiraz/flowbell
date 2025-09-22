package com.fathiraz.flowbell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import timber.log.Timber
import com.fathiraz.flowbell.data.preferences.ThemePreferences
import com.fathiraz.flowbell.presentation.FlowBellApp
import com.fathiraz.flowbell.presentation.screens.splash.SimpleSplashScreen
import com.fathiraz.flowbell.presentation.screens.onboarding.OnboardingScreen
import com.fathiraz.flowbell.presentation.screens.permissions.PermissionScreen
import com.fathiraz.flowbell.presentation.screens.onboarding.OnboardingViewModel
import com.fathiraz.flowbell.presentation.theme.AppTheme
import org.koin.androidx.compose.koinViewModel
import com.fathiraz.flowbell.core.utils.LoggerUtils

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Log activity creation for debugging
    LoggerUtils.d("MainActivity", "onCreate called")
    LoggerUtils.App.i("MainActivity started successfully")

    setContent {
      val themePreferences = remember { ThemePreferences(this@MainActivity) }
      val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)

      AppTheme(darkTheme = isDarkMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainApp(themePreferences = themePreferences)
        }
      }
    }
  }
}

@Composable
private fun MainApp(themePreferences: ThemePreferences) {
  var showSplash by remember { mutableStateOf(true) }
  var showOnboarding by remember { mutableStateOf(false) }
  var showPermission by remember { mutableStateOf(false) }
  var freshInstallHandled by remember { mutableStateOf(false) }
  val onboardingViewModel: OnboardingViewModel = koinViewModel()
  val context = LocalContext.current

  // Handle fresh install detection first
  LaunchedEffect(Unit) {
    if (!freshInstallHandled) {
      try {
        val dataStoreManager = com.fathiraz.flowbell.data.preferences.DataStoreManager(context)
        dataStoreManager.handleFreshInstallIfNeeded()
        freshInstallHandled = true
      } catch (e: Exception) {
        Timber.e(e, "Error handling fresh install")
        freshInstallHandled = true // Continue even if it fails
      }
    }
  }

  // Collect user preferences to check onboarding status
  val userPreferences by onboardingViewModel.userPreferences.collectAsState()

  // Check onboarding and permission status when splash completes
  LaunchedEffect(showSplash, userPreferences.isOnboardingCompleted, freshInstallHandled) {
    if (!showSplash && freshInstallHandled) {
      if (!userPreferences.isOnboardingCompleted) {
        showOnboarding = true
        showPermission = false
      } else {
        // Check if notification permission is granted
        val hasPermission = com.fathiraz.flowbell.core.utils.PermissionUtils.isNotificationListenerPermissionGranted(context)
        showOnboarding = false
        showPermission = !hasPermission
      }
    }
  }

  // Listen for onboarding completion
  val onboardingCompleted by onboardingViewModel.onboardingCompleted.collectAsState()
  LaunchedEffect(onboardingCompleted) {
    if (onboardingCompleted) {
      showOnboarding = false
      // After onboarding, check permission
      val hasPermission = com.fathiraz.flowbell.core.utils.PermissionUtils.isNotificationListenerPermissionGranted(context)
      showPermission = !hasPermission
    }
  }

  when {
    !freshInstallHandled || showSplash -> {
      SimpleSplashScreen(
        onNavigateToMain = {
          if (freshInstallHandled) {
            showSplash = false
          }
        }
      )
    }
    showOnboarding -> {
      OnboardingScreen(
        onComplete = {
          onboardingViewModel.completeOnboarding()
        }
      )
    }
    showPermission -> {
      PermissionScreen(
        onPermissionGranted = {
          showPermission = false
        }
      )
    }
    else -> {
      FlowBellApp(themePreferences = themePreferences)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun FlowBellAppPreview() {
  AppTheme {
    // Preview with mock theme preferences
  }
}
