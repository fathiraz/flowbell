package com.fathiraz.flowbell.core.di

import com.fathiraz.flowbell.data.local.database.FlowBellDatabase
import com.fathiraz.flowbell.data.local.database.dao.AppPreferencesDao
import com.fathiraz.flowbell.data.local.database.dao.NotificationQueueDao
import com.fathiraz.flowbell.data.local.database.dao.UserPreferencesDao
import com.fathiraz.flowbell.data.local.database.repository.AppPreferencesRepository
import com.fathiraz.flowbell.data.preferences.DataStoreManager
import com.fathiraz.flowbell.data.repositories.AppRepositoryImpl
import com.fathiraz.flowbell.data.repositories.NotificationQueueRepositoryImpl
import com.fathiraz.flowbell.data.repositories.NotificationStatisticsRepositoryImpl
import com.fathiraz.flowbell.data.repositories.UserPreferencesRepositoryImpl
import com.fathiraz.flowbell.data.repositories.UserPreferencesRoomRepository
import com.fathiraz.flowbell.domain.repositories.AppRepository
import com.fathiraz.flowbell.domain.repositories.NotificationQueueRepository
import com.fathiraz.flowbell.domain.repositories.NotificationStatisticsRepository
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import com.fathiraz.flowbell.core.utils.HttpRequestUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin DI module for data layer dependencies.
 * This module provides all data layer implementations following clean architecture principles.
 */
val dataModule = module {
    
    // Database
    single { FlowBellDatabase.getDatabase(androidContext()) }
    single { get<FlowBellDatabase>().appPreferencesDao() }
    single { get<FlowBellDatabase>().notificationQueueDao() }
    single { get<FlowBellDatabase>().userPreferencesDao() }
    
    // Data sources
    single { DataStoreManager(androidContext()) }

    // App preferences repository (Room-based)
    single { AppPreferencesRepository(get<AppPreferencesDao>()) }

    // Repository implementations
    single<AppRepository> {
        AppRepositoryImpl(androidContext(), get<AppPreferencesRepository>())
    }

    single<UserPreferencesRepository> {
        UserPreferencesRoomRepository(get<UserPreferencesDao>())
    }

    single<NotificationQueueRepository> {
        NotificationQueueRepositoryImpl(get<NotificationQueueDao>())
    }

    single<NotificationStatisticsRepository> {
        NotificationStatisticsRepositoryImpl(get<NotificationQueueDao>())
    }

    single<NotificationStatisticsRepository> {
        NotificationStatisticsRepositoryImpl(get<NotificationQueueDao>())
    }

    // Utils
    single { HttpRequestUtils(androidContext()) }
}