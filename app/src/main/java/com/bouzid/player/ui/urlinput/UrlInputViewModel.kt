package com.bouzid.player.ui.urlinput

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bouzid.player.data.PreferencesManager
import kotlinx.coroutines.launch

class UrlInputViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PreferencesManager(application)

    fun saveUrl(url: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            prefsManager.saveM3uUrl(url)
            onSaved()
        }
    }
}
