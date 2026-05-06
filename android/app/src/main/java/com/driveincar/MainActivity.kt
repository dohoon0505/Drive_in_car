package com.driveincar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.driveincar.ui.nav.AppNavGraph
import com.driveincar.ui.nav.RootDestination
import com.driveincar.ui.splash.SplashViewModel
import com.driveincar.ui.theme.DriveInCarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DriveInCarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val splashVm: SplashViewModel = hiltViewModel()
                    val rootDestination by splashVm.rootDestination.collectAsStateWithLifecycle()

                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        startDestination = rootDestination
                    )
                }
            }
        }
    }
}
