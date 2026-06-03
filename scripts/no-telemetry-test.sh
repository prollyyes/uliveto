#!/usr/bin/env bash
set -euo pipefail

# Prove zero outbound connections during 30-second cold start.
#
# Strategy: route all emulator traffic through mitmproxy. Any connection
# attempt (even a failed TLS handshake) is recorded as a flow. If telemetry
# is truly disabled the capture will be empty. No cert installation needed —
# we are detecting connection *attempts*, not inspecting content.

# ── 1. Start mitmproxy capture ────────────────────────────────────────────────
mkdir -p captures
mitmdump -w captures/capture.flow --listen-port 8080 &
MITM_PID=$!
sleep 5  # wait for mitmproxy to be ready

# ── 2. Route emulator traffic through mitmproxy on the host ──────────────────
# 10.0.2.2 is the host loopback address as seen from inside the emulator.
adb shell settings put global http_proxy 10.0.2.2:8080

# ── 3. Install APK and launch with NO URL ────────────────────────────────────
adb install app/build/outputs/apk/debug/app-x86_64-debug.apk
adb shell am start -n it.uliveto.browser/.MainActivity

# ── 4. Cold-start idle window ─────────────────────────────────────────────────
sleep 30

# ── 5. Stop capture and assert zero flows ─────────────────────────────────────
kill "$MITM_PID" || true
sleep 2
python3 scripts/assert-no-flows.py captures/capture.flow
