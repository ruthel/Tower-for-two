package com.crabscode.towerfortwo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.crabscode.towerfortwo.ui.TowerForTwoApp
import com.crabscode.towerfortwo.ui.theme.TowerForTwoTheme
import com.crabscode.towerfortwo.viewmodel.TowerViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TowerViewModel by viewModels { TowerViewModel.factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TowerForTwoTheme {
                TowerForTwoApp(viewModel)
            }
        }
    }
}
