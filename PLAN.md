# Uliveto — Privacy-First, Design-Led Android Browser

## Context

We are building a brand-new Android browser, working name **Uliveto**, on the two
non-negotiable pillars set in the source brief: **provable zero telemetry** and
**considered, design-led UI**. The project starts greenfield at
`/Users/Edoardo/Documents/personal_projects/uliveto/` (currently empty). The
sibling `air_browser` is intentionally **not** reused — its raw-GeckoView +
Hilt/MVI shape conflicts with the chosen Mozilla Android Components + manual-DI
direction, and inheriting it would slow rather than speed the build. We will
borrow ideas from it (its `air-privacy` fingerprinting WebExtension is a good
reference) but not code.

Decisions locked in before planning:

| Topic | Decision | Why |
| --- | --- | --- |
| Starting point | Greenfield in `uliveto/` | Cleanest mapping of the brief; no legacy to fight |
| Engine | **GeckoView stable** (`org.mozilla.geckoview:geckoview`) | Best privacy posture; IronFox tracks stable; predictable for the CI proof |
| AC modules | Selected subset; **never `service-glean`** | Keep Glean off the classpath as the cleanest telemetry gate |
| Architecture | Manual DI + Compose ViewModels + Repositories | Minimal ceremony; readable; right-sized for a single dev |
| CI proof host | GitHub Actions + Android emulator + `mitmdump` | Public, auditable receipts |
| Plan-file output | Milestones with DoD; not step-by-step code | The user signaled milestone-level autonomous execution in the brief |

Outcome we want: a buildable APK/AAB that boots a real Compose-shell browser on
Gecko, with a green CI job that publishes a network-capture artifact proving
zero outbound connections during cold start / idle / pre-navigation, and a UI
that visibly matches the brief across the start page, hourglass loaded state,
expanded search, overflow menu, and settings — in all three themes.

---

## Tech stack (decided)

- **Language:** Kotlin 2.x
- **UI:** Jetpack Compose + Material 3 (as a11y/dynamic-color scaffold; heavily restyled)
- **Build:** Gradle Kotlin DSL, AGP latest stable, version catalog (`gradle/libs.versions.toml`)
- **`minSdk` 26**, target latest stable
- **Engine:** `org.mozilla.geckoview:geckoview` (stable channel)
- **AC modules** (versions aligned to the Gecko stable train):
  - `concept-engine`, `browser-engine-gecko`
  - `browser-state`, `browser-session-storage`
  - `feature-session`, `feature-tabs`, `feature-search`
  - `feature-findinpage`, `feature-readerview`, `feature-downloads`
  - **Excluded on purpose:** `service-glean` and anything transitively pulling Nimbus/Sentry/Firebase
- **Persistence:** Room (tabs/bookmarks/history) + DataStore (preferences). Plain
  Room — Android FDE covers "encrypted at rest where practical." Note this in
  Settings → About.
- **No DI framework.** App-level `AppContainer` holds singletons (engine, store,
  repositories); ViewModels are created with a `ViewModelProvider.Factory` that
  pulls from the container.
- **Repos** in root `build.gradle.kts`: `google()`, `mavenCentral()`,
  `maven { url = uri("https://maven.mozilla.org/maven2/") }`.
- **Packaging:** AAB for store; `splits { abi {} }` for sideload
  (`arm64-v8a`, `armeabi-v7a`); `android:extractNativeLibs="false"`.
- **Package id:** `it.uliveto.browser`. App label: **Uliveto**.

---

## Module / package layout

Single-module app to start. Internal packages under `it.uliveto.browser`:

```
app/
  src/main/
    AndroidManifest.xml
    assets/
      prefs/phoenix.android.js          # vendored from celenity/Phoenix (OFL/MPL note in NOTICE)
      extensions/uliveto-privacy/       # optional: fingerprinting noise (post-MVP)
    res/font/
      hanken_grotesk_regular.ttf
      hanken_grotesk_medium.ttf
      instrument_serif_regular.ttf
      instrument_serif_italic.ttf
      OFL_hanken.txt
      OFL_instrument.txt
    kotlin/it/uliveto/browser/
      UlivetoApp.kt                     # Application; builds AppContainer
      di/AppContainer.kt                # singletons: engine, store, dataStore, repos
      di/ViewModelFactory.kt
      engine/
        EngineBuilder.kt                # GeckoRuntimeSettings.Builder, hardened
        PrefsLoader.kt                  # parse phoenix.android.js → Map<String, Any>
        PrefsApplier.kt                 # setPrefs() on the runtime
        SafeBrowsingToggle.kt           # exposes the one knob users can flip back on
      data/
        prefs/UserPreferences.kt        # DataStore schema: theme, navStyle, engine, sbOn
        prefs/UserPrefsRepository.kt
        tabs/TabRepository.kt           # wraps AC BrowserStore
        bookmarks/BookmarksDao.kt + Repository + Room entity
        history/HistoryDao.kt + Repository + Room entity
      domain/
        SearchEngine.kt                 # sealed list: DuckDuckGo (default), Startpage, Brave, Mojeek
        UrlClassifier.kt                # "is URL or query?" + engine query builder
      ui/
        UlivetoTheme.kt                 # ColorScheme x 3 (Terracotta/Dark/OLED), MaterialTheme wrap
        tokens/Colors.kt                # raw palette + warm cream + charcoal
        tokens/Type.kt                  # Hanken + Instrument FontFamily
        tokens/Shapes.kt                # HourglassShape, PillShape, CircleShape ext
        tokens/Glass.kt                 # GlassMaterial enum {Decorative, Functional}; modifier + tint math
        nav/UlivetoNavGraph.kt          # start, browser, tabs, bookmarks, settings
        nav/NavStyle.kt                 # Hourglass (default) | Classic
        components/
          GlassSurface.kt               # Modifier-style blur+clip+overlay+highlight+shadow
          HourglassNav.kt               # the continuous frosted hourglass + dots bubble
          ClassicTopBar.kt              # top URL field for NavStyle.Classic
          ClassicBottomBar.kt
          AddressField.kt               # shared input model; collapsed pill, expanded full
          OverflowMenuSheet.kt
          EngineLine.kt                 # "Searching with {engine}" with mixed type
        screens/
          start/StartScreen.kt + StartViewModel.kt
          browser/BrowserScreen.kt + BrowserViewModel.kt
          tabs/TabsScreen.kt + TabsViewModel.kt
          bookmarks/BookmarksScreen.kt + BookmarksViewModel.kt
          settings/SettingsScreen.kt + SettingsViewModel.kt
          settings/PrivacyReceiptsScreen.kt
    androidTest/kotlin/it/uliveto/browser/
      ColdStartNoNetworkTest.kt         # runs against the emulator; reads mitmproxy capture
      PhoenixPrefsAppliedTest.kt        # asserts each pref key from the bundle is set
      EngineLineNavigationTest.kt
    test/kotlin/it/uliveto/browser/
      PrefsLoaderTest.kt
      UrlClassifierTest.kt
      GlassTintMathTest.kt
.github/workflows/
  no-telemetry.yml                      # the pillar-one CI proof
  build.yml                             # assembleDebug + unit tests
PLAN.md                                 # in-repo, kept current
NOTICE.md                               # font OFL attributions; Phoenix MPL note
README.md
```

The package layout is the contract. Once it's in place, milestone work fits
inside it without restructuring.

---

## Milestones

Each milestone must **build, run on an emulator, and be independently
verifiable**. Commit per milestone with `feat(M{n}): …`. Do not block waiting
for review between milestones — proceed.

### M1 — Skeleton + engine + tokens

**Goal:** Compose app boots, Gecko renders a page, design tokens exist.

**Build:**
- Initialize Gradle project (Kotlin DSL, version catalog, manifest, app icon
  placeholder). Configure repos and the three split ABIs.
- Add GeckoView stable + the listed AC modules. Lock versions in
  `libs.versions.toml`.
- `UlivetoApp` → builds `AppContainer` lazily.
- `EngineBuilder` constructs `GeckoRuntimeSettings.Builder` with hardened knobs
  *for now*: `crashHandler(null)`, `experimentDelegate(null)`,
  `globalPrivacyControlEnabled(true)`, `remoteDebuggingEnabled(false)`,
  `aboutConfigEnabled(false)`. No prefs file yet — that's M2.
