package com.bouzid.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bouzid.player.ui.activation.ActivationScreen
import com.bouzid.player.ui.activation.ActivationViewModel
import com.bouzid.player.ui.player.PlayerScreen
import com.bouzid.player.ui.player.PlayerViewModel
import com.bouzid.player.ui.theme.BouzidTheme
import com.bouzid.player.ui.urlinput.UrlInputScreen
import com.bouzid.player.ui.urlinput.UrlInputViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefsManager = (application as BouzidApp).preferencesManager
        val savedUrl = runBlocking { prefsManager.m3uUrl.first() }
        val alreadyActivated = runBlocking { prefsManager.isActivated.first() }

        setContent {
            BouzidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var step by remember {
                        mutableStateOf(
                            when {
                                savedUrl.isBlank() -> AppStep.UrlInput
                                !alreadyActivated -> AppStep.Activation
                                else -> AppStep.Player
                            }
                        )
                    }
                    var m3uUrl by remember { mutableStateOf(savedUrl) }

                    when (step) {
                        AppStep.UrlInput -> {
                            val urlVM: UrlInputViewModel = viewModel()
                            UrlInputScreen(
                                viewModel = urlVM,
                                onUrlSaved = {
                                    m3uUrl = runBlocking { prefsManager.m3uUrl.first() }
                                    step = AppStep.Activation
                                }
                            )
                        }
                        AppStep.Activation -> {
                            val activationVM: ActivationViewModel = viewModel()
                            ActivationScreen(
                                viewModel = activationVM,
                                onActivated = { step = AppStep.Player }
                            )
                        }
                        AppStep.Player -> {
                            val playerVM: PlayerViewModel = viewModel()
                            LaunchedEffect(m3uUrl) {
                                playerVM.loadChannels(m3uUrl)
                            }
                            PlayerScreen(
                                viewModel = playerVM,
                                onChangeUrl = { step = AppStep.UrlInput }
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class AppStep { UrlInput, Activation, Player }
