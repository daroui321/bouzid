package com.bouzid.player

import android.app.Application
import com.bouzid.player.data.PreferencesManager

class BouzidApp : Application() {
    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
    }
}
