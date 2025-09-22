package com.fathiraz.flowbell.presentation.screens.webhook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import com.fathiraz.flowbell.core.utils.HttpRequestUtils
import com.fathiraz.flowbell.core.utils.WebhookResult
import com.fathiraz.flowbell.domain.repositories.UserPreferencesRepository
import android.content.Context

data class WebhookUiState(
  val webhookUrl: String = "",
  val isValid: Boolean = false,
  val errorMessage: String? = null,
  val isSaving: Boolean = false,
  val saveSuccess: Boolean = false,
  val currentWebhookUrl: String? = null,
  val isTesting: Boolean = false,
  val testResult: String? = null,
  val testSuccess: Boolean = false,
  val isEditing: Boolean = false
)

sealed interface WebhookEvent {
  object Save : WebhookEvent
  object Clear : WebhookEvent
  object Test : WebhookEvent
  object TestOnly : WebhookEvent  // Test without saving
  object ClearSuccess : WebhookEvent
  object ClearTestResult : WebhookEvent  // Clear test result
  object StartEdit : WebhookEvent
  object CancelEdit : WebhookEvent
  data class UpdateUrl(val url: String) : WebhookEvent
}

class WebhookViewModel(
  private val context: Context,
  private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(WebhookUiState())
  val uiState: StateFlow<WebhookUiState> = _uiState.asStateFlow()
  
  private val httpRequest = HttpRequestUtils(context)

  init {
    // Load saved webhook URL from preferences
    viewModelScope.launch {
      userPreferencesRepository.getUserPreferences().collect { preferences ->
        val savedUrl = preferences.webhookUrl.takeIf { it.isNotEmpty() }
        _uiState.value = _uiState.value.copy(
          currentWebhookUrl = savedUrl,
          isValid = savedUrl != null && validateUrl(savedUrl).isValid
        )
      }
    }
  }

  fun onEvent(event: WebhookEvent) {
    when (event) {
      is WebhookEvent.UpdateUrl -> {
        val validationResult = validateUrl(event.url)
        _uiState.value = _uiState.value.copy(
          webhookUrl = event.url,
          isValid = validationResult.isValid,
          errorMessage = validationResult.errorMessage
        )
      }
      WebhookEvent.Save -> {
        if (_uiState.value.isValid) {
          viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
              // Save to DataStore via repository
              Timber.d("Saving webhook URL: ${_uiState.value.webhookUrl}")
              val result = userPreferencesRepository.updateWebhookUrl(_uiState.value.webhookUrl)
              
              if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                  isSaving = false,
                  saveSuccess = true,
                  currentWebhookUrl = _uiState.value.webhookUrl,
                  isEditing = false,
                  errorMessage = null,  // Clear any previous errors
                  testResult = null     // Clear any previous test results
                )
                Timber.i("Webhook URL saved successfully")
              } else {
                _uiState.value = _uiState.value.copy(
                  isSaving = false,
                  errorMessage = "Failed to save webhook URL: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                )
                Timber.e(result.exceptionOrNull(), "Failed to save webhook URL")
              }
            } catch (e: Exception) {
              _uiState.value = _uiState.value.copy(
                isSaving = false,
                errorMessage = "Failed to save webhook URL: ${e.message}"
              )
              Timber.e(e, "Failed to save webhook URL")
            }
          }
        }
      }
      WebhookEvent.Clear -> {
        viewModelScope.launch {
          try {
            // Clear from DataStore
            val result = userPreferencesRepository.updateWebhookUrl("")
            if (result.isSuccess) {
              _uiState.value = _uiState.value.copy(
                webhookUrl = "",
                isValid = false,
                errorMessage = null,
                testResult = null,
                testSuccess = false,
                currentWebhookUrl = null
              )
              Timber.i("Webhook URL cleared successfully")
            } else {
              Timber.e(result.exceptionOrNull(), "Failed to clear webhook URL")
            }
          } catch (e: Exception) {
            Timber.e(e, "Failed to clear webhook URL")
          }
        }
      }
      WebhookEvent.Test -> {
        // Test & Save combined action (for backward compatibility)
        if (_uiState.value.isValid) {
          viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, testResult = null)
            try {
              Timber.d("Testing and saving webhook URL: ${_uiState.value.webhookUrl}")

              // First test the webhook
              val testResult = httpRequest.testWebhook(_uiState.value.webhookUrl)

              if (testResult is WebhookResult.Success) {
                // If test successful, save the URL
                val saveResult = userPreferencesRepository.updateWebhookUrl(_uiState.value.webhookUrl)

                if (saveResult.isSuccess) {
                  _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    testResult = "✅ Webhook tested and saved successfully!",
                    testSuccess = true,
                    saveSuccess = true,
                    currentWebhookUrl = _uiState.value.webhookUrl,
                    isEditing = false
                  )
                  Timber.i("Webhook URL tested and saved successfully")
                } else {
                  _uiState.value = _uiState.value.copy(
                    isTesting = false,
                    testResult = "✅ Test successful, but save failed: ${saveResult.exceptionOrNull()?.message}",
                    testSuccess = true
                  )
                }
              } else {
                _uiState.value = _uiState.value.copy(
                  isTesting = false,
                  testResult = "❌ Webhook test failed: ${(testResult as WebhookResult.Error).message}",
                  testSuccess = false
                )
              }
            } catch (e: Exception) {
              _uiState.value = _uiState.value.copy(
                isTesting = false,
                testResult = "❌ Test failed: ${e.message ?: "Connection error"}",
                testSuccess = false
              )
              Timber.e(e, "Webhook test failed")
            }
          }
        }
      }

      WebhookEvent.TestOnly -> {
        // Test without saving
        val urlToTest = _uiState.value.webhookUrl
        if (urlToTest.isNotBlank() && _uiState.value.isValid) {
          viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true, testResult = null)
            try {
              Timber.d("Testing webhook URL (no save): $urlToTest")

              val result = httpRequest.testWebhook(urlToTest)

              _uiState.value = _uiState.value.copy(
                isTesting = false,
                testResult = when (result) {
                  is WebhookResult.Success -> {
                    "✅ Webhook test successful! Ready to save."
                  }
                  is WebhookResult.Error -> {
                    "❌ Webhook test failed: ${result.message}"
                  }
                },
                testSuccess = result is WebhookResult.Success
              )
            } catch (e: Exception) {
              _uiState.value = _uiState.value.copy(
                isTesting = false,
                testResult = "❌ Webhook test failed: ${e.message ?: "Connection error"}",
                testSuccess = false
              )
              Timber.e(e, "Webhook test failed")
            }
          }
        }
      }
      WebhookEvent.ClearSuccess -> {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
      }
      WebhookEvent.ClearTestResult -> {
        _uiState.value = _uiState.value.copy(testResult = null, testSuccess = false)
      }
      WebhookEvent.StartEdit -> {
        val currentUrl = _uiState.value.currentWebhookUrl ?: ""
        val validationResult = if (currentUrl.isNotEmpty()) validateUrl(currentUrl) else ValidationResult(false)
        _uiState.value = _uiState.value.copy(
          isEditing = true,
          webhookUrl = currentUrl,
          isValid = validationResult.isValid,
          errorMessage = validationResult.errorMessage
        )
      }
      WebhookEvent.CancelEdit -> {
        _uiState.value = _uiState.value.copy(
          isEditing = false,
          webhookUrl = _uiState.value.currentWebhookUrl ?: "",
          isValid = _uiState.value.currentWebhookUrl != null,
          errorMessage = null
        )
      }
    }
  }

  private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
  )

  private fun validateUrl(url: String): ValidationResult {
    return when {
      url.isBlank() -> ValidationResult(false, "Webhook URL is required")
      !url.startsWith("https://") -> ValidationResult(false, "URL must use HTTPS protocol for security")
      url.length < 12 -> ValidationResult(false, "URL is too short")
      !isValidUrlFormat(url) -> ValidationResult(false, "Invalid URL format")
      else -> ValidationResult(true)
    }
  }

  private fun isValidUrlFormat(url: String): Boolean {
    return try {
      val urlPattern = Regex(
        "^https://" +
        "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)*" +
        "[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?" +
        "(:[0-9]{1,5})?" +
        "(/.*)?$"
      )
      urlPattern.matches(url)
    } catch (e: Exception) {
      Timber.w(e, "Error validating URL format")
      false
    }
  }
}