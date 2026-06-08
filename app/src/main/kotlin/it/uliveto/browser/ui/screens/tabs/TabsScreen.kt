package it.uliveto.browser.ui.screens.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.tabs.BrowserTab
import it.uliveto.browser.tabs.TabManager
import kotlinx.coroutines.delay
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.WarmCream

private val TabCardShape = RoundedCornerShape(20.dp)

private fun tabDisplayTitle(tab: BrowserTab): String = when {
    tab.title.isNotBlank() -> tab.title
    tab.url.isBlank() || tab.url == "about:blank" -> "Homepage"
    else -> try {
        android.net.Uri.parse(tab.url).host?.removePrefix("www.") ?: tab.url
    } catch (_: Exception) {
        tab.url
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(
    onSelectTab: (tabId: String) -> Unit,
    onCloseTab: (tabId: String) -> Unit,
    onNewTab: () -> Unit,
    onBack: () -> Unit,
) {
    val ulivetoColors = LocalUlivetoColors.current
    val gradient = Brush.radialGradient(ulivetoColors.gradientColors)
    val tabs = TabManager.tabs
    val tabCount = tabs.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = WarmCream,
                    )
                }
                Text(
                    text = "Tabs",
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = WarmCream,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                )
                Text(
                    text = "$tabCount open",
                    fontFamily = HankenGrotesk,
                    fontSize = 13.sp,
                    color = WarmCream.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 4.dp),
                )
                IconButton(onClick = onNewTab) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New tab",
                        tint = WarmCream,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (tabs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No open tabs",
                        fontFamily = HankenGrotesk,
                        fontSize = 16.sp,
                        color = WarmCream.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        val index = tabs.indexOf(tab)

                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index.coerceAtMost(5) * 30L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(220)) +
                                    slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 4 },
                        ) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value != SwipeToDismissBoxValue.Settled) {
                                        onCloseTab(tab.id)
                                    }
                                    true
                                },
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val bgColor by animateColorAsState(
                                        targetValue = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.Settled -> Color.Transparent
                                            else -> Color(0xFFB02020).copy(alpha = 0.85f)
                                        },
                                        label = "swipe_bg",
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(bgColor, TabCardShape),
                                    )
                                },
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                            ) {
                                TabCard(
                                    tab = tab,
                                    isActive = tab.id == TabManager.activeTabId,
                                    onClick = { onSelectTab(tab.id) },
                                    onClose = { onCloseTab(tab.id) },
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: BrowserTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    val title = tabDisplayTitle(tab)
    Surface(
        onClick = onClick,
        shape = TabCardShape,
        color = if (isActive)
            Color.White.copy(alpha = 0.25f)
        else
            Color.White.copy(alpha = 0.15f),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WarmCream.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (title == "Homepage") "⌂" else title.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontFamily = HankenGrotesk,
                    fontSize = 16.sp,
                    color = WarmCream,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = title,
                    fontFamily = HankenGrotesk,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 15.sp,
                    color = WarmCream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (tab.url != "about:blank" && tab.url.isNotBlank()) {
                    Text(
                        text = tab.url,
                        fontFamily = HankenGrotesk,
                        fontSize = 12.sp,
                        color = WarmCream.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close tab",
                    tint = WarmCream.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
