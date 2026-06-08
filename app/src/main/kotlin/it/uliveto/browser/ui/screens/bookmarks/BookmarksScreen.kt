package it.uliveto.browser.ui.screens.bookmarks

import android.net.Uri
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
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
import it.uliveto.browser.data.bookmarks.BookmarkEntity
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.WarmCream
import kotlinx.coroutines.delay

private val BookmarkCardShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BookmarksViewModel,
    onOpenUrl: (String) -> Unit,
    onBack: () -> Unit,
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val ulivetoColors = LocalUlivetoColors.current
    val gradient = Brush.radialGradient(ulivetoColors.gradientColors)

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
                    text = "Bookmarks",
                    fontFamily = InstrumentSerif,
                    fontSize = 24.sp,
                    color = WarmCream,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                )
                if (bookmarks.isNotEmpty()) {
                    Text(
                        text = "${bookmarks.size} saved",
                        fontFamily = HankenGrotesk,
                        fontSize = 13.sp,
                        color = WarmCream.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 16.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☆",
                            fontSize = 56.sp,
                            color = WarmCream.copy(alpha = 0.35f),
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No bookmarks yet",
                            fontFamily = HankenGrotesk,
                            fontSize = 17.sp,
                            color = WarmCream.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Save pages from the browser menu",
                            fontFamily = HankenGrotesk,
                            fontSize = 13.sp,
                            color = WarmCream.copy(alpha = 0.4f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(bookmarks, key = { _, b -> b.id }) { index, bookmark ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(bookmark.id) {
                            delay(index.coerceAtMost(8) * 25L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(200)) +
                                    slideInVertically(tween(200, easing = FastOutSlowInEasing)) { it / 4 },
                        ) {
                            BookmarkCard(
                                bookmark = bookmark,
                                onTap = { onOpenUrl(bookmark.url) },
                                onDelete = { viewModel.delete(bookmark) },
                            )
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkCard(
    bookmark: BookmarkEntity,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) onDelete()
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
                    .background(bgColor, BookmarkCardShape),
            )
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        val title = bookmark.title.ifBlank { bookmark.url }
        val initial = try {
            Uri.parse(bookmark.url).host?.removePrefix("www.")
                ?.firstOrNull()?.uppercaseChar()?.toString() ?: "★"
        } catch (_: Exception) {
            "★"
        }

        Surface(
            onClick = onTap,
            shape = BookmarkCardShape,
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
                        text = initial,
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
                    Text(
                        text = bookmark.url,
                        fontFamily = HankenGrotesk,
                        fontSize = 12.sp,
                        color = WarmCream.copy(alpha = 0.65f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
