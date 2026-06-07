# Drag-to-Search + Animation Polish + Settings Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a rubber-band drag-to-search gesture on the homescreen, polish every screen's animations, and redesign the Settings screen with a glassmorphic design language that matches the rest of the app.

**Architecture:** The gesture is driven by `Animatable<Float>` for pixel-precise 1:1 pill tracking; the pill lives in the outer Box overlay (not the Column) so it always z-indexes above the blur layer. Settings is rebuilt around two new composables — `GlassCard` and `SettingRow` — that render type-first rows over the Mediterranean gradient. Animation polish is a series of small, independent changes per screen.

**Tech Stack:** Jetpack Compose, `pointerInput` + `awaitEachGesture`, `Animatable<Float>`, `AnimatedVisibility`, `spring()` animation specs, `HapticFeedbackConstants`, `WindowInsets`, `onGloballyPositioned`, `PagerDefaults.flingBehavior`

---

## File Map

| File | Role |
|---|---|
| `ui/components/AddressField.kt` | Add `GestureFocused` state; fix scrim fade; tune AnimatedContent spring |
| `ui/screens/start/StartScreen.kt` | Gesture detection, pill overlay, blur layer, greeting entrance, nav press |
| `ui/nav/HomePager.kt` | Snap spring + parallax |
| `ui/nav/UlivetoNavGraph.kt` | All nav transition specs |
| `ui/screens/tabs/TabsScreen.kt` | Stagger entrance + animated X-button close |
| `ui/screens/bookmarks/BookmarksScreen.kt` | Stagger entrance |
| `ui/screens/settings/SettingsScreen.kt` | Full redesign: gradient bg, GlassCard, SettingRow, SectionLabel |

---

## Phase 1 — Drag-to-Search

---

### Task 1: AddressField — GestureFocused state

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/components/AddressField.kt`

This adds a fourth state to `AddressFieldState`. When the gesture commits, the pill renders as a focused, inline text field (same pill shape, brighter treatment, cursor, keyboard opens). It does NOT use the full-screen scrim overlay — the gesture's own blur layer handles that.

- [ ] **Step 1: Add `GestureFocused` to the enum**

In `AddressField.kt`, change:
```kotlin
enum class AddressFieldState { Pill, HourglassCenter, Expanded }
```
to:
```kotlin
enum class AddressFieldState { Pill, HourglassCenter, Expanded, GestureFocused }
```

- [ ] **Step 2: Add the `GestureFocusedState` composable**

Add this private composable below `PillState` in `AddressField.kt`:

```kotlin
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
                    contentDescription = null,
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
```

- [ ] **Step 3: Wire `GestureFocused` into the `AnimatedContent` switch**

In the `when (targetState)` block inside `AddressField`, add:

```kotlin
AddressFieldState.GestureFocused -> GestureFocusedState(
    searchEngine = searchEngine,
    customSearchEngineUrl = customSearchEngineUrl,
    onSubmit = onSubmit,
    onDismiss = onDismiss,
    modifier = Modifier.fillMaxWidth(),
)
```

- [ ] **Step 4: Build to verify no compile errors**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/components/AddressField.kt
git commit -m "feat: add GestureFocused state to AddressField"
```

---

### Task 2: StartScreen — gesture infrastructure (offset + state)

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt`

The pill is removed from the Column and rendered as an overlay in the outer Box. A `Spacer` in the Column keeps the layout intact and measures the pill's natural Y position. An `Animatable<Float>` drives the Y offset in pixels.

- [ ] **Step 1: Add `GestureState` sealed interface at the top of `StartScreen.kt`**

Add above the `mediterraneanGreetings` list:

```kotlin
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
```

- [ ] **Step 2: Write a unit test for `shouldCommitGesture`**

Create `app/src/test/kotlin/it/uliveto/browser/GestureThresholdTest.kt`:

```kotlin
package it.uliveto.browser

