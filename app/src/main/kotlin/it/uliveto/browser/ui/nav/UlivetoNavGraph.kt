package it.uliveto.browser.ui.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uliveto.browser.di.AppContainer
import it.uliveto.browser.di.ViewModelFactory
import it.uliveto.browser.ui.UlivetoTheme
import it.uliveto.browser.ui.screens.bookmarks.BookmarksScreen
import it.uliveto.browser.ui.screens.bookmarks.BookmarksViewModel
import it.uliveto.browser.ui.screens.browser.BrowserScreen
import it.uliveto.browser.ui.screens.browser.BrowserViewModel
import it.uliveto.browser.ui.screens.settings.PrivacyReceiptsScreen
import it.uliveto.browser.ui.screens.settings.SettingsScreen
import it.uliveto.browser.ui.screens.settings.SettingsViewModel
import it.uliveto.browser.ui.screens.start.StartScreen
import it.uliveto.browser.ui.screens.start.StartViewModel
import it.uliveto.browser.ui.screens.tabs.TabsScreen

@Composable
fun UlivetoNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // Collect preferences at nav-graph level so theme + navStyle stay in sync app-wide
    val prefs by appContainer.userPrefsRepository.preferences.collectAsState(
        initial = it.uliveto.browser.data.prefs.UserPreferences(),
    )

    UlivetoTheme(appTheme = prefs.theme) {
        NavHost(
            navController = navController,
            startDestination = "start",
            modifier = modifier,
        ) {
            composable("start") {
                val startVmFactory = ViewModelFactory {
                    StartViewModel(appContainer.userPrefsRepository)
                }
                val startVm: StartViewModel = viewModel(factory = startVmFactory)
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
                    initialUrl = Uri.decode(url),
                    navStyle = prefs.navStyle,
                    onNavigateToBookmarks = { navController.navigate("bookmarks") },
                    onNavigateToTabs = { navController.navigate("tabs") },
                    onNewTab = { navController.navigate("start") },
                )
            }
            composable("tabs") {
                val currentUrl = try {
                    val rawUrl = navController.getBackStackEntry("browser?url={url}")
                        .arguments?.getString("url") ?: ""
                    if (rawUrl.isNotBlank()) Uri.decode(rawUrl) else "about:blank"
                } catch (_: IllegalArgumentException) {
                    "about:blank"
                }
                TabsScreen(
                    currentUrl = currentUrl,
                    onSelectTab = { navController.popBackStack() },
                    onNewTab = { navController.navigate("start") },
                    onBack = { navController.popBackStack() },
                )
            }
            composable("bookmarks") {
                val bookmarksVmFactory = ViewModelFactory {
                    BookmarksViewModel(appContainer.bookmarksRepository)
                }
                BookmarksScreen(
                    viewModel = viewModel(factory = bookmarksVmFactory),
                    onOpenUrl = { url ->
                        navController.navigate("browser?url=${Uri.encode(url)}")
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable("settings") {
                val settingsVmFactory = ViewModelFactory {
                    SettingsViewModel(appContainer.userPrefsRepository)
                }
                SettingsScreen(
                    viewModel = viewModel(factory = settingsVmFactory),
                    onBack = { navController.popBackStack() },
                    onNavigateToPrivacyReceipts = { navController.navigate("privacy-receipts") },
                )
            }
            composable("privacy-receipts") {
                PrivacyReceiptsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
