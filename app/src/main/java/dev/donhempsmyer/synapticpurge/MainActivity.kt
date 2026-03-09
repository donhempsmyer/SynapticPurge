package dev.donhempsmyer.synapticpurge

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.donhempsmyer.synapticpurge.navigation.PurgeAppRoot
import dev.donhempsmyer.synapticpurge.ui.screens.settings.SettingsViewModel
import dev.donhempsmyer.synapticpurge.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val settings by settingsVm.settings.collectAsStateWithLifecycle()

            AppTheme(themeMode = settings.themeMode) {
                PurgeAppRoot()
            }
        }
    }
}

