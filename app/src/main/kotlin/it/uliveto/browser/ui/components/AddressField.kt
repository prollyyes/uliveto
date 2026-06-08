package it.uliveto.browser.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import it.uliveto.browser.domain.SearchEngine
import it.uliveto.browser.domain.UrlClassifier
import it.uliveto.browser.ui.tokens.CharcoalDark
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.PillShape
import it.uliveto.browser.ui.tokens.WarmCream

enum class AddressFieldState { Pill, HourglassCenter, Expanded, GestureFocused }

@Composable
fun AddressField(
    state: AddressFieldState,
    currentUrl: String = "",
    searchEngine: SearchEngine = SearchEngine.DuckDuckGo,
    customSearchEngineUrl: String = "",
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit = {},
    onPillTap: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            val fadeSpec = tween<Float>(durationMillis = 240, easing = FastOutSlowInEasing)
            val scaleSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 400f)
            (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = 0.96f))
                .togetherWith(fadeOut(fadeSpec) + scaleOut(scaleSpec, targetScale = 0.96f))
                .using(SizeTransform(clip = false))
        },
        label = "address_field_state",
        modifier = modifier,
    ) { targetState ->
        when (targetState) {
            AddressFieldState.Pill -> PillState(
                onTap = onPillTap,
                modifier = Modifier.fillMaxWidth(),
            )

            AddressFieldState.HourglassCenter -> HourglassCenterState(
                currentUrl = currentUrl,
                onTap = onPillTap,
                modifier = Modifier.fillMaxWidth(),
            )

            AddressFieldState.Expanded -> ExpandedState(
                currentUrl = currentUrl,
                searchEngine = searchEngine,
                customSearchEngineUrl = customSearchEngineUrl,
                onSubmit = onSubmit,
                onDismiss = onDismiss,
            )

            AddressFieldState.GestureFocused -> GestureFocusedState(
                searchEngine = searchEngine,
                customSearchEngineUrl = customSearchEngineUrl,
                onSubmit = onSubmit,
                onDismiss = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Pill state ────────────────────────────────────────────────────────────────

@Composable
private fun PillState(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(PillShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.30f),
                shape = PillShape,
            )
            .semantics { contentDescription = "Search or enter URL" }
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = WarmCream.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                text = "Search or enter URL",
                style = TextStyle(
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = WarmCream.copy(alpha = 0.6f),
                ),
            )
        }
    }
}

// ── GestureFocused state ──────────────────────────────────────────────────

@Composable
private fun GestureFocusedState(
    searchEngine: SearchEngine,
    customSearchEngineUrl: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = true) { onDismiss() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = HankenGrotesk,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = WarmCream,
        ),
        cursorBrush = SolidColor(WarmCream),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                if (text.isNotBlank()) {
                    val url = UrlClassifier.buildNavigationUrl(text, searchEngine, customSearchEngineUrl)
                    onSubmit(url)
                } else {
                    onDismiss()
                }
            },
        ),
        modifier = modifier
            .height(52.dp)
            .clip(PillShape)
            .background(Color.White.copy(alpha = 0.22f))
            .border(1.dp, Color.White.copy(alpha = 0.55f), PillShape)
            .focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = WarmCream.copy(alpha = 0.80f),
                    modifier = Modifier.padding(end = 8.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Search or enter URL",
                            style = TextStyle(
                                fontFamily = HankenGrotesk,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = WarmCream.copy(alpha = 0.50f),
                            ),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

// ── HourglassCenter state ─────────────────────────────────────────────────────

@Composable
private fun HourglassCenterState(
    currentUrl: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val domain = remember(currentUrl) { extractDomain(currentUrl) }
    Row(
        modifier = modifier.clickable(onClick = onTap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = null,
            tint = CharcoalDark.copy(alpha = 0.6f),
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = domain,
            color = CharcoalDark,
            fontFamily = HankenGrotesk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Expanded state ─────────────────────────────────────────────────────────────

@Composable
private fun ExpandedState(
    currentUrl: String,
    searchEngine: SearchEngine,
    customSearchEngineUrl: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = true) { onDismiss() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var scrimVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { scrimVisible = true }
    val scrimAlpha by animateFloatAsState(
        targetValue = if (scrimVisible) 0.45f else 0f,
        animationSpec = tween(280),
        label = "scrimAlpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim — tapping outside the text field dismisses
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss,
                ),
        )

        // Text field floats above the keyboard at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = WarmCream,
                ),
                cursorBrush = SolidColor(WarmCream),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (text.isNotBlank()) {
                            val url = UrlClassifier.buildNavigationUrl(text, searchEngine, customSearchEngineUrl)
                            onSubmit(url)
                        } else {
                            onDismiss()
                        }
                    },
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.20f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(50),
                    )
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = WarmCream.copy(alpha = 0.8f),
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Search or enter URL",
                                    style = TextStyle(
                                        fontFamily = HankenGrotesk,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 15.sp,
                                        color = WarmCream.copy(alpha = 0.5f),
                                    ),
                                )
                            }
                            innerTextField()
                        }
                    }
                },
            )
        }
    }
}

private fun extractDomain(url: String): String {
    return try {
        android.net.Uri.parse(url).host?.removePrefix("www.") ?: url
    } catch (_: Exception) {
        url
    }
}
