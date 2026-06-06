package it.uliveto.browser.ui.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.uliveto.browser.data.prefs.UserPreferences
import it.uliveto.browser.di.AppContainer
import it.uliveto.browser.di.ViewModelFactory
import it.uliveto.browser.tabs.TabManager
import it.uliveto.browser.ui.UlivetoTheme
import it.uliveto.browser.ui.screens.bookmarks.BookmarksScreen
import it.uliveto.browser.ui.screens.bookmarks.BookmarksViewModel
import it.uliveto.browser.ui.screens.browser.BrowserScreen
import it.uliveto.browser.ui.screens.browser.BrowserViewModel
import it.uliveto.browser.ui.screens.settings.PrivacyReceiptsScreen
import it.uliveto.browser.ui.screens.settings.SettingsScreen
import it.uliveto.browser.ui.screens.settings.SettingsViewModel
import it.uliveto.browser.ui.screens.start.StartViewModel
import it.uliveto.browser.ui.screens.tabs.TabsScreen

@Composable
fun UlivetoNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val prefs by appContainer.userPrefsRepository.preferences.collectAsState(
        initial = UserPreferences(),
    )

    UlivetoTheme(appTheme = prefs.theme) {
        NavHost(
            navController = navController,
            startDestination = "start",
            modifier = modifier,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) },
        ) {
            composable("start") {
                val startVm: StartViewModel = viewModel(
                    factory = ViewModelFactory { StartViewModel(appContainer.userPrefsRepository) },
                )
                val bookmarksVm: BookmarksViewModel = viewModel(
                    factory = ViewModelFactory { BookmarksViewModel(appContainer.bookmarksRepository) },
                )
                HomePager(
                    startViewModel = startVm,
                    bookmarksViewModel = bookmarksVm,
                    onNavigateToBrowser = { url ->
                        val tab = TabManager.createTab(url, appContainer.geckoRuntime)
                        navController.navigate("browser/${tab.id}")
                    },
                    onSelectTab = { tabId ->
                        TabManager.setActiveTab(tabId)
                        navController.navigate("browser/$tabId") {
                            popUpTo("start") { saveState = true }
                        }
                    },
                    onNavigateToSettings = { navController.navigate("settings") },
                )
            }

            composable(
                route = "browser/{tabId}",
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(280))
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280))
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280))
                },
            ) { backStackEntry ->
                val tabId = backStackEntry.arguments?.getString("tabId") ?: return@composable
                val browserVmFactory = ViewModelFactory {
                    BrowserViewModel(appContainer.bookmarksRepository)
                }
                BrowserScreen(
                    runtime = appContainer.geckoRuntime,
                    vmFactory = browserVmFactory,
                    tabId = tabId,
                    searchEngine = prefs.searchEngine,
                    customSearchEngineUrl = prefs.customSearchEngineUrl,
                    onNavigateToBookmarks = { navController.navigate("bookmarks") },
                    onNavigateToTabs = { navController.navigate("tabs") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToHome = { navController.navigate("start") },
                    onNewTab = { navController.navigate("start") },
                )
            }

            composable("tabs") {
                TabsScreen(
                    onSelectTab = { tabId ->
                        TabManager.setActiveTab(tabId)
                        navController.navigate("browser/$tabId") {
                            popUpTo("tabs") { inclusive = true }
                        }
                    },
                    onCloseTab = { tabId ->
                        TabManager.closeTab(tabId)
                        if (TabManager.tabs.isEmpty()) {
                            navController.navigate("start") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
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
                        val tab = TabManager.createTab(url, appContainer.geckoRuntime)
                        navController.navigate("browser/${tab.id}")
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
