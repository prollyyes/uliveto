package it.uliveto.browser.ui.screens.browser

import android.content.Intent
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.uliveto.browser.domain.SearchEngine
import it.uliveto.browser.tabs.TabManager
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.components.AddressField
import it.uliveto.browser.ui.components.AddressFieldState
import it.uliveto.browser.ui.components.FindInPageBar
import it.uliveto.browser.ui.components.OverflowMenuSheet
import it.uliveto.browser.ui.components.SimpleNavBar
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.WarmCream
import kotlinx.coroutines.delay
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
    val viewModel: BrowserViewModel = viewModel(factory = vmFactory)
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current

    // Capture theme accent for the SwipeRefreshLayout spinner
    val ulivetoColors = LocalUlivetoColors.current
    val swipeAccentArgb = remember(ulivetoColors) {
        ulivetoColors.gradientColors.firstOrNull()?.toArgb() ?: Color(0xFFB25737).toArgb()
    }

    SideEffect {
        WindowInsetsControllerCompat(
            (view.context as android.app.Activity).window, view,
        ).isAppearanceLightStatusBars = false
    }

    val session = remember(tabId) {
        TabManager.getSession(tabId) ?: GeckoSession().also { s ->
            s.open(runtime)
            s.loadUri("about:blank")
        }
    }

    // Navigation state
    var currentUrl by remember { mutableStateOf(TabManager.getTab(tabId)?.url ?: "about:blank") }
    var pageTitle by remember { mutableStateOf(TabManager.getTab(tabId)?.title ?: "") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // UI state
    var addressExpanded by remember { mutableStateOf(false) }
    var showOverflow by remember { mutableStateOf(false) }
    val overflowSheetState = rememberModalBottomSheetState()
    var showFindInPage by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var isDesktopSite by remember { mutableStateOf(false) }
    var bookmarkJustSaved by remember { mutableStateOf(false) }

    // Pull-to-refresh state
    var isPullRefreshing by remember { mutableStateOf(false) }

    // Stable int array shared between scroll delegate and SwipeRefreshLayout child callback.
    // [0] = current scrollY for "can scroll up?" check
    // [1] = previous scrollY for chrome-hide delta computation
    // Using intArrayOf rather than mutableIntStateOf avoids triggering recomposition on scroll.
    val scrollYRef = remember { intArrayOf(0, 0) }

    // Chrome animation — driven by GeckoSession.ScrollDelegate
    val chromeHeightPx = with(density) { 120.dp.toPx() }
    var chromeOffsetY by remember { mutableFloatStateOf(0f) }

    val animatedTranslationY by animateFloatAsState(
        targetValue = -chromeOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chrome_hide",
    )
    val animatedChromeAlpha = (1f - animatedTranslationY / chromeHeightPx).coerceIn(0f, 1f)

    // Dismiss pull-refresh spinner once the page finishes loading
    LaunchedEffect(isLoading) {
        if (!isLoading && isPullRefreshing) {
            isPullRefreshing = false
        }
    }

    // Bookmark toast auto-dismiss
    LaunchedEffect(bookmarkJustSaved) {
        if (bookmarkJustSaved) {
            delay(2000)
            bookmarkJustSaved = false
        }
    }

    // Stable setters avoid stale closure captures inside DisposableEffect
    val setCurrentUrl: (String) -> Unit = { currentUrl = it }
    val setPageTitle: (String) -> Unit = { pageTitle = it }
    val setCanGoBack: (Boolean) -> Unit = { canGoBack = it }
    val setCanGoForward: (Boolean) -> Unit = { canGoForward = it }
    val setIsLoading: (Boolean) -> Unit = { isLoading = it }

    DisposableEffect(session) {
        session.setProgressDelegate(object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) = setIsLoading(true)
            override fun onPageStop(session: GeckoSession, success: Boolean) = setIsLoading(false)
        })

        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                val resolved = url ?: "about:blank"
                // GeckoSession fires onLocationChange("about:blank") during session
                // startup/initialisation before committing the real URL — suppress it
                // so the URL bar never flashes blank during a navigation.
                if (resolved == "about:blank") return
                setCurrentUrl(resolved)
                TabManager.updateTab(tabId, url = resolved)
            }
            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) = setCanGoBack(canGoBack)
            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) = setCanGoForward(canGoForward)
        })

        session.setScrollDelegate(object : GeckoSession.ScrollDelegate {
            override fun onScrollChanged(session: GeckoSession, scrollX: Int, scrollY: Int) {
                chromeOffsetY = when {
                    scrollY == 0 -> 0f
                    else -> {
                        val delta = scrollY - scrollYRef[1]
                        (chromeOffsetY - delta.toFloat()).coerceIn(-chromeHeightPx, 0f)
                    }
                }
                scrollYRef[0] = scrollY  // for SwipeRefreshLayout child check
                scrollYRef[1] = scrollY  // for next delta computation
            }
        })

        session.setContentDelegate(object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) {
                    setPageTitle(title)
                    TabManager.updateTab(tabId, title = title)
                }
            }
        })

        onDispose {
            session.setProgressDelegate(null)
            session.setNavigationDelegate(null)
            session.setScrollDelegate(null)
            session.setContentDelegate(null)
        }
    }

    BackHandler(enabled = canGoBack) { session.goBack() }

    Box(modifier = Modifier.fillMaxSize()) {
        // SwipeRefreshLayout wraps GeckoView so pull-to-refresh can intercept touches at
        // the View level — GeckoView eats all MotionEvents so Compose NestedScroll never
        // fires. SwipeRefreshLayout.onInterceptTouchEvent() runs before GeckoView and
        // correctly captures the overscroll gesture when the page is at the top.
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                val geckoView = GeckoView(ctx).apply {
                    setSession(session)
                    // Load immediately after setSession so there is no blank-frame delay
                    val url = TabManager.consumeInitialUrl(tabId)
                    if (url != null) session.loadUri(url)
                }
                // Subclass so canChildScrollUp() uses the tracked GeckoView scroll position.
                // SwipeRefreshLayout calls this before intercepting touches — if it returns
                // true the layout assumes the child can still scroll up and passes the event
                // through instead of triggering the refresh indicator.
                object : SwipeRefreshLayout(ctx) {
                    override fun canChildScrollUp() = scrollYRef[0] > 0
                }.apply {
                    addView(
                        geckoView,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ),
                    )
                    // Mediterranean theme: WarmCream background, accent spinner
                    setProgressBackgroundColorSchemeColor(WarmCream.toArgb())
                    setColorSchemeColors(swipeAccentArgb)
                    setOnRefreshListener {
                        isPullRefreshing = true
                        // Reset scroll tracking so chrome re-appears immediately after refresh
                        scrollYRef[0] = 0
                        scrollYRef[1] = 0
                        chromeOffsetY = 0f
                        session.reload()
                    }
                }
            },
            update = { view ->
                // Sync the spinner with our refresh state on every recomposition
                (view as SwipeRefreshLayout).isRefreshing = isPullRefreshing
            },
            modifier = Modifier.fillMaxSize(),
        )

        // Narrow status-bar scrim — keeps system icons readable over any web content
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

        // Thin progress bar — only for navigation-driven loads (URL bar, new tab, links).
        // Hidden during pull-to-refresh because SwipeRefreshLayout's spinner already signals it.
        if (isLoading && !isPullRefreshing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 34.dp),
                color = WarmCream.copy(alpha = 0.85f),
                trackColor = Color.Transparent,
            )
        }

        // Find-in-page bar
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

        // Bottom chrome — slides away on scroll-down, hides during find-in-page / address mode
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

        // Bookmark-saved toast
        AnimatedVisibility(
            visible = bookmarkJustSaved,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = WarmCream,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = CharcoalDark,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Bookmark saved",
                        fontFamily = HankenGrotesk,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = CharcoalDark,
                    )
                }
            }
        }

        if (showOverflow) {
            OverflowMenuSheet(
                sheetState = overflowSheetState,
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
                onBookmarks = {
                    showOverflow = false
                    onNavigateToBookmarks()
                },
                onSaveBookmark = {
                    showOverflow = false
                    viewModel.saveBookmark(
                        url = currentUrl,
                        title = pageTitle.ifBlank { currentUrl },
                    )
                    bookmarkJustSaved = true
                },
                onShare = {
                    showOverflow = false
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, currentUrl)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                },
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
