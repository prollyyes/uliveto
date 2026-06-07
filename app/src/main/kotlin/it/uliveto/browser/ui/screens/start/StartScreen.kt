package it.uliveto.browser.ui.screens.start

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.uliveto.browser.ui.LocalUlivetoColors
import it.uliveto.browser.ui.components.AddressField
import it.uliveto.browser.ui.components.AddressFieldState
import it.uliveto.browser.ui.components.EngineLine
import it.uliveto.browser.ui.tokens.HankenGrotesk
import it.uliveto.browser.ui.tokens.InstrumentSerif
import it.uliveto.browser.ui.tokens.PillShape
import it.uliveto.browser.ui.tokens.WarmCream
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.launch

private sealed interface GestureState {
    data object Rest : GestureState
    data object Dragging : GestureState
    data object Committed : GestureState
}

internal fun shouldCommitGesture(
    offsetPx: Float,
    velocityPx: Float,
    thresholdPx: Float,
    velocityThresholdPxPerSec: Float = 900f,
): Boolean = offsetPx >= thresholdPx || velocityPx >= velocityThresholdPxPerSec

private val mediterraneanGreetings = listOf(
    "Benvenuto",
    "Buongiorno",
    "Kalimera",
    "Bienvenido",
    "Bienvenue",
    "Merhaba",
    "Bon dia",
    "Salve",
    "Olá",
)

