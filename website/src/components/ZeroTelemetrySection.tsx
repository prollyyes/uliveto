import React from 'react';

export const ZeroTelemetrySection: React.FC = () => {
  const codeSnippet = `def main():
    path = sys.argv[1]
    flows = []
    with open(path, "rb") as fh:
        for flow in mio.FlowReader(fh).stream():
            flows.append(flow) # record every flow type

    if not flows:
        print("✅ Zero flows captured — telemetry proof passed.")
        sys.exit(0)
    else:
        print("❌ Telemetry proof FAILED. Offending hosts:")
        for flow in flows:
            print(f"  {flow.request.method} {flow.request.pretty_url}")
        sys.exit(1)`;

  return (
    <section id="privacy" className="w-full py-20 bg-[#121214] text-white">
      <div className="max-w-6xl mx-auto px-6">
        
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          
          {/* Left Block: Explanation */}
          <div className="lg:col-span-5">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-red-950/40 border border-red-900/40 text-red-400 text-xs font-semibold uppercase tracking-wider mb-6">
              <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse" />
              <span>CI Verified Privacy</span>
            </div>
            
            <h2 className="font-instrument italic text-4xl md:text-5xl mb-6 leading-tight">
              Zero Telemetry. <br />
              No exceptions. No pings.
            </h2>
            
            <p className="font-hanken text-slate-400 text-base leading-relaxed mb-6">
              Most browsers tell you they respect your privacy in long policies, while their apps silently send telemetry reports, search suggestions, and crash reports. 
            </p>
            
            <p className="font-hanken text-slate-400 text-base leading-relaxed mb-8">
              Uliveto guarantees zero telemetry. Every single compile triggers a GitHub Actions emulator build. All traffic is intercepted through **mitmproxy**. If the browser attempts to make a single connection during cold start — even a failed TLS handshake — the build automatically fails.
            </p>

            <div className="border-l-2 border-[#b25737] pl-4 italic text-sm text-slate-300 font-hanken">
              "We don't ask you to trust a privacy policy. We ask you to trust a failing build."
            </div>
          </div>

          {/* Right Block: Visual Code Inspector */}
          <div className="lg:col-span-7">
            <div className="w-full rounded-2xl border border-slate-800 bg-slate-950/60 overflow-hidden shadow-2xl">
              {/* Header controls */}
              <div className="flex items-center justify-between px-5 py-3.5 bg-slate-900/80 border-b border-slate-900">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full bg-red-500/80" />
                  <span className="w-3 h-3 rounded-full bg-amber-500/80" />
                  <span className="w-3 h-3 rounded-full bg-green-500/80" />
                </div>
                <span className="font-mono text-xs text-slate-500">scripts/assert-no-flows.py</span>
                <span className="w-4" /> {/* spacer */}
              </div>

              {/* Code window */}
              <div className="p-6 overflow-x-auto">
                <pre className="font-mono text-xs md:text-sm text-slate-300 leading-6">
                  <code>
                    {codeSnippet}
                  </code>
                </pre>
              </div>

              {/* Footer status */}
              <div className="flex items-center justify-between px-5 py-3 bg-slate-900/30 border-t border-slate-900 text-xs font-semibold text-slate-500 font-hanken">
                <div className="flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-4 h-4 text-green-500">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75 11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 0 1-1.043 3.296 3.745 3.745 0 0 1-3.296 1.043A3.745 3.745 0 0 1 12 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 0 1-3.296-1.043 3.745 3.745 0 0 1-1.043-3.296A3.745 3.745 0 0 1 3 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 0 1 1.043-3.296 3.746 3.746 0 0 1 3.296-1.043A3.746 3.746 0 0 1 12 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 0 1 3.296 1.043 3.746 3.746 0 0 1 1.043 3.296A3.745 3.745 0 0 1 21 12Z" />
                  </svg>
                  <span>Build Verified - 0 Outbound Packets</span>
                </div>
                <span>100% Client-Side</span>
              </div>
            </div>
          </div>

        </div>

      </div>
    </section>
  );
};
