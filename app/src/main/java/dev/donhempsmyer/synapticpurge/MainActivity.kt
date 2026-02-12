package dev.donhempsmyer.synapticpurge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.donhempsmyer.synapticpurge.ui.screens.PurgeRoute
import dev.donhempsmyer.synapticpurge.ui.theme.AppTheme
import dev.donhempsmyer.synapticpurge.viewModels.PurgeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: PurgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent  {
            AppTheme {
                PurgeRoute(viewModel = viewModel)
            }
        }
    }
}