import it.uliveto.browser.ui.screens.start.shouldCommitGesture
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class GestureThresholdTest {

    @Test
    fun `commits when offset exceeds threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 400f, velocityPx = 0f, thresholdPx = 360f))
    }

    @Test
    fun `commits when velocity exceeds threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 50f, velocityPx = 950f, thresholdPx = 360f))
    }

    @Test
    fun `cancels when both below threshold`() {
        assertFalse(shouldCommitGesture(offsetPx = 100f, velocityPx = 200f, thresholdPx = 360f))
    }

    @Test
    fun `commits exactly at offset threshold`() {
        assertTrue(shouldCommitGesture(offsetPx = 360f, velocityPx = 0f, thresholdPx = 360f))
    }
}
```

- [ ] **Step 3: Run the test**

```bash
./gradlew :app:test --tests "it.uliveto.browser.GestureThresholdTest"
```
Expected: 4 tests pass. (They will fail first because `shouldCommitGesture` is `internal` — if the test can't see it, make it `@VisibleForTesting` or move it to a companion. The function is pure so it can also live in a separate `GestureUtils.kt` file; just keep the import consistent.)

- [ ] **Step 4: Add gesture state + Animatable to `StartScreen`**

In `StartScreen`, add these state variables after the existing `var addressExpanded`:

```kotlin
var gestureState: GestureState by remember { mutableStateOf(GestureState.Rest) }
val dragOffsetY = remember { Animatable(0f) }
var pillNaturalY by remember { mutableIntStateOf(0) }
val scope = rememberCoroutineScope()
val density = LocalDensity.current
val view = LocalView.current
```

Add these computed values below:

```kotlin
val thresholdPx = with(density) { 120.dp.toPx() }
// Target Y for committed state: 45% of window height from top
val windowHeight = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
val targetCommitOffsetPx by remember(pillNaturalY, windowHeight) {
    derivedStateOf { windowHeight * 0.45f - pillNaturalY }
}
// Blur alpha: 0 during REST, builds during DRAGGING, full during COMMITTED
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
```

You will need these additional imports at the top of `StartScreen.kt`:
```kotlin
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
```

- [ ] **Step 5: Replace the pill in the Column with a measuring Spacer**

In `StartScreen`, find the section in the Column that renders `AddressField(Pill)` and the surrounding Spacer:

```kotlin
if (!addressExpanded) {
    AddressField(
        state = AddressFieldState.Pill,
        ...
    )
} else {
    Spacer(modifier = Modifier.height(52.dp))
}
```

Replace with a Spacer that measures its Y position:

```kotlin
Spacer(
    modifier = Modifier
        .height(52.dp)
        .fillMaxWidth()
        .onGloballyPositioned { coords ->
            pillNaturalY = coords.positionInRoot().y.roundToInt()
        },
)
```

- [ ] **Step 6: Add the pill overlay in the outer Box**

In the outer Box of `StartScreen` (the one with `fillMaxSize().background(themeGradient)`), add after the Column:

```kotlin
// Pill overlay — rendered above blur layer, z-indexed last
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
```

- [ ] **Step 7: Build and verify pill appears at correct position**

```bash
./gradlew :app:assembleDebug
```
Expected: BUILD SUCCESSFUL. Install and open the app — the pill should appear exactly where it did before. The Column Spacer reserves its space; the pill overlay renders over it at the same position.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt \
        app/src/test/kotlin/it/uliveto/browser/GestureThresholdTest.kt
git commit -m "feat: extract pill to overlay layer with Animatable offset infrastructure"
```

---

