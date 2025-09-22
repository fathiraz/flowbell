package com.fathiraz.flowbell

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
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

class App : Application() {
  override fun onCreate() {
    super.onCreate()

    // Initialize Timber with Crashlytics integration
    initializeTimber()

    // Initialize Chucker HTTP inspector
    initializeChucker()

    // Initialize Koin
    startKoin {
      androidContext(this@App)
      modules(dataModule, domainModule, presentationModule)
    }
    LoggerUtils.App.d("Koin initialized")

    // Hyperion debugging tools initialize automatically in debug builds
    // Shake device or call Hyperion.open(activity) to access debugging drawer
    LoggerUtils.App.d("Hyperion debugging tools available (shake to activate)")

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

  private fun initializeChucker() {
    // Chucker collector for HTTP inspection
    val chuckerCollector = ChuckerCollector(
      context = this,
      // Keep HTTP transaction data for up to 7 days
      retentionPeriod = RetentionManager.Period.ONE_WEEK
    )

    LoggerUtils.App.d("Chucker HTTP inspector initialized")
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