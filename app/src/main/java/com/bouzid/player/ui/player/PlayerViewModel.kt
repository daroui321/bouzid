package com.bouzid.player.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bouzid.player.data.Channel
import com.bouzid.player.data.M3UParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlayerUiState {
    data object Loading : PlayerUiState()
    data class ChannelsLoaded(
        val channels: List<Channel>,
        val selectedChannel: Channel?,
        val groups: List<String>,
        val selectedGroup: String?,
        val isPlaying: Boolean
    ) : PlayerUiState()
    data class Error(val message: String) : PlayerUiState()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    fun loadChannels(m3uUrl: String) {
        _state.value = PlayerUiState.Loading
        viewModelScope.launch {
            try {
                val channels = M3UParser.parse(m3uUrl)
                if (channels.isEmpty()) {
                    _state.value = PlayerUiState.Error("No channels found in playlist")
                    return@launch
                }
                val groups = channels.map { it.group }.distinct().sorted()
                _state.value = PlayerUiState.ChannelsLoaded(
                    channels = channels,
                    selectedChannel = channels.first(),
                    groups = groups,
                    selectedGroup = null,
                    isPlaying = false
                )
            } catch (e: Exception) {
                _state.value = PlayerUiState.Error(
                    "Failed to load channels: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    fun selectChannel(channel: Channel) {
        val current = _state.value
        if (current is PlayerUiState.ChannelsLoaded) {
            _state.value = current.copy(
                selectedChannel = channel,
                isPlaying = true
            )
        }
    }

    fun selectGroup(group: String?) {
        val current = _state.value
        if (current is PlayerUiState.ChannelsLoaded) {
            _state.value = current.copy(selectedGroup = group)
        }
    }

    fun getFilteredChannels(): List<Channel> {
        val current = _state.value
        if (current is PlayerUiState.ChannelsLoaded) {
            return if (current.selectedGroup != null) {
                current.channels.filter { it.group == current.selectedGroup }
            } else {
                current.channels
            }
        }
        return emptyList()
    }
}
