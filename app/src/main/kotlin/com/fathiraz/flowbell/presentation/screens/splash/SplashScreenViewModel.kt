package com.fathiraz.flowbell.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for SplashScreen handling initialization logic
 */
class SplashScreenViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _navigationReady = MutableStateFlow(false)
    val navigationReady: StateFlow<Boolean> = _navigationReady.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            // Simulate initialization time
            delay(2000)

            // Mark loading as complete
            _isLoading.value = false

            // Enable navigation
            delay(500)
            _navigationReady.value = true
        }
    }

    fun onNavigationComplete() {
        _navigationReady.value = false
    }
}