### Task 3: StartScreen — gesture detection + haptics

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt`

- [ ] **Step 1: Add `pointerInput` gesture handler to the outer Box**

Add the following modifier to the outer Box (alongside `fillMaxSize()`, `background()`, `drawWithCache()`):

```kotlin
.pointerInput(gestureState) {
    // Only intercept when at rest — committed state manages its own dismissal via BackHandler
    if (gestureState != GestureState.Rest) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)

        // Ignore touches starting in the bottom nav zone (approx 80dp from bottom)
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

            // Disambiguate: only commit to vertical if predominant axis is vertical
            if (!dragCommitted && abs(accumulatedY + dy) > touchSlop) {
                if (abs(dy) > abs(dx) * 1.5f) {
                    dragCommitted = true
                } else {
                    break // horizontal gesture — let HorizontalPager have it
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

        // Gesture ended — decide commit or cancel
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
}
```

- [ ] **Step 2: Add blur/dim overlay in the outer Box**

Add this between the Column and the pill overlay in the outer Box:

```kotlin
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
```

Add to imports:
```kotlin
import android.os.Build
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.IntOffset
```

- [ ] **Step 3: Build and test gesture manually**

```bash
./gradlew :app:assembleDebug
```

Install on device. Test:
1. Drag down slowly from anywhere on the homescreen — pill should follow your finger 1:1
2. Release before ~120dp — pill snaps back, no haptic on release
3. Drag down past 120dp then release — pill springs to center with bounce, screen blurs, keyboard opens
4. While search is open, tap the blurred area — pill returns to rest
5. While search is open, press Back — pill returns to rest
6. Swipe left/right to switch pager pages — still works (horizontal swipes unaffected)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt
git commit -m "feat: drag-to-search gesture with spring physics, blur overlay and haptics"
```

---

## Phase 2 — Settings Redesign

---

### Task 4: SettingsScreen — GlassCard, SettingRow, SectionLabel components

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt`

Build the three new composables before touching the layout. Each has a single clear responsibility.

- [ ] **Step 1: Add `SectionLabel` composable**

Replace the existing `SectionHeader` composable with:

```kotlin
@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = HankenGrotesk,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.4.sp,
        color = WarmCream.copy(alpha = 0.28f),
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
    )
}
```

- [ ] **Step 2: Add `GlassCard` composable**

Add below `SectionLabel`:

```kotlin
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmCream.copy(alpha = 0.065f))
            .border(
                width = 1.dp,
                color = WarmCream.copy(alpha = 0.13f),
                shape = RoundedCornerShape(16.dp),
            ),
        content = content,
    )
}
```

- [ ] **Step 3: Add `SettingRow` composable**

Add below `GlassCard`:

```kotlin
@Composable
private fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showArrow: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 14.dp, vertical = 11.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = HankenGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = WarmCream.copy(alpha = 0.90f),
                lineHeight = 17.sp,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    color = WarmCream.copy(alpha = 0.40f),
                    lineHeight = 13.sp,
                )
            }
        }
        if (value != null) {
            Text(
                text = value,
                fontFamily = InstrumentSerif,
                fontStyle = FontStyle.Italic,
                fontSize = 12.sp,
                color = WarmCream.copy(alpha = 0.52f),
                modifier = Modifier.padding(end = if (showArrow) 4.dp else 0.dp),
            )
        }
        if (trailing != null) {
            trailing()
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = WarmCream.copy(alpha = 0.22f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp),
            )
        }
    }
}
```

Add `RowDivider` helper between rows inside a card:

```kotlin
@Composable
private fun RowDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = WarmCream.copy(alpha = 0.07f),
    )
}
```

You'll need these additional imports:
```kotlin
import androidx.compose.ui.text.font.FontStyle
import it.uliveto.browser.ui.tokens.WarmCream
import it.uliveto.browser.ui.LocalUlivetoColors
```

- [ ] **Step 4: Delete the old `SectionHeader`, `SectionDivider`, `ClickableRow` composables**

Remove the three old private composables (`SectionHeader`, `SectionDivider`, `ClickableRow`) from the bottom of the file — they are fully replaced.

- [ ] **Step 5: Build to verify components compile**

```bash
./gradlew :app:compileDebugKotlin
```
Expected: BUILD SUCCESSFUL (there will be red use-sites of the old composables — fix in the next task).

- [ ] **Step 6: Commit components**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt
git commit -m "feat(settings): add GlassCard, SettingRow, SectionLabel components"
```

---

