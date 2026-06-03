package it.uliveto.browser.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uliveto.browser.di.AppContainer
import it.uliveto.browser.di.ViewModelFactory
import it.uliveto.browser.ui.screens.browser.BrowserScreen
import it.uliveto.browser.ui.screens.browser.BrowserViewModel
import it.uliveto.browser.ui.screens.start.StartScreen
import it.uliveto.browser.ui.screens.start.StartViewModel

@Composable
fun UlivetoNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier,
    ) {
        composable("start") {
            val startVmFactory = ViewModelFactory {
                StartViewModel(appContainer.userPrefsRepository)
            }
            val startVm: StartViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = startVmFactory)
            StartScreen(
                viewModel = startVm,
                onNavigateToBrowser = { url ->
                    navController.navigate("browser?url=${Uri.encode(url)}")
                },
                onNavigateToTabs = { navController.navigate("tabs") },
                onNavigateToBookmarks = { navController.navigate("bookmarks") },
                onNavigateToSettings = { navController.navigate("settings") },
            )
        }
        composable("browser?url={url}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: "about:blank"
            val browserVmFactory = ViewModelFactory { BrowserViewModel() }
            BrowserScreen(
                runtime = appContainer.geckoRuntime,
                vmFactory = browserVmFactory,
            )
        }
        composable("tabs") { /* Placeholder */ }
        composable("bookmarks") { /* Placeholder */ }
        composable("settings") { /* Placeholder */ }
    }
}
