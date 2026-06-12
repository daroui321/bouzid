package com.bouzid.player.ui.activation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bouzid.player.data.ActivationApi
import com.bouzid.player.data.ActivationRequest
import com.bouzid.player.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActivationState {
    data object Loading : ActivationState()
    data object NotRequired : ActivationState()
    data class Ready(
        val configMessage: String = "",
        val isSubmitting: Boolean = false,
        val error: String? = null
    ) : ActivationState()
    data class Success(val email: String) : ActivationState()
}

class ActivationViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PreferencesManager(application)

    private val _state = MutableStateFlow<ActivationState>(ActivationState.Loading)
    val state: StateFlow<ActivationState> = _state.asStateFlow()

    init {
        checkActivation()
    }

    private fun checkActivation() {
        viewModelScope.launch {
            try {
                val config = ActivationApi.service.getConfig()
                if (config.activation.enabled) {
                    _state.value = ActivationState.Ready(config.activation.message)
                } else {
                    _state.value = ActivationState.NotRequired
                }
            } catch (_: Exception) {
                _state.value = ActivationState.NotRequired
            }
        }
    }

    fun submitEmail(email: String) {
        if (!isValidEmail(email)) {
            val current = _state.value
            if (current is ActivationState.Ready) {
                _state.value = current.copy(error = "Please enter a valid email address")
            }
            return
        }
        val current = _state.value
        if (current is ActivationState.Ready) {
            _state.value = current.copy(isSubmitting = true, error = null)
        }
        viewModelScope.launch {
            try {
                val response = ActivationApi.service.activate(ActivationRequest(email))
                if (response.success) {
                    prefsManager.setActivated(email)
                    _state.value = ActivationState.Success(email)
                } else {
                    val s = _state.value
                    if (s is ActivationState.Ready) {
                        _state.value = s.copy(
                            isSubmitting = false,
                            error = response.message.ifBlank { "Activation failed" }
                        )
                    }
                }
            } catch (_: Exception) {
                val s = _state.value
                if (s is ActivationState.Ready) {
                    _state.value = s.copy(
                        isSubmitting = false,
                        error = "Connection failed. Check your internet."
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
