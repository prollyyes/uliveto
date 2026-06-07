# Drag-to-Search Gesture + Animation Polish Design

**Date:** 2026-06-08  
**Status:** Approved

---

## Overview

Two goals addressed in this spec:

1. **Goal 1 — Drag-to-Search**: A new gesture on the homescreen (`StartScreen`) where dragging down anywhere on the screen pulls the search pill with your finger. Releasing past a threshold springs the pill to vertical center, blurs the background, and opens the keyboard for search.

2. **Goal 2 — Animation Polish**: A sweep across every screen to eliminate rough transitions, add entrance animations, and make motion feel cohesive.

---

## Goal 1: Drag-to-Search Gesture

### State Machine

| State | Description |
|---|---|
| **REST** | Pill at natural column position. `dragOffsetY = 0`. Tap still works (existing behaviour). |
| **DRAGGING** | Vertical drag detected anywhere on screen (excluding bottom nav zone). Pill offset = `dragDeltaY × 1.0` (1:1 tracking). Reversible — dragging back up moves pill back. Blur opacity = `clamp(offset / threshold, 0, 0.6)` builds progressively. |
| **COMMITTED** | Release with velocity ≥ 300 dp/s **or** offset ≥ 120 dp. Spring to center. Haptic beat 3. Full blur. Focus text field. |
| **SEARCH OPEN** | Pill at ~45% from top (vertical center of non-keyboard area). Keyboard visible. Blurred background. Dismiss via Back or tap-outside → snap to REST. |
| **CANCELLED** | Release below threshold. Fast spring-back to `dragOffsetY = 0`. Blur fades out. No haptic. |

### Gesture Implementation

- **Detection**: `pointerInput` + `awaitEachGesture` on the outer `Box` in `StartScreen`. Manually track `ACTION_DOWN / MOVE / UP`.
- **Disambiguation**: Only commit to vertical gesture if vertical component > horizontal component by a 1.5× ratio (prevents conflict with `HorizontalPager` swipes).
- **Exclusion zone**: Bottom nav bar height + `navigationBarsPadding` — touches originating there do not trigger the drag.
- **Offset driver**: `Animatable<Float>` named `dragOffsetY`, storing values in **pixels**. On move events: `snapTo(accumulated pixel delta)` (direct, no physics lag). Pill applied via `Modifier.offset { IntOffset(0, dragOffsetY.value.roundToInt()) }` (pixel-based offset lambda avoids dp conversion artifacts).

### Spring Parameters

**Commit spring (pill → center):**
```
dampingRatio  = 0.50  (underdamped → overshoot bounce)
stiffness     = 280
initialVelocity = last drag velocity (carries momentum)
approx duration = 420 ms
```

**Cancel spring (pill → rest):**
```
dampingRatio  = 0.72  (slight overshoot, snappy)
stiffness     = 480
initialVelocity = last drag velocity
approx duration = 240 ms
```

**Blur/dim:**
- During drag: instant (tracks `offset / threshold` ratio, no extra spring)
- On commit: `tween(300ms, FastOutSlowIn)` to full opacity
- On dismiss: `tween(220ms, FastOutSlowIn)` to 0

### Target Position (Committed State)

Pill lands at `targetY = (screenHeight - keyboardHeight) / 2 - pillHeight / 2`.  
This is computed at gesture-commit time using `WindowInsets` to get the current keyboard height estimate; if keyboard not yet visible, use `screenHeight * 0.45f` as approximation.

### Blur / Dim Effect

**API 31+ (real blur):**
- Background content layer wrapped with `Modifier.blur(16.dp)` gated on `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S`.
- Animated via `animateFloatAsState` on a `blurAlpha` value (0 → 1).
- A `Color.Black.copy(0.40f)` semi-transparent overlay sits above the blur for depth.

**API 26–30 (dim fallback):**
- Same dark overlay lerping from 0 → `0.55f` alpha. No blur filter.
- Detected at composition time via `Build.VERSION.SDK_INT`.

**Pill in search state (SEARCH OPEN):**
- Background: `rgba(255,255,255, 0.22)`
- Border: `rgba(255,255,255, 0.55)`
- Glow ring: `2.5.dp` shadow in `rgba(255,255,255, 0.10)`
- `BasicTextField` replaces placeholder text (same field as current `ExpandedState` but rendered inline in the pill shape, not at bottom of screen)
- Cursor in `WarmCream`

### Haptic Pattern

Three distinct beats, all via `View.performHapticFeedback`:

| Beat | Trigger | Constant | Character |
|---|---|---|---|
| 1 | Drag first recognized | `VIRTUAL_KEY` | Soft tick |
| 2 | Threshold crossed (120 dp or velocity ≥ 300 dp/s) | `CONTEXT_CLICK` | Crisp click |
| 3 | Pill arrives at center (spring settling) | `KEYBOARD_RELEASE` | Satisfying thud |

**No haptic on cancel** — silence reinforces that nothing was committed.

### Architecture Notes

