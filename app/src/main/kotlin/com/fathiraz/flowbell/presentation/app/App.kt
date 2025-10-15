package com.fathiraz.flowbell

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.fathiraz.flowbell.core.utils.DebugToolsManager
import com.fathiraz.flowbell.core.di.dataModule
import com.fathiraz.flowbell.core.di.domainModule
import com.fathiraz.flowbell.core.di.presentationModule
// TODO: Fix NotificationWorkManager import after clean architecture refactoring
// import com.fathiraz.flowbell.utils.NotificationWorkManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import com.fathiraz.flowbell.core.utils.LoggerUtils
import com.fathiraz.flowbell.BuildConfig

// Beagle debug menu imports (noop in release builds)
import com.pandulapeter.beagle.Beagle
import com.pandulapeter.beagle.modules.AppInfoButtonModule
import com.pandulapeter.beagle.modules.DeveloperOptionsButtonModule
import com.pandulapeter.beagle.modules.DeviceInfoModule
import com.pandulapeter.beagle.modules.KeylineOverlaySwitchModule
import com.pandulapeter.beagle.modules.AnimationDurationSwitchModule
import com.pandulapeter.beagle.modules.ScreenCaptureToolboxModule
import com.pandulapeter.beagle.modules.LogListModule
import com.pandulapeter.beagle.modules.LifecycleLogListModule
import com.pandulapeter.beagle.modules.HeaderModule
import com.pandulapeter.beagle.modules.PaddingModule
import com.pandulapeter.beagle.modules.TextModule
import com.pandulapeter.beagle.modules.DividerModule
import com.pandulapeter.beagle.modules.NetworkLogListModule

class App : Application() {
  override fun onCreate() {
    super.onCreate()

    // Initialize Timber with Crashlytics integration
    initializeTimber()

    // Initialize debug tools (Chucker, Beagle)
    DebugToolsManager.initialize(this)

    // Initialize Koin
    startKoin {
      androidContext(this@App)
      modules(dataModule, domainModule, presentationModule)
    }
    LoggerUtils.App.d("Koin initialized")

    // Initialize Beagle debug menu
    initializeBeagle()

    // Initialize WorkManager for batch notification processing
    initializeWorkManager()
  }

  private fun initializeTimber() {
    // For now, always use DebugTree for development
    // In production, you can switch to CrashlyticsTree
    Timber.plant(Timber.DebugTree())
    LoggerUtils.App.d("Timber DebugTree initialized")

    // TODO: Add proper BuildConfig check when available
    // if (BuildConfig.DEBUG) {
    //   Timber.plant(Timber.DebugTree())
    // } else {
    //   Timber.plant(CrashlyticsTree())
    // }
  }


  private fun initializeBeagle() {
    try {
      // Initialize Beagle with Application context
      Beagle.initialize(application = this)
      LoggerUtils.App.d("Beagle initialized successfully")
      
      // Configure Beagle modules
      Beagle.set(
        HeaderModule(
          title = "FlowBell",
          subtitle = "com.fathiraz.flowbell",
          text = "Debug v${BuildConfig.VERSION_NAME}"
        ),
        AppInfoButtonModule(),
        DeveloperOptionsButtonModule(),
        PaddingModule(),
        TextModule("Device & System", TextModule.Type.SECTION_HEADER),
        DeviceInfoModule(),
        KeylineOverlaySwitchModule(),
        AnimationDurationSwitchModule(),
        ScreenCaptureToolboxModule(),
        DividerModule(),
        TextModule("Network & Logs", TextModule.Type.SECTION_HEADER),
        NetworkLogListModule(), // Network requests
        LogListModule(), // App logs
        LifecycleLogListModule() // Lifecycle events
      )
      LoggerUtils.App.d("Beagle modules configured successfully")
      
      // Plant a Timber tree that forwards logs to Beagle
      Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
          Beagle.log("[$tag] $message", tag ?: "App", t?.stackTraceToString())
        }
      })
      LoggerUtils.App.d("Beagle Timber integration configured")
    } catch (e: Exception) {
      LoggerUtils.App.e("Failed to initialize Beagle", e)
    }
  }

  private fun initializeWorkManager() {
    try {
      // TODO: Fix NotificationWorkManager after clean architecture refactoring
      // val workManager = NotificationWorkManager(this)
      // workManager.initialize()
      LoggerUtils.App.d("WorkManager initialization temporarily disabled")
    } catch (e: Exception) {
      LoggerUtils.App.e("Failed to initialize WorkManager", e)
    }
  }
}

/**
 * Custom Timber Tree that forwards logs to Firebase Crashlytics
 * Only logs warnings, errors, and critical issues to avoid spam
 */
class CrashlyticsTree : Timber.Tree() {
  private val crashlytics = FirebaseCrashlytics.getInstance()

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    // Only log warnings and above to Crashlytics to avoid spam
    if (priority >= android.util.Log.WARN) {
      // Set custom keys for better debugging
      crashlytics.setCustomKey("log_priority", priority)
      tag?.let { crashlytics.setCustomKey("log_tag", it) }
      
      // Log the message
      crashlytics.log("$tag: $message")
      
      // Log exceptions if present
      t?.let { crashlytics.recordException(it) }
    }
  }
}