@Composable
fun StartScreen(
    viewModel: StartViewModel,
    onNavigateToBrowser: (String) -> Unit,
    onNavigateToTabs: () -> Unit,
    onNavigateToBookmarks: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs by viewModel.preferences.collectAsState()
    // Use companion object flag so the dialog never re-appears on new-tab navigation
    var hasShownNamePrompt by remember { mutableStateOf(StartViewModel.namePromptShown) }
    var showNameDialog by remember { mutableStateOf(false) }
    var addressExpanded by remember { mutableStateOf(false) }
    var gestureState: GestureState by remember { mutableStateOf(GestureState.Rest) }
    val dragOffsetY = remember { Animatable(0f) }
    var pillNaturalY by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val view = LocalView.current

    val thresholdPx = with(density) { 120.dp.toPx() }
    val windowHeight = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val targetCommitOffsetPx by remember(pillNaturalY, windowHeight) {
        derivedStateOf { windowHeight * 0.45f - pillNaturalY }
    }
    val blurAlpha by animateFloatAsState(
        targetValue = when (gestureState) {
            GestureState.Rest -> 0f
            GestureState.Dragging -> (dragOffsetY.value / thresholdPx).coerceIn(0f, 0.6f)
            GestureState.Committed -> 1f
        },
        animationSpec = when (gestureState) {
            GestureState.Rest -> tween(220, easing = FastOutSlowInEasing)
            GestureState.Committed -> tween(300, easing = FastOutSlowInEasing)
            else -> snap()
        },
        label = "blurAlpha",
    )

    BackHandler(enabled = addressExpanded) { addressExpanded = false }

    LaunchedEffect(prefs.userName, hasShownNamePrompt) {
        if (prefs.userName.isBlank() && !hasShownNamePrompt) {
            showNameDialog = true
        }
    }

    if (showNameDialog) {
        NamePromptDialog(
            onConfirm = { name ->
                if (name.isNotBlank()) viewModel.setUserName(name)
                hasShownNamePrompt = true
                StartViewModel.namePromptShown = true
                showNameDialog = false
            },
            onDismiss = {
                hasShownNamePrompt = true
                StartViewModel.namePromptShown = true
                showNameDialog = false
            },
        )
    }

    val ulivetoColors = LocalUlivetoColors.current
    val themeGradient = remember(ulivetoColors) {
        Brush.radialGradient(ulivetoColors.gradientColors)
    }

    // New random greeting every time the Homepage is visited
    val greetingWord = remember { mediterraneanGreetings.random() }
    val greeting = if (prefs.userName.isBlank()) greetingWord else "$greetingWord, ${prefs.userName}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = themeGradient)
            // 4%-opacity grain overlay pre-rendered into an ImageBitmap — no PNG texture
            .drawWithCache {
                val w = size.width.toInt().coerceAtLeast(1)
                val h = size.height.toInt().coerceAtLeast(1)
                val grainBitmap = ImageBitmap(w, h)
                val rng = Random(seed = 42)
                val paint = Paint().apply {
                    color = Color.White.copy(alpha = 0.04f)
                }
                val count = 4000
                androidx.compose.ui.graphics.Canvas(grainBitmap).apply {
                    for (i in 0 until count) {
                        val x = rng.nextFloat() * w
                        val y = rng.nextFloat() * h
                        val r = rng.nextFloat() * 1.5f + 0.5f
                        drawCircle(Offset(x, y), r, paint)
                    }
                }
                onDrawWithContent {
                    drawContent()
                    drawImage(grainBitmap)
                }
            }
            .pointerInput(gestureState) {
                if (gestureState != GestureState.Rest) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    val bottomNavZonePx = with(density) { 80.dp.toPx() }
                    if (down.position.y > size.height - bottomNavZonePx) return@awaitEachGesture

                    var dragCommitted = false
                    var firstHapticFired = false
                    var thresholdHapticFired = false
                    val velocityTracker = VelocityTracker()
                    velocityTracker.addPosition(down.uptimeMillis, down.position)
                    var accumulatedY = 0f
                    val touchSlop = viewConfiguration.touchSlop

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break

                        val dy = change.positionChange().y
                        val dx = change.positionChange().x

                        if (!dragCommitted && abs(accumulatedY + dy) > touchSlop) {
                            if (abs(dy) > abs(dx) * 1.5f) {
                                dragCommitted = true
                            } else {
                                break
                            }
                        }

                        if (dragCommitted) {
                            change.consume()
                            accumulatedY = (accumulatedY + dy).coerceAtLeast(0f)
                            velocityTracker.addPosition(change.uptimeMillis, change.position)

                            if (!firstHapticFired) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                firstHapticFired = true
                                gestureState = GestureState.Dragging
                            }

                            scope.launch { dragOffsetY.snapTo(accumulatedY) }

                            if (!thresholdHapticFired && accumulatedY >= thresholdPx) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                thresholdHapticFired = true
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (dragCommitted) {
                        val velocityY = velocityTracker.calculateVelocity().y
                        if (shouldCommitGesture(accumulatedY, velocityY, thresholdPx)) {
                            gestureState = GestureState.Committed
                            scope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = targetCommitOffsetPx,
                                    animationSpec = spring(
                                        dampingRatio = 0.50f,
                                        stiffness = 280f,
                                        visibilityThreshold = 0.5f,
                                    ),
                                    initialVelocity = velocityY,
                                )
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
                            }
                        } else {
                            scope.launch {
                                dragOffsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = 0.72f,
                                        stiffness = 480f,
                                        visibilityThreshold = 0.5f,
                                    ),
                                    initialVelocity = velocityY,
                                )
                                gestureState = GestureState.Rest
                            }
                        }
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            EngineLine(
                engine = prefs.searchEngine,
                onEngineSelected = { viewModel.setSearchEngine(it) },
                customSearchEngineUrl = prefs.customSearchEngineUrl,
                onCustomUrlChange = { viewModel.setCustomSearchEngineUrl(it) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = greeting,
                style = TextStyle(
                    fontFamily = InstrumentSerif,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                    color = WarmCream,
                ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Spacer(
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        pillNaturalY = coords.positionInRoot().y.roundToInt()
                    },
            )

            Spacer(modifier = Modifier.weight(1f))

            BottomNavBar(
                onTabsClick = onNavigateToTabs,
                onBookmarksClick = onNavigateToBookmarks,
                onSettingsClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            )
        }

        // Blur / dim overlay — between background content and floating pill
        if (blurAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .run {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            blur(16.dp)
                        } else this
                    }
                    .background(Color.Black.copy(alpha = 0.40f * blurAlpha))
                    .clickable(
                        enabled = gestureState == GestureState.Committed,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        gestureState = GestureState.Rest
                        scope.launch {
                            dragOffsetY.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f),
                            )
                        }
                    },
            )
        }

        // Pill overlay — rendered above content, managed by gesture system
        if (!addressExpanded) {
            AddressField(
                state = when (gestureState) {
                    GestureState.Committed -> AddressFieldState.GestureFocused
                    else -> AddressFieldState.Pill
                },
                searchEngine = prefs.searchEngine,
                customSearchEngineUrl = prefs.customSearchEngineUrl,
                onSubmit = { url ->
                    onNavigateToBrowser(url)
                    gestureState = GestureState.Rest
                    scope.launch { dragOffsetY.snapTo(0f) }
                },
                onDismiss = {
                    gestureState = GestureState.Rest
                    scope.launch {
                        dragOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(dampingRatio = 0.72f, stiffness = 480f),
                        )
                    }
                },
                onPillTap = { addressExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .offset { IntOffset(0, pillNaturalY + dragOffsetY.value.roundToInt()) }
                    .align(Alignment.TopStart),
            )
        }

        if (addressExpanded) {
            AddressField(
                state = AddressFieldState.Expanded,
                searchEngine = prefs.searchEngine,
                customSearchEngineUrl = prefs.customSearchEngineUrl,
                onSubmit = { url ->
                    onNavigateToBrowser(url)
                    addressExpanded = false
                },
                onDismiss = { addressExpanded = false },
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    onTabsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(64.dp)
            .clip(PillShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.20f),
                shape = PillShape,
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Tab, contentDescription = "Tabs", tint = WarmCream) },
            label = "Tabs",
            onClick = onTabsClick,
            modifier = Modifier.weight(1f),
        )
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Bookmarks, contentDescription = "Bookmarks", tint = WarmCream) },
            label = "Bookmarks",
            onClick = onBookmarksClick,
            modifier = Modifier.weight(1f),
        )
        NavBarItem(
            icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint = WarmCream) },
            label = "Settings",
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NavBarItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = TextStyle(
                fontFamily = HankenGrotesk,
                fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
                fontSize = 11.sp,
                color = if (active) WarmCream else WarmCream.copy(alpha = 0.80f),
            ),
        )
    }
}

@Composable
private fun NamePromptDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var nameInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("What's your name?", fontFamily = InstrumentSerif, fontSize = 20.sp) },
        text = {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Your name", fontFamily = HankenGrotesk) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nameInput.trim()) }) {
                Text("Let's go", fontFamily = HankenGrotesk)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip", fontFamily = HankenGrotesk)
            }
        },
    )
}
