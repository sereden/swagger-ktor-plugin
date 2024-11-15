package example.presentation.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import example.presentation.main.MainScreen

@Composable
fun Navigation(
    navController: NavHostController
) {
    CompositionLocalProvider(
        LocalProviders.LocalNavControllerProvider provides navController
    ) {
        NavHost(
            navController = navController,
            startDestination = "root",
            modifier = Modifier
                .fillMaxSize()
        ) {
            navigation(
                route = "root",
                startDestination = "main"
            ) {
                composable("main") {
                    MainScreen()
                }
            }
        }
    }
}