- `BrowserScreen` shows an `AndroidView` of a `GeckoView` bound to a single
  `GeckoSession`, plus a temporary debug URL bar. Load `about:blank` on start.
- `UlivetoTheme` + tokens: bundle the four font files, define `Colors`,
  `Type`, `Shapes`, `Glass` (the implementation can stub blur with a flat
  overlay — real blur lands in M4).

**DoD:** App installs on emulator; opens; can type a URL into the debug bar and
load it via Gecko; light theme reads as terracotta with the right cream text
color; both font families render somewhere on screen.

**Commit:** `feat(M1): app skeleton, GeckoView wired, design tokens bundled`.

---

### M2 — Telemetry lockdown + CI proof (highest priority)

This is pillar one. It is gating — nothing visual ships before the CI is green.

**Build:**
- **Vendor** `phoenix.android.js` from
  `https://codeberg.org/celenity/Phoenix` (`android/phoenix.js`) into
  `assets/prefs/`. Add a `NOTICE.md` entry crediting Phoenix + license.
- **`PrefsLoader`**: read the asset, regex-parse `pref("key", value);`
  statements (booleans, ints, quoted strings, blank string), return
  `Map<String, Any>`. Reject unrecognized literals with a hard error.
- **`PrefsApplier`**: at runtime startup, call
  `runtime.getSettings().setPrefs(map)`. Wire this in `EngineBuilder` before
  the runtime is first handed out.
- **Builder hardening (final):** in addition to M1's knobs, ensure no Glean
  delegate (`GeckoGleanAdapter`) is constructed anywhere in the codebase.
  Confirm with a unit test that greps the compiled R8 mapping or, more
  reliably, an `androidTest` that asserts `Class.forName(...)` for the
  delegate symbols is NOT loaded post-runtime-init.
- **Safe Browsing default off.** Expose a single `SafeBrowsingToggle` API the
  Settings screen will call later. Default state: false. When false, also
  blank the `browser.safebrowsing.provider.google4.*` URLs at runtime.
- **CI workflow (`.github/workflows/no-telemetry.yml`):**
  1. Ubuntu runner, KVM enabled, `reactivecircus/android-emulator-runner@v2`,
     API 33 / `x86_64` / `google_apis_playstore=false`.
  2. Install `mitmproxy`; start `mitmdump -w capture.flow --listen-port 8080`
     in the background; configure emulator HTTP proxy to point at it; install
     mitm CA via `adb push` + `settings put`.
  3. Build debug APK, install, launch the main activity with an intent that
     **does not** include a URL.
  4. Wait 30 s idle (cold start window). Do not navigate. Do not interact.
  5. Stop `mitmdump`. Assert with a small Python helper
     (`scripts/assert-no-flows.py`) that the capture contains **zero** flows.
     Exit non-zero if any flow exists; print the offending host(s).
  6. **Always** upload `capture.flow` and a human-readable
     `capture-summary.txt` (`mitmdump --no-server -nr capture.flow -s
     dump_summary.py`) as artifacts. Green or red, the receipts publish.
- **`PhoenixPrefsAppliedTest` (`androidTest`)**: iterates the parsed pref map
  and asserts each key reads back equal via the runtime's pref read API.
  Catches drift if Mozilla renames a key under us.

**DoD:** `no-telemetry.yml` is green on a fresh PR. `capture-summary.txt`
artifact is downloadable and shows "0 flows." `PhoenixPrefsAppliedTest`
passes. `PLAN.md` in the repo lists every pref category we set with a
one-line justification.

**Commit:** `feat(M2): telemetry lockdown — Phoenix prefs, no Glean, CI proof green`.

---

### M3 — Start page

**Build:**
- `StartScreen` composable: full-bleed terracotta backdrop using a radial
  `Brush.radialGradient(colors = listOf(0xFFB25737, 0xFF9D4626, 0xFF7E3415))`
  + a 4%-opacity grain `Modifier.drawWithCache` (use a tiled noise PNG bundled
  as a drawable, drawn at low alpha; no procedural noise in shader).
