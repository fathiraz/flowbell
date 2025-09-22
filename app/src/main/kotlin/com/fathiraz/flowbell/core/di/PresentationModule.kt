package com.fathiraz.flowbell.core.di

import com.fathiraz.flowbell.presentation.screens.analytics.AnalyticsViewModel
import com.fathiraz.flowbell.presentation.screens.dashboard.DashboardViewModel
import com.fathiraz.flowbell.presentation.screens.notifications.NotificationHistoryViewModel
import com.fathiraz.flowbell.presentation.screens.settings.SettingsViewModel
import com.fathiraz.flowbell.presentation.screens.notifications.AppListViewModel
import com.fathiraz.flowbell.presentation.screens.webhook.WebhookViewModel
import com.fathiraz.flowbell.presentation.screens.splash.SplashScreenViewModel
import com.fathiraz.flowbell.presentation.screens.onboarding.OnboardingViewModel
import com.fathiraz.flowbell.domain.repositories.AppRepository
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for presentation layer dependencies.
 * This module provides all ViewModels following clean architecture principles.
 */
val presentationModule = module {
    
    // ViewModels
    viewModel {
        SplashScreenViewModel()
    }

    viewModel {
        OnboardingViewModel(get<UserPreferencesRepository>())
    }
    
    
    viewModel {
        WebhookViewModel(androidContext(), get<UserPreferencesRepository>())
    }
    
    viewModel {
        AppListViewModel(get<AppRepository>())
    }
    
    viewModel {
        NotificationHistoryViewModel(get<UserPreferencesRepository>(), get())
    }
    
    viewModel {
        SettingsViewModel(get<UserPreferencesRepository>())
    }
    
    viewModel {
        DashboardViewModel(androidContext(), get<UserPreferencesRepository>(), get<NotificationStatisticsRepository>(), get())
    }
    
    viewModel {
        AnalyticsViewModel(get<NotificationStatisticsRepository>())
    }
}