### Task 5: SettingsScreen — gradient background + full layout rewire

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1: Add gradient background to the Scaffold**

At the top of `SettingsScreen`, add:

```kotlin
val ulivetoColors = LocalUlivetoColors.current
val themeGradient = remember(ulivetoColors) { Brush.radialGradient(ulivetoColors.gradientColors) }
```

Replace the `Scaffold` with:

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(brush = themeGradient),
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontFamily = InstrumentSerif,
                        fontStyle = FontStyle.Italic,
                        fontSize = 20.sp,
                        color = WarmCream.copy(alpha = 0.92f),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WarmCream.copy(alpha = 0.50f),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // sections go here — built in steps below
        }
    }
}
```

Add needed imports:
```kotlin
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
```

- [ ] **Step 2: Rewire Personal section**

```kotlin
item {
    SectionLabel("Personal")
    GlassCard {
        var nameInput by remember(prefs.userName) { mutableStateOf(prefs.userName) }
        var editing by remember { mutableStateOf(false) }

        if (editing) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "Name",
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = WarmCream.copy(alpha = 0.90f),
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.setUserName(nameInput.trim())
                            keyboard?.hide()
                            editing = false
                        }),
                        placeholder = { Text("Your name", fontFamily = HankenGrotesk) },
                    )
                    TextButton(onClick = {
                        viewModel.setUserName(nameInput.trim())
                        keyboard?.hide()
                        editing = false
                    }) { Text("Save", fontFamily = HankenGrotesk, color = WarmCream) }
                }
            }
        } else {
            SettingRow(
                label = "Name",
                value = if (prefs.userName.isBlank()) "Not set" else prefs.userName,
                showArrow = true,
                onClick = { editing = true },
            )
        }
    }
}
```

- [ ] **Step 3: Rewire Search section**

```kotlin
item {
    Spacer(Modifier.height(8.dp))
    SectionLabel("Search")
    GlassCard {
        val engineName = if (prefs.searchEngine == SearchEngine.Custom && prefs.customSearchEngineUrl.isNotBlank()) {
            prefs.customSearchEngineUrl.substringAfter("://").substringBefore("/").substringBefore("?")
        } else {
            prefs.searchEngine.displayName
        }
        var showEngineSheet by remember { mutableStateOf(false) }
        SettingRow(
            label = "Search engine",
            value = engineName,
            showArrow = true,
            onClick = { showEngineSheet = true },
        )
        if (showEngineSheet) {
            EngineLine(
                engine = prefs.searchEngine,
                onEngineSelected = { viewModel.setSearchEngine(it); showEngineSheet = false },
                customSearchEngineUrl = prefs.customSearchEngineUrl,
                onCustomUrlChange = { viewModel.setCustomSearchEngineUrl(it) },
            )
        }
    }
}
```

Note: `EngineLine` triggers its sheet on first composition via `showSheet`. Wrap it in a `LaunchedEffect` or use the sheet directly. Simplest fix: keep `EngineLine` as-is but call `showEngineSheet` to toggle a local flag that shows a copy of the sheet. Actually the cleanest approach is just to keep `EngineLine` as an invisible trigger composable only when `showEngineSheet == true` — `EngineLine` auto-shows its sheet on first composition when `showSheet` starts `true`. Look at `EngineLine` — it uses `var showSheet by remember { mutableStateOf(false) }` set by user click. So: call `EngineLine` composable always (it won't show the sheet until clicked), but trigger the click programmatically. Since `EngineLine` manages its own sheet state internally, the simplest integration is to keep `EngineLine` visible but hidden (zero size) and call `onClick` on it. **Simpler**: duplicate the bottom sheet logic inline. Even simpler: just leave `EngineLine` as the clickable row — keep its existing `Row(modifier = Modifier.clickable { showSheet = true })` and drop it directly into the `GlassCard` after a `SettingRow`-style wrapper. The actual implementation here is:

```kotlin
GlassCard {
    EngineLine(
        engine = prefs.searchEngine,
        onEngineSelected = { viewModel.setSearchEngine(it) },
        customSearchEngineUrl = prefs.customSearchEngineUrl,
        onCustomUrlChange = { viewModel.setCustomSearchEngineUrl(it) },
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
    )
}
```

`EngineLine` already renders as a tappable row. Style it to match by overriding its typography inside, or just use it as-is — the glass card wrapping it is the key change. Return to this to tighten typography later if needed.

- [ ] **Step 4: Rewire Appearance section**

```kotlin
item {
    Spacer(Modifier.height(8.dp))
    SectionLabel("Appearance")
    GlassCard {
        // Theme label
        Text(
            text = "Theme",
            fontFamily = HankenGrotesk,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = WarmCream.copy(alpha = 0.90f),
            modifier = Modifier.padding(start = 14.dp, top = 11.dp, bottom = 8.dp),
        )
        // Swatches row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
        ) {
            items(themeSwatches) { (theme, _, swatchColor) ->
                ThemeSwatch(
                    label = null, // no labels — colours speak for themselves
                    color = swatchColor,
                    selected = prefs.theme == theme,
                    onClick = { viewModel.setTheme(theme) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        RowDivider()
        // Navigation style row — taps cycle between the two options
        val navLabel = if (prefs.navStyle == NavStyle.Hourglass) "Bubbles" else "Classic"
        SettingRow(
            label = "Navigation",
            value = navLabel,
            showArrow = true,
            onClick = {
                val next = if (prefs.navStyle == NavStyle.Hourglass) NavStyle.Classic else NavStyle.Hourglass
                viewModel.setNavStyle(next)
            },
        )
    }
}
```

Update `ThemeSwatch` to hide the label when `null`:

```kotlin
@Composable
private fun ThemeSwatch(
    label: String?,          // nullable — null = no label
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) WarmCream.copy(alpha = 0.85f)
                            else WarmCream.copy(alpha = 0.15f),
                    shape = CircleShape,
                )
                .clickable(onClick = onClick),
        )
        if (label != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = HankenGrotesk,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = WarmCream.copy(alpha = if (selected) 1f else 0.75f),
            )
        }
    }
}
```

Update the existing call site in `themeSwatches.items`:

```kotlin
ThemeSwatch(label = null, color = swatchColor, selected = prefs.theme == theme, onClick = { viewModel.setTheme(theme) })
```

- [ ] **Step 5: Rewire Privacy section**

```kotlin
item {
    Spacer(Modifier.height(8.dp))
    SectionLabel("Privacy")
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Safe Browsing",
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = WarmCream.copy(alpha = 0.90f),
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    "Sends URLs to Google",
                    fontFamily = HankenGrotesk,
                    fontSize = 9.sp,
                    color = WarmCream.copy(alpha = 0.40f),
                )
            }
            Switch(
                checked = prefs.safeBrowsingEnabled,
                onCheckedChange = { viewModel.setSafeBrowsing(it) },
            )
        }
        RowDivider()
        SettingRow(
            label = "Privacy Receipts",
            subtitle = "Verified network capture per release",
            showArrow = true,
            onClick = onNavigateToPrivacyReceipts,
        )
        RowDivider()
        SettingRow(
            label = "Clear browsing data",
            showArrow = false,
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFB25737).copy(alpha = 0.55f),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(14.dp),
                )
            },
            onClick = { showClearDataDialog = true },
        )
    }
}
```

- [ ] **Step 6: Rewire About section + remove cookie row from main list**

The cookie policy dialog stays — it moves to `PrivacyReceiptsScreen` in scope. Remove it from main list here.

```kotlin
item {
    Spacer(Modifier.height(8.dp))
    SectionLabel("About")
    GlassCard {
        SettingRow(
            label = "Version",
            value = BuildConfig.VERSION_NAME,
        )
        RowDivider()
        SettingRow(
            label = "Open source",
            value = "GitHub",
            showArrow = true,
            onClick = { uriHandler.openUri("https://github.com/prollyyes/uliveto") },
        )
        RowDivider()
        SettingRow(
            label = "Licenses",
            showArrow = true,
            onClick = { showLicensesDialog = true },
        )
    }
    Spacer(Modifier.height(24.dp))
}
```

- [ ] **Step 7: Build and verify settings screen visually**

```bash
./gradlew :app:assembleDebug
```

Install on device. Open Settings. Verify:
- Gradient background matches homescreen
- Glass cards render with visible warm-cream tinted borders
- Each row shows label (HankenGrotesk) + value (InstrumentSerif italic)
- Theme swatches are 22dp circles, active one has cream ring
- No plain white background visible anywhere

- [ ] **Step 8: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt
git commit -m "feat(settings): glassmorphic redesign with gradient bg, GlassCard rows"
```