- `EngineLine` composable: `Row` mixing Hanken regular ("Searching with ") and
  Instrument Serif italic (engine name). Tap → opens a name-only
  `BottomSheetDialog` with the four engines listed top-to-bottom. Selecting
  one writes through `UserPrefsRepository.setSearchEngine`.
- "Welcome, {name}" using Instrument Serif. Name source: a single-line
  prompt the very first time the start page opens (DataStore-backed); editable
  in Settings. No login, no account.
- Frosted decorative search pill (the `AddressField` in its collapsed,
  decorative-glass form). Pressing it transitions to the expanded state from
  M5 — for now, stub the expansion to a fullscreen `TextField` and submit to
  `BrowserScreen` via nav.
- Bottom frosted decorative pill bar with three labeled icons:
  **Tabs · Bookmarks · Settings** (Hanken). Active item bolder + brighter.
- Enforce "only show what's possible": no back/forward anywhere on this
  screen.
- Defaults: search engine = DuckDuckGo. Confirm DuckDuckGo's mobile endpoint
  does not require enabling search suggestions (suggestions stay off by
  default).

**DoD:** Visually matches §5.1 of the brief in light theme. Typing a query in
the search pill (or its expanded form) navigates to the engine's results page.
Engine picker round-trips through DataStore and updates `EngineLine` live.

**Commit:** `feat(M3): start page — terracotta, greeting, search pill, engine line`.

---

### M4 — HourglassNav (loaded-page navigation)

This is the hardest UI work. Treat it carefully.

**Build:**
- **`HourglassShape`** in `tokens/Shapes.kt`: a `Shape` whose `createOutline`
  builds a single `Path` with two concave waists. Geometry from the brief
  (dp): left circle Ø44, 30dp waist, center 44dp tall × ~140dp wide, 30dp
  waist, right circle Ø44. Waists are 12dp pinched (concave). End caps are
  the **same height** as the center — no bubbling. Implementation: start at
  the top of the left arc, arc round to its right side, cubic-bezier inward
  and back out to the top-left of the center, line across, mirror on the
  right side, close. Unit-test the shape in `GlassTintMathTest`-adjacent
  `ShapeGeometryTest` (height monotonic, total width predictable, the
  derived hit-test rect for back/forward extends into each waist).
- **`GlassSurface(material = Functional)` modifier**: `Modifier.shadow(…)` →
  `Modifier.clip(shape)` → `Modifier.blur(22.dp, BlurredEdgeTreatment.Unbounded)`
  applied via a child that draws the backdrop into an offscreen layer
  (`Modifier.graphicsLayer` with `compositingStrategy = Offscreen`) → over it
  draw a fill `Color(0xFFF7F7F9).copy(alpha = 0.42f)` with saturation 1.5 and
  brightness +6%, then an inner 1px top rim
  `Color.White.copy(alpha = 0.65f)`. The blur is **clipped to the shape**,
  never a full rectangle.
- **`HourglassNav`**: composes Back, Address (center, displays
  `lock + currentDomain`), Forward inside the hourglass. Charcoal `#2C2C2E`
  glyphs and text (the legibility rule). Disabled forward = 26% opacity but
  the shape stays symmetric. Invisible hit areas: wrap the back/forward icons
  in 48dp `Box` that extends a few dp into the waist.
- **Detached three-dot bubble**: separate 44dp `GlassSurface(material =
  Functional, shape = CircleShape)`, placed with `Spacer(11.dp)` to the
  right. Opens `OverflowMenuSheet`.
- **Scroll-to-hide chrome**: a `NestedScrollConnection` watches the Gecko
  page scroll; chrome `translateY` 120dp + alpha 0 over 320ms with
  `FastOutSlowInEasing` on down, restores on any up scroll. Pause the blur
  rendering while hidden (toggle `compositingStrategy` back to `Auto`) to
  save battery.
- Tapping the center section triggers the expanded address state — for now,
  navigate to a placeholder full-screen `AddressField(expanded = true)`. The
  full morph animation lands in M5.

**DoD:** On any loaded page, the hourglass renders as a single shape with
neutral cloudy frost and a real blur of the page behind it; back/forward have
≥48dp hit targets; forward is dimmed before any forward history exists; the
bar slides away on scroll-down and back on scroll-up; the dots bubble opens an
empty sheet placeholder.

