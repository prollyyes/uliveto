#!/usr/bin/env bash
set -euo pipefail

# Run by the no-telemetry CI workflow via android-emulator-runner.
# Must be executed from the repository root.

# ── 1. Start mitmproxy capture ────────────────────────────────────────────────
mkdir -p captures
mitmdump -w captures/capture.flow --listen-port 8080 &
MITM_PID=$!

# Wait for mitmproxy to initialise and write its CA cert to disk
sleep 5

# ── 2. Get the CA cert from disk (do NOT curl localhost:8080/cert/pem) ────────
# mitmproxy writes the cert to ~/.mitmproxy/ on startup.
# Curling 127.0.0.1:8080 sends a request *to* the proxy port, which mitmproxy
# cannot forward to itself — it kills the connection and loops until exit 2.
cp ~/.mitmproxy/mitmproxy-ca-cert.pem /tmp/mitm.pem
if [ ! -s /tmp/mitm.pem ]; then
  echo "ERROR: mitmproxy CA cert not found at ~/.mitmproxy/mitmproxy-ca-cert.pem" >&2
  exit 1
fi

# ── 3. Install the CA cert into the emulator system trust store ───────────────
CERT_HASH=$(openssl x509 -inform PEM -subject_hash_old -in /tmp/mitm.pem | head -1)
adb root
adb wait-for-device
adb remount
adb push /tmp/mitm.pem /system/etc/security/cacerts/"${CERT_HASH}".0
adb shell chmod 644 /system/etc/security/cacerts/"${CERT_HASH}".0

# ── 4. Reboot so the new cert is loaded into the trust store ──────────────────
adb reboot
adb wait-for-device

# Poll for full boot completion (boot_completed == 1)
for _ in $(seq 1 30); do
  if adb shell getprop sys.boot_completed 2>/dev/null | grep -q '^1$'; then
    break
  fi
  sleep 3
done
sleep 5

# ── 5. Set proxy AFTER reboot (setting before reboot can be lost) ─────────────
adb shell settings put global http_proxy 10.0.2.2:8080

# ── 6. Install APK and launch ─────────────────────────────────────────────────
adb install app/build/outputs/apk/debug/app-x86_64-debug.apk

# Launch with NO URL intent — cold start only, do not navigate
adb shell am start -n it.uliveto.browser/.MainActivity

# ── 7. Wait 30 s idle (cold start window) ────────────────────────────────────
sleep 30

# ── 8. Stop capture and assert zero flows ─────────────────────────────────────
kill "$MITM_PID" || true
sleep 2

python3 scripts/assert-no-flows.py captures/capture.flow
