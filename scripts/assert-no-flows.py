#!/usr/bin/env python3
"""Assert that a mitmproxy capture contains zero flows of any kind.

Counts all flow types (HTTP, TCP, errored TLS attempts) so that even a
failed CONNECT handshake — which proves a connection was attempted — is
flagged. If telemetry is truly disabled the capture will be empty.

Usage: python3 assert-no-flows.py <capture.flow>
Exit 0 if zero flows, exit 1 with offending hosts listed otherwise.
"""
import sys
from mitmproxy import io as mio
from mitmproxy.http import HTTPFlow


def main():
    if len(sys.argv) != 2:
        print("Usage: assert-no-flows.py <capture.flow>", file=sys.stderr)
        sys.exit(2)

    path = sys.argv[1]
    flows = []
    try:
        with open(path, "rb") as fh:
            for flow in mio.FlowReader(fh).stream():
                flows.append(flow)  # count every flow type
    except FileNotFoundError:
        print(f"Capture file not found: {path}", file=sys.stderr)
        sys.exit(1)

    if not flows:
        print("✅  Zero flows captured — telemetry proof passed.")
        sys.exit(0)
    else:
        print(f"❌  {len(flows)} flow(s) captured — telemetry proof FAILED.")
        for flow in flows:
            if isinstance(flow, HTTPFlow):
                print(f"  HTTP  {flow.request.method} {flow.request.pretty_url}")
            else:
                print(f"  {type(flow).__name__}  {flow}")
        sys.exit(1)


if __name__ == "__main__":
    main()
