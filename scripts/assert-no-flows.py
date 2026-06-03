#!/usr/bin/env python3
"""Assert that a mitmproxy capture file contains zero HTTP flows.

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
                if isinstance(flow, HTTPFlow):
                    flows.append(flow)
    except FileNotFoundError:
        print(f"Capture file not found: {path}", file=sys.stderr)
        sys.exit(1)

    if not flows:
        print("Zero flows captured — telemetry proof passed.")
        sys.exit(0)
    else:
        print(f"{len(flows)} flow(s) captured — telemetry proof FAILED.")
        for flow in flows:
            print(f"  {flow.request.method} {flow.request.pretty_url}")
        sys.exit(1)


if __name__ == "__main__":
    main()
