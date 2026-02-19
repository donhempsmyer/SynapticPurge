package dev.donhempsmyer.synapticpurge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.donhempsmyer.synapticpurge.ui.PurgeAppRoot
import dev.donhempsmyer.synapticpurge.ui.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent  {
            AppTheme {
                PurgeAppRoot()
            }
        }
    }
}