---

## Phase 3 — Animation Polish

---

### Task 6: NavGraph transition specs

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/nav/UlivetoNavGraph.kt`

- [ ] **Step 1: Update the NavHost default transitions**

The `start` route (HomePager) uses default fade for all auxiliary screens. Change the NavHost defaults:

```kotlin
NavHost(
    navController = navController,
    startDestination = "start",
    modifier = modifier,
    enterTransition = {
        slideInVertically(initialOffsetY = { it / 8 }, animationSpec = tween(300)) +
        fadeIn(animationSpec = tween(300))
    },
    exitTransition = {
        fadeOut(animationSpec = tween(220))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(220))
    },
    popExitTransition = {
        slideOutVertically(targetOffsetY = { it / 8 }, animationSpec = tween(220)) +
        fadeOut(animationSpec = tween(220))
    },
)
```

- [ ] **Step 2: Update Browser route transitions**

```kotlin
composable(
    route = "browser/{tabId}",
    enterTransition = {
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) +
        scaleIn(initialScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing))
    },
    exitTransition = {
        slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(280, easing = FastOutSlowInEasing)) +
        scaleOut(targetScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing))
    },
    popEnterTransition = {
        slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(280, easing = FastOutSlowInEasing)) +
        scaleIn(initialScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing))
    },
    popExitTransition = {
        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280, easing = FastOutSlowInEasing)) +
        scaleOut(targetScale = 0.97f, animationSpec = tween(280, easing = FastOutSlowInEasing))
    },
)
```

Add imports:
```kotlin
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
```

- [ ] **Step 3: Build and test transitions**

```bash
./gradlew :app:assembleDebug
```

Install. Navigate Home → Browser (should slide-in with subtle scale). Press Back (reverse). Navigate to Settings (should rise from below, fall away on back).

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/nav/UlivetoNavGraph.kt
git commit -m "polish: nav transitions — browser slides+scales, aux screens rise vertically"
```

