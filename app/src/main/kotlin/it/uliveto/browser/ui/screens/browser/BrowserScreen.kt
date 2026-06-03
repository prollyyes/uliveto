package it.uliveto.browser.ui.screens.browser

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import it.uliveto.browser.ui.components.AddressField
import it.uliveto.browser.ui.components.AddressFieldState
import it.uliveto.browser.ui.components.FindInPageBar
import it.uliveto.browser.ui.components.HourglassNav
import it.uliveto.browser.ui.components.OverflowDot
import it.uliveto.browser.ui.components.OverflowMenuSheet
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

/**
 * Full-screen browser screen.
 *
 * Shows a [GeckoView] filling the screen with a floating [HourglassNav] + [OverflowDot]
 * anchored to the bottom. The chrome hides on scroll-down and reappears on scroll-up.
 */
@Composable
fun BrowserScreen(
    runtime: GeckoRuntime,
    vmFactory: ViewModelProvider.Factory,
    initialUrl: String = "about:blank",
    onNavigateToBookmarks: () -> Unit = {},
    onNavigateToTabs: () -> Unit = {},
    onNewTab: () -> Unit = {},
) {
    @Suppress("UNUSED_VARIABLE")
    val viewModel: BrowserViewModel = viewModel(factory = vmFactory)

    val context = LocalContext.current
    val session = remember { GeckoSession() }

    // Navigation state tracked via GeckoSession.NavigationDelegate
    var currentUrl by remember { mutableStateOf(initialUrl) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Address field expansion state
    var addressExpanded by remember { mutableStateOf(false) }

    // Overflow sheet state
    var showOverflow by remember { mutableStateOf(false) }

    // Reader mode (stub false for M6)
    val isReaderAvailable by remember { mutableStateOf(false) }

    // Find-in-page state
    var showFindInPage by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }

    // Desktop site toggle
    var isDesktopSite by remember { mutableStateOf(false) }

    // Captured mutable state references for use inside the delegate lambda
    val setCurrentUrl: (String) -> Unit = { currentUrl = it }
    val setCanGoBack: (Boolean) -> Unit = { canGoBack = it }
    val setCanGoForward: (Boolean) -> Unit = { canGoForward = it }

    DisposableEffect(session, runtime) {
        session.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                setCurrentUrl(url ?: "about:blank")
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                setCanGoBack(canGoBack)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                setCanGoForward(canGoForward)
            }
        })
        session.open(runtime)
        session.loadUri(initialUrl)
        onDispose {
            session.setNavigationDelegate(null)
            session.close()
        }
    }

    // Scroll-to-hide chrome (HourglassNav + OverflowDot)
    val density = LocalDensity.current
    val chromeHeightPx = with(density) { 120.dp.toPx() }

    var chromeOffsetY by remember { mutableFloatStateOf(0f) }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                chromeOffsetY = (chromeOffsetY + delta).coerceIn(-chromeHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    // Animate the chrome translation — negative chromeOffsetY means chrome should slide down
    val animatedTranslationY by animateFloatAsState(
        targetValue = -chromeOffsetY,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "chrome_hide",
    )
    val chromeAlpha = 1f + chromeOffsetY / chromeHeightPx  // 1f when visible, 0f when hidden

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection),
    ) {
        // ── GeckoView — fills entire screen ───────────────────────────────────
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                GeckoView(ctx).apply {
                    setSession(session)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        // ── Find-in-page bar — shown above chrome when active ─────────────────
        if (showFindInPage) {
            FindInPageBar(
                query = findQuery,
                onQueryChange = { findQuery = it },
                onPrevious = { /* stub: GeckoView find prev */ },
                onNext = { /* stub: GeckoView find next */ },
                onClose = {
                    showFindInPage = false
                    findQuery = ""
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 76.dp),
            )
        }

        // ── Bottom chrome: HourglassNav + OverflowDot ─────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .graphicsLayer {
                    translationY = animatedTranslationY
                    alpha = chromeAlpha.coerceIn(0f, 1f)
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HourglassNav(
                currentUrl = currentUrl,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                onBack = { session.goBack() },
                onForward = { session.goForward() },
                onAddressTap = { addressExpanded = true },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(11.dp))
            OverflowDot(
                onClick = { showOverflow = true },
            )
        }

        // ── Expanded address overlay ───────────────────────────────────────────
        if (addressExpanded) {
            AddressField(
                state = AddressFieldState.Expanded,
                currentUrl = currentUrl,
                onSubmit = { url ->
                    session.loadUri(url)
                    addressExpanded = false
                },
                onDismiss = { addressExpanded = false },
            )
        }

        // ── Overflow menu sheet ────────────────────────────────────────────────
        if (showOverflow) {
            OverflowMenuSheet(
                isReaderAvailable = isReaderAvailable,
                onNewTab = {
                    showOverflow = false
                    onNewTab()
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
                onReader = {
                    showOverflow = false
                    // Stub: reader mode wired in future milestone
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
