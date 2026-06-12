package com.bouzid.player.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bouzid_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val M3U_URL_KEY = stringPreferencesKey(Config.PREFS_M3U_URL)
        private val ACTIVATED_KEY = booleanPreferencesKey(Config.PREFS_ACTIVATED)
        private val EMAIL_KEY = stringPreferencesKey(Config.PREFS_EMAIL)
    }

    val m3uUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[M3U_URL_KEY] ?: ""
    }

    val isActivated: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ACTIVATED_KEY] ?: false
    }

    val savedEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EMAIL_KEY] ?: ""
    }

    suspend fun saveM3uUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[M3U_URL_KEY] = url
        }
    }

    suspend fun setActivated(email: String) {
        context.dataStore.edit { prefs ->
            prefs[ACTIVATED_KEY] = true
            prefs[EMAIL_KEY] = email
        }
    }
}
