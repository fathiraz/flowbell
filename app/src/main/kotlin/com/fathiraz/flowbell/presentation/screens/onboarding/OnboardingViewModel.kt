package com.fathiraz.flowbell.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fathiraz.flowbell.domain.entities.UserPreferences
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isCompleting = MutableStateFlow(false)
    val isCompleting: StateFlow<Boolean> = _isCompleting.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences(isOnboardingCompleted = false))
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    init {
        // Observe user preferences
        viewModelScope.launch {
            userPreferencesRepository.getUserPreferences()
                .onEach { preferences ->
                    _userPreferences.value = preferences
                }
                .launchIn(this)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            _isCompleting.value = true
            try {
                val result = userPreferencesRepository.setOnboardingCompleted(true)
                if (result.isSuccess) {
                    Timber.i("Onboarding completed successfully")
                    _onboardingCompleted.value = true
                } else {
                    Timber.e("Failed to complete onboarding: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error completing onboarding")
            } finally {
                _isCompleting.value = false
            }
        }
    }
}