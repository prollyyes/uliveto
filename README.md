<div align="center">

<svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
  <!-- Background circle -->
  <circle cx="60" cy="60" r="60" fill="#9D4626"/>
  <!-- Trunk -->
  <path d="M57 95 Q55 80 56 70 Q57 60 60 55 Q63 60 64 70 Q65 80 63 95 Z" fill="#6B7C45" opacity="0.85"/>
  <!-- Root spread -->
  <path d="M54 95 Q50 92 46 94" stroke="#6B7C45" stroke-width="2.5" stroke-linecap="round" opacity="0.7"/>
  <path d="M66 95 Q70 92 74 94" stroke="#6B7C45" stroke-width="2.5" stroke-linecap="round" opacity="0.7"/>
  <!-- Main canopy — large central cluster -->
  <ellipse cx="60" cy="44" rx="18" ry="14" fill="#6B7C45"/>
  <!-- Left cluster -->
  <ellipse cx="44" cy="52" rx="13" ry="10" fill="#6B7C45"/>
  <!-- Right cluster -->
  <ellipse cx="76" cy="52" rx="13" ry="10" fill="#6B7C45"/>
  <!-- Upper left cluster -->
  <ellipse cx="48" cy="38" rx="11" ry="9" fill="#7A8E52"/>
  <!-- Upper right cluster -->
  <ellipse cx="72" cy="38" rx="11" ry="9" fill="#7A8E52"/>
  <!-- Top highlight cluster -->
  <ellipse cx="60" cy="32" rx="10" ry="8" fill="#7A8E52"/>
  <!-- Olives -->
  <ellipse cx="50" cy="58" rx="3" ry="4" fill="#3D4F1C"/>
  <ellipse cx="70" cy="56" rx="3" ry="4" fill="#3D4F1C"/>
  <ellipse cx="60" cy="53" rx="2.5" ry="3.5" fill="#3D4F1C"/>
  <!-- Small leaf highlights -->
  <ellipse cx="54" cy="43" rx="4" ry="2.5" fill="#8A9E60" transform="rotate(-20 54 43)"/>
  <ellipse cx="67" cy="41" rx="4" ry="2.5" fill="#8A9E60" transform="rotate(20 67 41)"/>
  <ellipse cx="60" cy="36" rx="3.5" ry="2" fill="#8A9E60"/>
</svg>

<br>

# Uliveto

**A privacy-first, design-led Android browser. Zero telemetry. Mediterranean spirit.**

<br>

![build: passing](https://img.shields.io/badge/build-passing-6B7C45?style=flat-square&logo=github-actions&logoColor=white)&nbsp;&nbsp;![No Telemetry](https://img.shields.io/badge/telemetry-none-9D4626?style=flat-square)

</div>

---

## Two Pillars

<table>
<tr>
<td width="50%" valign="top">

### Zero Telemetry

Every cold start is verified by CI. The network capture is public and downloadable. If the app ever tries to phone home, the build fails.

</td>
<td width="50%" valign="top">

### Mediterranean Design

The Hourglass nav, terracotta palette, and glass surfaces are not themes — they are the browser.

</td>
</tr>
</table>

---

## Features

- **Hourglass navigation** with frosted glass and scroll-hide chrome
- **Full start page** with engine picker: DuckDuckGo, Startpage, Brave, Mojeek
- **Three themes**: Light (terracotta), Dark, OLED Black
- **Classic NavStyle** alternative for minimal chrome
- **Reader mode** for distraction-free reading
- **Bookmarks and history** — Room database, no cloud sync
- **Expanded address field** with morph animation
- **Privacy by default**: Phoenix pref bundle, Safe Browsing off, no crash reporter, zero telemetry

---

## CI / Privacy Proof

`no-telemetry.yml` on GitHub Actions spins up an emulator, runs a cold start behind mitmproxy, and asserts **zero outbound flows**. The capture file is attached as a build artifact — download it and verify yourself.

The build fails the moment the app phones home. That's the guarantee.

[View workflow runs on GitHub Actions](https://github.com/prollyyes/uliveto/actions)

---

## Build

```bash
git clone git@github.com:prollyyes/uliveto.git
cd uliveto
./gradlew assembleDebug
```

**Minimum requirements:** Android SDK 26+, JDK 17+

---

## Design System

<details>
<summary>Key design tokens</summary>

<br>

| Token | Value | Used for |
|---|---|---|
| Terracotta | `#B25737` / `#9D4626` / `#7E3415` | Start page gradient |
| WarmCream | `#F5E6D3` | Primary text on terracotta |
| CharcoalDark | `#2C2C2E` | Text on glass surfaces |
| Hanken Grotesk | OFL 1.1 | UI text |
| Instrument Serif | OFL 1.1 | Display / greeting |

</details>

---

## License

Uliveto is open source. Fonts (Hanken Grotesk, Instrument Serif) are under SIL OFL 1.1. Privacy preferences bundle (Phoenix) is GPL-3.0-or-later. See `NOTICE.md`.