---

### Task 7: HomePager — snap spring + page parallax

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/nav/HomePager.kt`

- [ ] **Step 1: Add custom fling behaviour for spring snap**

```kotlin
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.animation.core.spring

// Inside HomePager composable, after pagerState:
val snapFlingBehavior = PagerDefaults.flingBehavior(
    state = pagerState,
    snapAnimationSpec = spring(dampingRatio = 0.78f, stiffness = 350f),
)
```

Pass it to `HorizontalPager`:
```kotlin
HorizontalPager(
    state = pagerState,
    flingBehavior = snapFlingBehavior,
    modifier = Modifier.fillMaxSize(),
) { page ->
```

- [ ] **Step 2: Add parallax to page content**

Inside the `HorizontalPager` page block, wrap each page composable with a `Box` that applies a parallax offset:

```kotlin
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
            0 -> TabsScreen(...)
            1 -> StartScreen(...)
            2 -> BookmarksScreen(...)
        }
    }
}
```

Add imports:
```kotlin
import androidx.compose.ui.graphics.graphicsLayer
```

- [ ] **Step 3: Build and test**

```bash
./gradlew :app:assembleDebug
```

Swipe between Home/Tabs/Bookmarks — the page snap should feel springy with a slight elastic settle. The pages should have a subtle depth parallax as they move.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/nav/HomePager.kt
git commit -m "polish: HomePager spring snap + page parallax"
```

