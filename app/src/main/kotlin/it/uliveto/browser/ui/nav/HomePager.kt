package it.uliveto.browser.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import it.uliveto.browser.tabs.TabManager
import it.uliveto.browser.ui.screens.bookmarks.BookmarksScreen
import it.uliveto.browser.ui.screens.bookmarks.BookmarksViewModel
import it.uliveto.browser.ui.screens.start.StartScreen
import it.uliveto.browser.ui.screens.start.StartViewModel
import it.uliveto.browser.ui.screens.tabs.TabsScreen
import kotlinx.coroutines.launch

// Pages: 0 = Tabs, 1 = Home (default), 2 = Bookmarks
@Composable
fun HomePager(
    startViewModel: StartViewModel,
    bookmarksViewModel: BookmarksViewModel,
    onNavigateToBrowser: (String) -> Unit,
    onSelectTab: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val snapFlingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        snapAnimationSpec = spring(dampingRatio = 0.78f, stiffness = 350f),
    )

    // Hardware back on a side page scrolls home instead of exiting the app
    BackHandler(enabled = pagerState.currentPage != 1) {
        scope.launch { pagerState.animateScrollToPage(1) }
    }

    HorizontalPager(
        state = pagerState,
        flingBehavior = snapFlingBehavior,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = pageOffset * 40.dp.toPx() },
        ) {
            when (page) {
                0 -> TabsScreen(
                    onSelectTab = onSelectTab,
                    onCloseTab = { tabId ->
                        TabManager.closeTab(tabId)
                        if (TabManager.tabs.isEmpty()) scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    onNewTab = { scope.launch { pagerState.animateScrollToPage(1) } },
                    onBack = { scope.launch { pagerState.animateScrollToPage(1) } },
                )
                1 -> StartScreen(
                    viewModel = startViewModel,
                    onNavigateToBrowser = onNavigateToBrowser,
                    onNavigateToTabs = { scope.launch { pagerState.animateScrollToPage(0) } },
                    onNavigateToBookmarks = { scope.launch { pagerState.animateScrollToPage(2) } },
                    onNavigateToSettings = onNavigateToSettings,
                )
                2 -> BookmarksScreen(
                    viewModel = bookmarksViewModel,
                    onOpenUrl = onNavigateToBrowser,
                    onBack = { scope.launch { pagerState.animateScrollToPage(1) } },
                )
            }
        }
    }
}
