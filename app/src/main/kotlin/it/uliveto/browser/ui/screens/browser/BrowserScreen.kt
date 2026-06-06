package it.uliveto.browser.ui.screens.browser

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import it.uliveto.browser.domain.SearchEngine
import it.uliveto.browser.tabs.TabManager
import it.uliveto.browser.ui.components.AddressField
import it.uliveto.browser.ui.components.AddressFieldState
import it.uliveto.browser.ui.components.FindInPageBar
import it.uliveto.browser.ui.components.OverflowMenuSheet
import it.uliveto.browser.ui.components.SimpleNavBar
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    runtime: GeckoRuntime,
    vmFactory: ViewModelProvider.Factory,
    tabId: String,
    searchEngine: SearchEngine = SearchEngine.DuckDuckGo,
    customSearchEngineUrl: String = "",
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToTabs: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNewTab: () -> Unit = {},
) {
    @Suppress("UNUSED_VARIABLE")
    val viewModel: BrowserViewModel = viewModel(factory = vmFactory)

    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current

    SideEffect {
        WindowInsetsControllerCompat(
            (view.context as android.app.Activity).window, view,
        ).isAppearanceLightStatusBars = false
    }

    // Reuse the existing GeckoSession from TabManager — avoids re-creating the engine
    val session = remember(tabId) {
        TabManager.getSession(tabId) ?: GeckoSession().also { s ->
            s.open(runtime)
            s.loadUri("about:blank")
        }
    }

    // Navigation state
    var currentUrl by remember { mutableStateOf(TabManager.getTab(tabId)?.url ?: "about:blank") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // UI state
    var addressExpanded by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    val overflowSheetState = rememberModalBottomSheetState()
    val isReaderAvailable by remember { mutableStateOf(true) }
    var showFindInPage by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var isDesktopSite by remember { mutableStateOf(false) }

    // Chrome scroll — driven by GeckoSession.ScrollDelegate (NestedScrollConnection
    // never fires for GeckoView because GeckoView intercepts all touch events)
    val chromeHeightPx = with(density) { 120.dp.toPx() }
    var chromeOffsetY by remember { mutableFloatStateOf(0f) }
    var lastScrollY by remember { mutableIntStateOf(0) }

    val animatedTranslationY by animateFloatAsState(
        targetValue = -chromeOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chrome_hide",
    )
    val animatedChromeAlpha = (1f - animatedTranslationY / chromeHeightPx).coerceIn(0f, 1f)

    // Captured setters avoid stale closure captures inside DisposableEffect
    val setCurrentUrl: (String) -> Unit = { currentUrl = it }
    val setCanGoBack: (Boolean) -> Unit = { canGoBack = it }
    val setCanGoForward: (Boolean) -> Unit = { canGoForward = it }

    DisposableEffect(session) {
        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                val resolved = url ?: "about:blank"
                setCurrentUrl(resolved)
                TabManager.updateTab(tabId, url = resolved)
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                setCanGoBack(canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                setCanGoForward(canGoForward)
            }
        })

        // ScrollDelegate fires on the UI thread with absolute scroll position
        session.setScrollDelegate(object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
                chromeOffsetY = when {
                    scrollY == 0 -> 0f  // always reveal chrome at the top of the page
                    else -> {
                        val delta = scrollY - lastScrollY
                        (chromeOffsetY - delta.toFloat()).coerceIn(-chromeHeightPx, 0f)
                    }
                }
                lastScrollY = scrollY
            }
        })

        session.setContentDelegate(object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) TabManager.updateTab(tabId, title = title)
            }
        })

        onDispose {
            // Clear delegates but keep session alive — TabManager owns the lifecycle
            session.setNavigationDelegate(null)
            session.setScrollDelegate(null)
            session.setContentDelegate(null)
        }
    }

    BackHandler(enabled = canGoBack) { session.goBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        // GeckoView fills the whole screen. A static statusBarsPadding() wrapper keeps
        // web content below the status bar / notch without causing a relayout on every
        // scroll event (animated padding was the root cause of scroll choppiness).
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx -> GeckoView(ctx).apply { setSession(session) } },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Narrow status-bar scrim — only covers the status bar area for icon legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.40f), Color.Transparent),
                    )
                ),
        )

        // Find-in-page bar — imePadding lifts it above the keyboard automatically
        if (showFindInPage) {
            FindInPageBar(
                query = findQuery,
                onQueryChange = { findQuery = it },
                onPrevious = { },
                onNext = { },
                onClose = {
                    showFindInPage = false
                    findQuery = ""
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        // Bottom chrome — hidden while find-in-page is active, slides away on scroll-down
        if (!addressExpanded && !showFindInPage) {
            SimpleNavBar(
                currentUrl = currentUrl,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                onBack = { session.goBack() },
                onForward = { session.goForward() },
                onAddressTap = { addressExpanded = true },
                onMenuTap = { showOverflow = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .graphicsLayer {
                        translationY = animatedTranslationY
                        alpha = animatedChromeAlpha
                    },
            )
        }

        if (addressExpanded) {
            AddressField(
                state = AddressFieldState.Expanded,
                currentUrl = currentUrl,
                searchEngine = searchEngine,
                customSearchEngineUrl = customSearchEngineUrl,
                onSubmit = { url ->
                    session.loadUri(url)
                    addressExpanded = false
                },
                onDismiss = { addressExpanded = false },
            )
        }

        if (showOverflow) {
            OverflowMenuSheet(
                sheetState = overflowSheetState,
                isReaderAvailable = isReaderAvailable,
                isDesktopSite = isDesktopSite,
                onNewTab = {
                    showOverflow = false
                    onNewTab()
                },
                onTabs = {
                    showOverflow = false
                    onNavigateToTabs()
                },
                onHome = {
                    showOverflow = false
                    onNavigateToHome()
                },
                onSettings = {
                    showOverflow = false
                    onNavigateToSettings()
                },
                onBookmarks = {
                    showOverflow = false
                    onNavigateToBookmarks()
                },
                onShare = {
                    showOverflow = false
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                },
                onReader = { showOverflow = false },
                onFind = {
                    showFindInPage = true
                    showOverflow = false
                },
                onDesktopSite = {
                    showOverflow = false
                    isDesktopSite = !isDesktopSite
                    session.settings.setUserAgentMode(
                        if (isDesktopSite) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                        else GeckoSessionSettings.USER_AGENT_MODE_MOBILE,
                    )
                    session.reload()
                },
                onDismiss = { showOverflow = false },
            )
        }
    }
}
