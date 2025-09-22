package com.fathiraz.flowbell.core.di

import com.fathiraz.flowbell.domain.usecases.GetNotificationStatisticsUseCase
import com.fathiraz.flowbell.domain.usecases.HandleNotificationQueueUseCase
import com.fathiraz.flowbell.domain.usecases.ManageAppPreferencesUseCase
import com.fathiraz.flowbell.domain.usecases.ManageUserSettingsUseCase
import com.fathiraz.flowbell.domain.usecases.SendWebhookUseCase
import org.koin.dsl.module

/**
 * Koin DI module for domain layer dependencies.
 * This module provides all use cases following clean architecture principles.
 */
val domainModule = module {
    
    // Use cases
    factory { 
        GetNotificationStatisticsUseCase(get()) 
    }
    
    factory { 
        SendWebhookUseCase(get()) 
    }
    
    factory { 
        ManageAppPreferencesUseCase(get()) 
    }
    
    factory { 
        HandleNotificationQueueUseCase(get()) 
    }
    
    factory { 
        ManageUserSettingsUseCase(get()) 
    }
}