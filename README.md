<div align="center">

<img src="images/logo.svg" width="160" alt="Uliveto logo — an olive branch" />

<br>

# Uliveto

### *The web, as it was always meant to feel.*

**A privacy-first, design-led Android browser. Zero telemetry. Mediterranean spirit.**

<br>

[![No Telemetry — CI verified](https://img.shields.io/badge/telemetry-zero%2C%20CI%20verified-9D4626?style=flat-square)](https://github.com/prollyyes/uliveto/actions)&nbsp;&nbsp;[![Build](https://img.shields.io/badge/build-passing-6B7C45?style=flat-square&logo=github-actions&logoColor=white)](https://github.com/prollyyes/uliveto/actions)&nbsp;&nbsp;![Platform](https://img.shields.io/badge/android-API%2026%2B-3DDC84?style=flat-square&logo=android&logoColor=white)

</div>

---

<div align="center">

*Somewhere between the olive groves and the open sea, the internet feels honest again.*

</div>

---

## Why Uliveto

Most browsers are built to watch you back.

They track where you go, sell that data to someone, and dress the whole arrangement up as a feature. You get "personalization." They get your attention, your habits, your private searches.

Uliveto doesn't do any of that.

It is a browser the way an olive tree is a tree — slow-grown, honest, built for the long run. Named after the Italian word for *olive grove*, it carries the sensibility of a Mediterranean afternoon: unhurried, clear-eyed, nothing to hide.

It exists for three simple reasons: to respect your privacy, to be gentle on your eyes, and to make browsing feel like something you chose, not something you consented to.

---

## Two Pillars

<table>
<tr>
<td width="50%" valign="top">

### Zero Telemetry

Your browsing is yours. Every cold start is verified by CI. The network capture is public, downloadable, and pinned to every commit. If the app ever tries to phone home — a tracking ping, a crash report, a telemetry handshake — the build fails automatically.

No analytics. No reporting. No exceptions. The guarantee isn't a promise in a privacy policy; it's a failing build.

</td>
<td width="50%" valign="top">

### Mediterranean Design

The Hourglass navigation, terracotta palette, and frosted glass surfaces aren't themes you switch on. They are the browser — designed as a whole, from the ground up, the way a good meal is made. Warm tones that don't tire your eyes. Glass that moves with the light. Chrome that steps aside when you're reading and returns when you need it.

The screen is your window. We tried to make it a nice one.

</td>
</tr>
</table>

---

## For Your Eyes

Most apps treat your screen like a billboard. Bright whites, high contrast, constant motion.

Uliveto is built around a different idea: your eyes deserve rest. The terracotta tones are warm — closer to paper than to fluorescent light. The OLED Black mode drops surfaces to true black, letting the display switch pixels off entirely. The glass chrome fades away the moment you start scrolling and only returns when you pause.

Looking at the web should feel like looking through a window, not into a lamp.

---

## For Your Privacy

The moment you install most browsers, you've already made a deal. Uliveto refuses that deal on your behalf.

The **Phoenix preference bundle** — a carefully curated set of GeckoView settings — disables fingerprinting, telemetry, crash reporting, and Safe Browsing (which would otherwise call Google's servers on every URL you type). It applies silently, before the first page ever loads. And it hard-fails if anything goes wrong, rather than silently continuing without protection.

Your searches, your history, your bookmarks — they live on your device, in a local Room database. Nothing leaves unless you choose to share it.

---

## For Your Phone

Uliveto runs on any Android phone with Android 8.0 (Oreo) or later. It uses **GeckoView** — Mozilla's rendering engine, the same one that powers Firefox — so you get a real, modern browser, not a WebView wrapper.

---

## Features

| | |
|---|---|
| **Hourglass navigation** | A single floating shape that holds back, forward, and your address — frosted glass, scroll-to-hide |
| **Classic NavStyle** | A minimal top/bottom bar layout for those who prefer something familiar |
| **Privacy by default** | Phoenix prefs, Safe Browsing off, no crash reporter, no telemetry — before the first page loads |
| **Three themes** | Light (warm terracotta), Dark, and OLED Black (true black surfaces for AMOLED screens) |
| **Bookmarks & history** | Local Room database — no cloud sync, no account required |
| **Expanded address field** | Morphing search/URL bar with smooth AnimatedContent transition |
| **Find in page** | Quick text search in the current page |
| **Desktop site toggle** | Switch user agent on demand |
| **Privacy Receipts** | A screen that shows exactly what Uliveto has disabled and why |
| **Engine picker** | DuckDuckGo, Startpage, Brave Search, or Mojeek — no Google by default |

---

## Getting It On Your Phone

There are two ways.

### Option A — Build from source (recommended)

You need Android Studio, JDK 17+, and a USB cable.

```bash
# Clone the repo
git clone git@github.com:prollyyes/uliveto.git
cd uliveto

# Build a debug APK
./gradlew assembleDebug
```

Then plug in your phone, enable **Developer Options → USB Debugging**, and run:

```bash
# Install directly to your connected phone
adb install app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

If `adb` isn't in your path, it's inside `~/Library/Android/sdk/platform-tools/adb` on macOS. You can also use Android Studio's **Run** button to build and install in one step.

### Option B — Download from GitHub Actions

Every commit to `main` produces a signed debug APK as a build artifact.

1. Go to [github.com/prollyyes/uliveto/actions](https://github.com/prollyyes/uliveto/actions)
2. Click the latest passing workflow run
3. Download the APK from the **Artifacts** section at the bottom
4. On your phone, allow installation from unknown sources (**Settings → Apps → Special app access → Install unknown apps**)
5. Open the downloaded APK and install

> For a production-signed release APK ready for direct distribution, a keystore and `./gradlew assembleRelease` is all that's needed — if you'd like that, open an issue.

---

## CI / Privacy Proof

`no-telemetry.yml` runs on every commit. It spins up an Android emulator, proxies all traffic through **mitmproxy**, launches a cold start of the app, waits 30 seconds, and then asserts that **zero outbound flows** were recorded — including errored TLS attempts that never completed.

The capture file is attached as a GitHub Actions artifact on every run. Download it and verify yourself with `mitmdump -r capture.flow`.

The build fails the moment the app phones home. That is the contract.

[View workflow runs →](https://github.com/prollyyes/uliveto/actions)

---

## Build

```bash
git clone git@github.com:prollyyes/uliveto.git
cd uliveto
./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-arm64-v8a-debug.apk
```

**Requirements:** Android SDK 26+, JDK 17+, Kotlin 2.1

---

## Design System

<details>
<summary>Design tokens</summary>

<br>

| Token | Value | Used for |
|---|---|---|
| Terracotta | `#B25737` / `#9D4626` / `#7E3415` | Start page radial gradient |
| WarmCream | `#F5E6D3` | Primary text on terracotta |
| CharcoalDark | `#2C2C2E` | Text on glass surfaces |
| Glass fill | `#F7F7F9` @ 0.42α | Functional glass (nav chrome) |
| Glass fill | `#F7F7F9` @ 0.20α | Decorative glass |
| Blur radius | 22 dp (API 31+) | Real blur via `Modifier.blur` |
| Hanken Grotesk | Variable, OFL 1.1 | All UI text |
| Instrument Serif | OFL 1.1 | Display headings, greeting |
| HourglassShape | Single `Path`, cubic Bézier waist | Navigation bar shape |

</details>

---

## License

Uliveto is open source.

- App code: see repository license
- Hanken Grotesk, Instrument Serif: SIL OFL 1.1
- Phoenix privacy preferences bundle: GPL-3.0-or-later (see `NOTICE.md`)

---

<div align="center">

*Browse like no one is watching. Because no one is.*

</div>