**Commit:** `feat(M4): hourglass navigation — single shape, functional glass, scroll-hide`.

---

### M5 — Expanded address / search state

**Build:**
- Promote `AddressField` to a single composable used both on the start page
  and in the hourglass center, parameterized by `state ∈ {Pill, HourglassCenter,
  Expanded}`.
- Transition: on tap, animate the center-section bounds from the hourglass
  position into a full-width frosted field near the top of the safe area,
  morphing with `animateBounds()` (~280ms). Behind it, draw a soft scrim
  `Color.Black.copy(alpha = 0.18f)` + a low-intensity blur on the page —
  "non-threatening focus."
- Keyboard rises automatically via `LocalSoftwareKeyboardController`.
- Submit: `UrlClassifier.classify(input)` → either navigate directly (URL) or
  build the engine's query URL (search). Dismiss morphs back.

**DoD:** Tapping the address from the hourglass smoothly enlarges into the
field; back gesture / tap-outside collapses it; submitting a URL navigates;
submitting free text routes to the chosen engine.

**Commit:** `feat(M5): expanded address state — shared input model with morph`.

---

### M6 — Overflow menu + Reader (contextual) + bookmarks/tabs

**Build:**
- **`OverflowMenuSheet`**: frosted sheet (functional glass), 2×3 grid of
  icon + Hanken-label items: New tab, Bookmarks, Share, Reader, Find,
  Desktop site. Rise+fade 250ms. Auto-dismiss on scroll.
- **Reader**: bind `ReaderViewFeature` (from
  `mozilla.components:feature-readerview`); its
  `onReaderViewAvailableChange: (Boolean) -> Unit` flips a state flag.
  Render the Reader entry as **disabled** when false. When tapped (and
  available), call `showReaderView()`.
- **Bookmarks**: Room schema + `BookmarksRepository`; `BookmarksScreen`
  showing list with simple swipe-to-delete and tap-to-open. Add-bookmark
  action lives on the overflow menu's primary section (long-press the page
  in the dots → add bookmark). Two taps from a loaded page max.
- **Tabs**: AC `BrowserStore` already holds session state. `TabsScreen`
  renders a 2-column grid of frosted cards (decorative glass on terracotta
  for the start-style backdrop; functional on a regular page). New tab
  button is a + tile at the end.
- **Find in page**: `FindInPageFeature` wired to a thin in-line bar that
  appears below the hourglass when triggered from overflow.
- **Share / Desktop site**: standard `Intent.ACTION_SEND` and a per-session
  toggle of `GeckoSessionSettings.userAgentMode = DESKTOP`.

**DoD:** Reader entry visibly disabled on non-article pages, enabled and
working on articles. Bookmarks persist across cold restart. Tabs survive
restoration via `browser-session-storage`. All overflow flows reachable in
≤3 taps from anywhere.

**Commit:** `feat(M6): overflow menu, reader, bookmarks, tabs`.

---

### M7 — Settings + Classic NavStyle + theming

**Build:**
- **`SettingsScreen`**: simple `LazyColumn` of rows with hairline dividers,
  generous spacing, **no cards**. Section headers in Instrument Serif; rows
  in Hanken. Sections per §5.5:
  - **Search** — default engine (reuses the §5.1 picker).
  - **Navigation** — `NavStyle: Hourglass (default) | Classic` segmented
    control. Writes through `UserPrefsRepository`.
  - **Appearance** — `Theme: Light | Dark | OLED black | Follow system`.
    Wire `UlivetoTheme` to read this. OLED uses pure `#000000` for surfaces
    and base; functional glass shifts to a slightly darker neutral
    (`Color(0xFF1F1F22).copy(alpha = 0.48f)`, charcoal swapped for warm
    light `#E9E9EC`) to maintain AA contrast.
  - **Privacy** — short statement: "Uliveto collects no telemetry. The CI
    job that proves this publishes a network capture for every release."
    Below: Safe Browsing toggle (default off, with the explainer copy);
    cookie policy radio; "Clear browsing data" action;
    `PrivacyReceiptsScreen` link summarizing zero analytics connections
    (renders the bundled `capture-summary.txt` from the latest release).
  - **About** — version, licenses (incl. font OFL text), source link.