---

### Task 8: StartScreen — greeting entrance + bottom nav press feedback

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt`

- [ ] **Step 1: Animate the greeting text entrance**

Wrap the greeting `Text` in an `AnimatedVisibility`:

```kotlin
var greetingVisible by remember { mutableStateOf(false) }
LaunchedEffect(Unit) { greetingVisible = true }

AnimatedVisibility(
    visible = greetingVisible,
    enter = fadeIn(animationSpec = tween(400)) +
            slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { -12 },
) {
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
}
```

Add imports:
```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
```

- [ ] **Step 2: Add press scale to `NavBarItem`**

In `NavBarItem`, add press-scale feedback using `InteractionSource`:

```kotlin
@Composable
private fun NavBarItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "navItemScale",
    )

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .graphicsLayer { scaleX = scale; scaleY = scale }
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
```

Add imports:
```kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
```

- [ ] **Step 3: Build and test**

```bash
./gradlew :app:assembleDebug
```

Open the homescreen. The greeting should fade+slide in smoothly on each visit. Tap the bottom nav icons — they should scale down on press.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/start/StartScreen.kt
git commit -m "polish: greeting fade-in entrance, nav icon press scale"
```

---

### Task 9: AddressField — scrim fade + AnimatedContent spring

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/components/AddressField.kt`

- [ ] **Step 1: Animate the scrim alpha in `ExpandedState`**

In `ExpandedState`, the scrim Box currently uses a hardcoded `Color.Black.copy(alpha = 0.45f)`. Animate it:

```kotlin
var scrimVisible by remember { mutableStateOf(false) }
LaunchedEffect(Unit) { scrimVisible = true }
val scrimAlpha by animateFloatAsState(
    targetValue = if (scrimVisible) 0.45f else 0f,
    animationSpec = tween(280),
    label = "scrimAlpha",
)

// Then in the scrim Box:
.background(Color.Black.copy(alpha = scrimAlpha))
```

- [ ] **Step 2: Tune `AnimatedContent` to use spring for scale**

In `AddressField`, replace the existing `transitionSpec` with:

```kotlin
transitionSpec = {
    val fadeSpec = tween<Float>(durationMillis = 240, easing = FastOutSlowInEasing)
    val scaleSpec = spring<Float>(dampingRatio = 0.7f, stiffness = 400f)
    (fadeIn(fadeSpec) + scaleIn(scaleSpec, initialScale = 0.96f))
        .togetherWith(fadeOut(fadeSpec) + scaleOut(scaleSpec, targetScale = 0.96f))
        .using(SizeTransform(clip = false))
},
```

- [ ] **Step 3: Build and test**

```bash
./gradlew :app:assembleDebug
```

Tap the pill → scrim should fade in smoothly (no harsh black flash). The transition between Pill and Expanded should feel springy.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/components/AddressField.kt
git commit -m "polish: scrim fade-in, spring AnimatedContent on AddressField"
```

---

