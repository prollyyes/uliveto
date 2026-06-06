package it.uliveto.browser.ui.screens.tabs

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.WarmCream

private val TabCardShape = RoundedCornerShape(20.dp)

private fun urlDisplayTitle(url: String): String = when {
    url.isBlank() || url == "about:blank" -> "Homepage"
    else -> try {
        android.net.Uri.parse(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabsScreen(
    currentUrl: String,
    onSelectTab: () -> Unit,
    onNewTab: () -> Unit,
    onBack: () -> Unit,
) {
    val ulivetoColors = LocalUlivetoColors.current
    val gradient = Brush.radialGradient(ulivetoColors.gradientColors)
    val title = urlDisplayTitle(currentUrl)

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
                    text = "1 open",
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value != SwipeToDismissBoxValue.Settled) {
                                onNewTab()
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
                                    .background(bgColor, TabCardShape)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Close tab",
                                    tint = Color.White,
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                    ) {
                        TabCard(
                            title = title,
                            url = currentUrl,
                            onClick = onSelectTab,
                            onClose = onNewTab,
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TabCard(
    title: String,
    url: String,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = TabCardShape,
        color = Color.White.copy(alpha = 0.15f),
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
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = WarmCream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (url != "about:blank" && url.isNotBlank()) {
                    Text(
                        text = url,
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