- The existing `AddressFieldState.Expanded` (full-screen bottom overlay) is **not reused** for the gesture path. Instead, a new `AddressFieldState.GestureFocused` is added that renders the pill in-place at its animated position with a `BasicTextField` inside.
- The gesture state is owned by `StartScreen` as `var gestureState: GestureState` (sealed class: `Rest`, `Dragging(offsetY)`, `Committed`).
- The `HomePager`'s `HorizontalPager` is unaffected — it only responds to predominantly horizontal drags, so vertical drags pass through naturally.

---

## Goal 2: Animation Polish

### NavGraph Transitions (`UlivetoNavGraph.kt`)

| Route | Before | After |
|---|---|---|
| Home → Browser (enter) | `slideInHorizontally(it, 280ms)` | `slideInHorizontally(it, 280ms) + scaleIn(0.97f, 280ms, FastOutSlowIn)` |
| Browser → Home (pop enter) | `slideInHorizontally(-it/4, 280ms)` | `slideInHorizontally(-it/4, 280ms) + scaleIn(0.97f, 280ms)` |
| Home → Settings / Tabs / Bookmarks | `fadeIn(250ms)` | `slideInVertically(it/6, 300ms) + fadeIn(300ms)` (sheet-like rise) |
| Settings / Tabs / Bookmarks → Home (pop) | `fadeOut(250ms)` | `slideOutVertically(it/6, 220ms) + fadeOut(220ms)` |

**Design principle**: Browser slides horizontally (directional navigation). Auxiliary screens (settings, tabs, bookmarks) rise/fall vertically (overlay pattern).

### HomePager (`HomePager.kt`)

- Explicit `snapAnimationSpec = spring(dampingRatio = 0.78f, stiffness = 350f)` on `HorizontalPager`.
- Parallax on outgoing page: drive `graphicsLayer { translationX = pageOffset * -30.dp.toPx() }` on page content.

### StartScreen (`StartScreen.kt`)

| Element | Fix |
|---|---|
| Pill → Expanded layout | Pill exit: `AnimatedVisibility(fadeOut + shrinkVertically)` so it dissolves. Overlay expands with `fadeIn + scaleIn(0.97f)`. Scrim animates from 0 → 0.45f alpha (`tween(280ms)`). |
| Bottom nav icon press | `Modifier.scale(if (pressed) 0.88f else 1f)` driven by `InteractionSource`. |
| Greeting entrance | `AnimatedVisibility` with `fadeIn(tween(400ms)) + slideInVertically(initialOffset = -12px)` on first composition per visit. |

### AddressField (`AddressField.kt`)

| Element | Fix |
|---|---|
| `AnimatedContent` scale spec | Change from `tween(280ms)` to `spring(dampingRatio = 0.7f, stiffness = 400f)` for scale; keep `tween(240ms)` for fade. |
| Scrim fade | Animate from 0 → 0.45f with `tween(280ms)`. Currently appears at full opacity instantly. |

### TabsScreen (`TabsScreen.kt`)

- **Stagger entrance**: Each tab card wrapped in `AnimatedVisibility` with `fadeIn + slideInVertically`, delay = `index × 30ms`, capped at 5 items (max 150ms total stagger).
- **Tab close**: Set `closing = true` on tap → `AnimatedVisibility(visible = !closing)` exits with `shrinkVertically(tween(200ms)) + fadeOut(150ms)` → after 200ms (`LaunchedEffect` + `delay(200)`) call `onCloseTab(tabId)`. The actual `TabManager.closeTab` fires after the exit animation completes.

### BookmarksScreen (`BookmarksScreen.kt`)

- **Stagger entrance**: `fadeIn + slideInVertically`, delay = `index × 25ms`, capped at 8 items (200ms total).
- **Bookmark remove**: `shrinkVertically + fadeOut(180ms)` before removal (deferred-remove pattern).

### SettingsScreen (`SettingsScreen.kt`) — see also Goal 3 below

- **Stagger entrance**: `fadeIn + slideInVertically(-8px)`, delay = `index × 20ms`, capped at 10 items.

### BrowserScreen (`BrowserScreen.kt`)

- Chrome bar scroll-hide: wrap height/alpha changes in `animateFloatAsState(tween(180ms, FastOutSlowIn))` if currently instant-snapping.

---

## Goal 3: Settings Screen Redesign

### Design Language

**Words over icons.** No leading icons. Labels are precise enough to be self-evident; subtitles carry nuance. This is a deliberate departure from standard Material settings — cleaner, more confident, distinctly Uliveto.

**Serif italic for values.** The current setting value (e.g. "DuckDuckGo", "Bubbles", "Eddie") is displayed in `InstrumentSerif` italic trailing the label. This is the same typeface as the homescreen greeting — a visual thread connecting identity across the whole app.

**Real frosted glass.** Cards use `backdrop-filter: blur(20.dp) saturate(1.4)` (via `Modifier.graphicsLayer` + `RenderEffect` on API 31+; solid `rgba` fallback on API 26–30) over the Mediterranean gradient, so the background world shows through.

### Background & Top Bar