### Task 10: TabsScreen — stagger entrance + animated close

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/tabs/TabsScreen.kt`

- [ ] **Step 1: Add stagger entrance to tab cards**

In `TabsScreen`, the `LazyColumn` items block:

```kotlin
items(tabs, key = { it.id }) { tab ->
    val index = tabs.indexOf(tab)

    // Stagger entrance: each card fades+slides in with delay capped at 5 items
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
                if (value != SwipeToDismissBoxValue.Settled) onCloseTab(tab.id)
                true
            },
        )
        SwipeToDismissBox(
            state = dismissState,
            // ... existing swipe-to-dismiss content unchanged
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
```

Add imports:
```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
```

- [ ] **Step 2: Build and test**

```bash
./gradlew :app:assembleDebug
```

Open the Tabs screen with multiple tabs open. Cards should stagger in with a ~30ms delay between them.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/tabs/TabsScreen.kt
git commit -m "polish: tab cards stagger entrance animation"
```

---

### Task 11: BookmarksScreen — stagger entrance

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/bookmarks/BookmarksScreen.kt`

- [ ] **Step 1: Add stagger entrance to bookmark cards**

In `BookmarksScreen`, the `LazyColumn` items block:

```kotlin
items(bookmarks, key = { it.id }) { bookmark ->
    val index = bookmarks.indexOf(bookmark)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
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
```

Add the same imports as Task 10.

- [ ] **Step 2: Build and test**

```bash
./gradlew :app:assembleDebug
```

Open Bookmarks with several entries. Cards should stagger in smoothly.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/bookmarks/BookmarksScreen.kt
git commit -m "polish: bookmark cards stagger entrance animation"
```

---

### Task 12: SettingsScreen — stagger entrance

**Files:**
- Modify: `app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt`

- [ ] **Step 1: Add stagger to settings sections**

Wrap each top-level `item { ... }` block in the `LazyColumn` with a staggered `AnimatedVisibility`. Since the items are sections (not individual rows), use a small index counter. The cleanest approach in `LazyColumn` for settings is `itemsIndexed` on a list of section composables, but since sections are heterogeneous, use a manual index:

```kotlin
// At the top of the LazyColumn lambda, add a counter:
var sectionIndex = 0

// Wrap each `item { ... }` block like this:
item {
    val idx = sectionIndex++
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(idx.coerceAtMost(10) * 20L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { -8 },
    ) {
        // ... section content (SectionLabel + GlassCard)
    }
}
```

Apply this pattern to each of the 5 section items (Personal, Search, Appearance, Privacy, About). Spacer items between sections don't need animation.

- [ ] **Step 2: Build and test**

```bash
./gradlew :app:assembleDebug
```

Open Settings. The sections should cascade in with a subtle upward slide.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/it/uliveto/browser/ui/screens/settings/SettingsScreen.kt
git commit -m "polish: settings sections stagger entrance"
```

---

## Self-Review Against Spec

### Spec coverage check

| Spec requirement | Task |
|---|---|
| Drag from anywhere, 1:1 tracking, reversible | Task 3 |
| Pill follows finger | Task 3 |
| Spring to center (damping 0.50, stiffness 280) | Task 3 |
| Cancel spring (damping 0.72, stiffness 480) | Task 3 |
| Commit threshold: 120dp or 900px/s velocity | Task 2 (`shouldCommitGesture`), Task 3 |
| 3-beat haptic pattern | Task 3 |
| No haptic on cancel | Task 3 |
| Blur API 31+, dim fallback API 26–30 | Task 3 |
| GestureFocused pill state | Task 1 |
| Dismiss via Back or tap-outside | Task 3 |
| Nav transitions: browser slide+scale | Task 6 |
| Nav transitions: aux screens vertical rise | Task 6 |
| HomePager spring snap + parallax | Task 7 |
| Greeting entrance animation | Task 8 |
| Nav icon press scale | Task 8 |
| AddressField scrim fade + spring | Task 9 |
| Tab cards stagger | Task 10 |
| Bookmark cards stagger | Task 11 |
| Settings gradient background | Task 5 |
| GlassCard component | Task 4 |
| SettingRow (label + italic value) | Task 4 |
| SectionLabel (all-caps, muted) | Task 4 |
| No icons anywhere in settings | Tasks 4–5 |
| Settings stagger | Task 12 |
| Clear data arrow in terracotta red | Task 5 |
| Theme swatches 22dp with cream ring | Task 5 |
| Cookie policy removed from main list | Task 5 |
| BrowserScreen chrome easing | Not addressed — spec says "if currently instant-snapping." Inspect `BrowserScreen.kt` before starting and add a task only if snap-hiding is present. |

**One gap:** BrowserScreen chrome easing. Add as a quick-check task only if the implementer finds snap-hide chrome in `BrowserScreen.kt` (look for `animationSpec = snap()` or no animation spec on chrome visibility changes).