- **Classic NavStyle**: a second top-of-screen URL bar + bottom 5-icon strip
  (back / forward / home / tabs / menu). Built on AC's Compose
  `BrowserToolbar` for time-to-correctness. Reuses the same
  `BrowserViewModel`. Top-level scaffold reads `navStyle` and swaps
  `HourglassNav` ↔ `ClassicTopBar`/`ClassicBottomBar`.
- **Theming**: ensure every functional surface meets WCAG AA in all three
  themes. The greeting and engine line are explicitly allowed below AA — they
  are decorative.

**DoD:** Three themes all readable; switching `NavStyle` swaps the shell
without re-creating sessions; settings persist; the privacy receipts page
displays.

**Commit:** `feat(M7): settings, classic navstyle, theming with OLED`.

---

### M8 — Polish pass

**Build:**
- Motion tuning per §4.5; ensure springs aren't toy-bouncy.
- A11y audit: TalkBack labels on every actionable element; AA contrast check
  on functional text/icons in all three themes (add a screenshot test that
  computes contrast for known surfaces).
- Performance: profile blur cost on a low-end emulator config (API 26,
  no-acceleration). Verify the blur is clipped to the shape (not a
  full-screen rect) and that the offscreen composition layer is released
  while chrome is scroll-hidden.
- Adaptive tint: sample page `theme-color` (via `browser-state`'s
  `ContentState.metaThemeColor`) once per page change; cache and recompute
  only on change; apply via `Glass.functionalTintFor(backdrop)` with the
  formulas in §4.4.

**DoD:** Both pillars demonstrably met. The latest CI run is green and its
`capture.flow` artifact is downloadable. The UI matches the brief across
start, hourglass loaded, expanded search, overflow, and settings, in Light,
Dark, and OLED.

**Commit:** `feat(M8): polish — motion, a11y, perf, adaptive tint`.

---

## Pillar-one telemetry lockdown — specifics

Sources to vendor at M2:

- `assets/prefs/phoenix.android.js` ← `https://codeberg.org/celenity/Phoenix`
  → `android/phoenix.js` (license text into `NOTICE.md`).

Categories the bundle must cover (each verified by
`PhoenixPrefsAppliedTest`):

- Glean / telemetry (`toolkit.telemetry.*`, `datareporting.*`).
- Normandy + experiments (`app.normandy.*`, `nimbus.global-opt-out=true`,
  `messaging-system.rsexperimentloader.enabled=false`,
  `app.shield.optoutstudies.enabled=false`).
- Pocket / sponsored (`extensions.pocket.*`,
  `browser.newtabpage.activity-stream.*`).
- Captive portal / connectivity probes
  (`network.captive-portal-service.enabled=false`,
  `captivedetect.canonicalURL` blank,
  `network.connectivity-service.enabled=false`).
- Safe Browsing endpoints (`browser.safebrowsing.*`,
  `browser.safebrowsing.provider.google4.*` blank).
- Crash reporter (`breakpad.reportURL` blank,
  `browser.tabs.crashReporting.sendReport=false`,
  `browser.crashReports.unsubmittedCheck.autoSubmit2=false`).
- First run / default-browser (`browser.aboutwelcome.enabled=false`,
  `browser.shell.checkDefaultBrowser=false`).
- Push & WebPush (`dom.push.*=false`, server URL blank,
  `dom.webnotifications.enabled=false`).
- Geolocation + region network providers (`geo.provider.network.url` blank,
  `browser.region.network.url` blank, `browser.region.update.enabled=false`).
- Misc beacons / prefetch (`beacon.enabled=false`, `network.prefetch-next=false`,
  `network.predictor.enabled=false`,
  `network.http.speculative-parallel-limit=0`).
- Updates (`app.update.enabled=false`).

`EngineBuilder` builder-level calls:

- `crashHandler(null)`
- `experimentDelegate(null)`
- `globalPrivacyControlEnabled(true)`
- `remoteDebuggingEnabled(false)`
- `aboutConfigEnabled(false)`
- No `telemetryDelegate(...)`. No reference to `GeckoGleanAdapter`. No
  dependency on `service-glean`. Verify via `./gradlew app:dependencies | grep
  glean` returning empty in CI.

Process env: set `MOZ_CRASHREPORTER_DISABLE=1` before runtime startup.

CI proof (`no-telemetry.yml`) is the receipt. Without a green run, M2 is not
done.

---

## Design system — implementation notes

- **Fonts** bundled into `res/font/`. Build `FontFamily`s in
  `tokens/Type.kt`. OFL texts ship in `res/raw/` and are surfaced in Settings
  → About.
- **Two glass materials** are distinct `enum class GlassMaterial` values;
  `Modifier.ulivetoGlass(material, shape, backdropColor)` is the single entry
  point. Decorative glass uses lower opacity, no saturation lift, soft
  brand-tinted edge highlight. Functional glass uses the milky fill +
  saturation/brightness lift + white rim + shadow.
- **Adaptive tint** lives in `tokens/Glass.kt` as pure color math
  (`backdrop.lighten(0.08f).desaturate(0.06f).copy(alpha = 0.42f)`). Unit-test
  this in `GlassTintMathTest`. The sampling happens once per page in M8 via
  the page's meta `theme-color`; otherwise fall back to a derived backdrop
  average computed on a `Bitmap.createScaledBitmap(snapshot, 8, 8, true)`.
  **Never per frame.**
- **Hourglass geometry** is one `Path`. Resist the urge to compose it from
  overlapping shapes — overlap doubles alpha and shows seams.
- **Charcoal-on-functional** is the legibility rule. Apply it via theme
  tokens (`onFunctionalGlass = Color(0xFF2C2C2E)`), not local overrides.

---

## Verification

End-to-end manual checks (run on emulator API 33 unless stated):

1. **Cold-start no-network (automated, CI-only):** the
   `no-telemetry.yml` workflow above. Local repro: `make verify-no-telemetry`
   wraps the same steps (`mitmdump` + `adb` + assertion).
2. **Pref application:** `./gradlew connectedDebugAndroidTest --tests
   '*PhoenixPrefsApplied*'` — pass.
3. **Engine smoke:** open the app, type a URL into the start search pill,
   navigate, confirm the hourglass appears and back/forward behave per
   §5.2.
4. **Theming:** rotate Light → Dark → OLED in Settings → Appearance; confirm
   functional glass remains AA-readable on all three.
5. **Classic mode:** Settings → Navigation → Classic; confirm the shell
   swaps without re-creating sessions; switch back.
6. **Reader contextual:** open a known article (e.g. a Wikipedia page);
   Reader entry in overflow is enabled. Open the home page of a JS-heavy
   web app; Reader entry is disabled.
7. **Persistence:** open three tabs + bookmark a page; force-stop and
   reopen; tabs restore; bookmark is present.
8. **3-tap budget:** from any screen, any feature reachable in ≤3 taps.
9. **APK size sanity:** `./gradlew :app:bundleRelease` produces an AAB whose
   per-ABI splits fall in the expected 50–60 MB ranges.

---

## Risks & notes

- **GeckoView API surface shifts release-to-release.** Pin Gecko + AC
  versions in `libs.versions.toml`; bump deliberately. `PhoenixPrefsAppliedTest`
  is the canary for pref renames.
- **Phoenix is upstream code we don't own.** Vendor it; never fetch at runtime.
  Note its license in `NOTICE.md`. Re-vendor on each Gecko bump.
- **Blur cost on low-end devices** is the only real perf concern. Mitigations:
  clip to shape (mandatory), release the offscreen layer while scroll-hidden,
  cache the adaptive tint per page.
- **Reader uses Mozilla's bundled extension.** It ships inside AC; no
  external download. Verify in M2's no-network test that loading the extension
  does not trigger any network call.
- **Safe Browsing default-off** is a deliberate privacy trade. The Settings
  copy must explain this — don't soften it.
- **No accounts, no sync, no cloud — period.** If any future PR introduces a
  network call that isn't a user-initiated navigation or the chosen engine's
  query, the CI proof fails. That is the design.