**Background**: The Mediterranean gradient (`Brush.radialGradient(ulivetoColors.gradientColors)`) fills the screen — same as `StartScreen`. Settings feels like part of the same world, not a separate system screen.

**Top bar**: `TopAppBar` with `containerColor = Color.Transparent`. Title: `InstrumentSerif` italic 20sp, `WarmCream.copy(0.92f)`. Back icon: `WarmCream.copy(0.50f)`. No elevation or shadow.

### Section Structure

```
SectionLabel   ← 9sp all-caps, letter-spacing 1.4sp, WarmCream 28% — structural scaffolding
GlassCard      ← rounded-16dp, blur backdrop, WarmCream 6.5% bg, WarmCream 13% border
                  inset top highlight: WarmCream 10% at 1dp
  SettingRow   ← label + optional subtitle | trailing value or control
  ──────────── ← 0.5dp WarmCream 7% divider between rows, none after last
```

### SettingRow Anatomy

```
[Label  13sp HankenGrotesk Medium  WarmCream 90%]   [Value InstrumentSerif italic 12sp WarmCream 52%]  [›]
[Sub    9sp  HankenGrotesk Normal  WarmCream 40%]
```

No leading icon. No trailing icon except `›` (`ArrowForward`, `WarmCream.copy(0.22f)`) on navigable rows. Toggles replace the arrow on boolean rows.

**Minimum row height**: 42dp with 11dp vertical padding.

### Section Labels

Replace `SectionHeader` composable: `InstrumentSerif` removed from headers entirely. New `SectionLabel`: `HankenGrotesk` 8.5sp, all-caps, `WarmCream.copy(0.28f)`, letter-spacing 1.4sp. Padding: `0.dp` horizontal, `6.dp` bottom. Sits above each `GlassCard`, not inside it.

### Specific Item Changes

| Item | Before | After |
|---|---|---|
| Your name | Plain text + `TextButton("Edit")` | `SettingRow` label "Name", value shows current name in italic serif. Tap → inline edit unchanged. |
| Search engine | `EngineLine` widget inline in `Row` | `SettingRow` label "Search engine", value shows engine name in italic serif. Tap → opens existing `EngineLine` bottom sheet. |
| Navigation style | `SingleChoiceSegmentedButtonRow` | `SettingRow` label "Navigation", value shows current style in italic serif. Tap → cycles `NavStyle` options directly (two states only, no sheet needed). |
| Theme | `LazyRow` of 48dp circles | Inside `GlassCard`: label "Theme" above a `LazyRow` of 22dp swatch circles. Selected swatch: `WarmCream` border ring + subtle inner dot. Padding: left-aligned, no extra indent. |
| Privacy prose block | Paragraph text floating in layout | Removed. The prose is gone; intent surfaced via subtitles on individual rows. |
| Safe Browsing | Subtitle: full sentence | Subtitle: "Sends URLs to Google" |
| Privacy Receipts | `ClickableRow` with no description | Subtitle: "Verified network capture per release" |
| Cookie policy | Standalone `ClickableRow` | Removed from main list. Surfaced inside Privacy Receipts screen where it belongs. |
| Clear browsing data | Arrow tinted `onBackground 38%` | Arrow tinted `WarmCream.copy(0.55f)` with a warm-red hue (`Color(0xFFB25737).copy(0.55f)`) — the only accent colour on the screen, signalling destructive action without a label. |
| Version | Plain `Row` | `SettingRow` with label "Version", value shows `BuildConfig.VERSION_NAME` in italic serif. No arrow (not navigable). |
| Open source licenses | Label "Open source licenses" | Shortened to "Open source" — value "GitHub ›" |

### GlassCard Component

New private composable replacing all section containers:

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

On API 31+, apply `RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)` via `graphicsLayer { renderEffect = ... }` on the card's backing layer for real frosted glass. On API 26–30, the solid `WarmCream.copy(0.065f)` background provides adequate legibility without blur.

### SettingRow Component

Replaces `ClickableRow` and all ad-hoc `Row` items:

```kotlin
@Composable
private fun SettingRow(
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,          // shown in InstrumentSerif italic
    showArrow: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) { ... }
```

---

## Files to Touch

| File | Changes |
|---|---|
| `StartScreen.kt` | Gesture detection, pill offset animation, blur layer, greeting entrance, nav press feedback |
| `AddressField.kt` | New `GestureFocused` state, scrim fade fix, AnimatedContent spring tune |
| `HomePager.kt` | Pager spring + parallax |
| `UlivetoNavGraph.kt` | All transition specs |
| `TabsScreen.kt` | Stagger + close animation |
| `BookmarksScreen.kt` | Stagger + remove animation |
| `SettingsScreen.kt` | Full redesign: gradient bg, glass cards, type-only rows, `GlassCard` + `SettingRow` components |
| `BrowserScreen.kt` | Chrome scroll-hide easing (if applicable) |

---

## Out of Scope

- Existing pull-to-refresh on `BrowserScreen` (M13) — review only, no changes unless jank found
- New browser features or settings items
- Haptics on any screen other than the drag-to-search gesture
