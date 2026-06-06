import { Hero } from './components/Hero';
import { InteractiveBrowserMockup } from './components/InteractiveBrowserMockup';
import { ZeroTelemetrySection } from './components/ZeroTelemetrySection';
import { FeaturesGrid } from './components/FeaturesGrid';
import { FerrofluidParticles } from './components/FerrofluidParticles';

function App() {
  const scrollToMockup = () => {
    const element = document.getElementById('looks');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="min-h-screen bg-[#121214] text-slate-100 flex flex-col selection:bg-[#b25737]/30 selection:text-white relative overflow-hidden">
      {/* Global Interactive Background Ferrofluid Particles */}
      <FerrofluidParticles />

      {/* Hero Section */}
      <Hero onExploreMockup={scrollToMockup} />

      {/* Main Interactive Showcase */}
      <InteractiveBrowserMockup />

      {/* Telemetry Lockdown Section */}
      <ZeroTelemetrySection />

      {/* Feature Grid */}
      <FeaturesGrid />

      {/* Immersive CTA / Download Section */}
      <section className="w-full py-24 bg-radial-[at_bottom] from-[#b25737]/20 via-[#121214] to-[#121214] border-t border-slate-800/60 relative overflow-hidden">
        {/* Glow */}
        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full max-w-5xl h-[300px] bg-[#b25737]/10 blur-3xl pointer-events-none" />

        <div className="max-w-4xl mx-auto px-6 text-center relative z-10">
          <h2 className="font-instrument italic text-4xl md:text-6xl text-white mb-6">
            Get Uliveto on your phone.
          </h2>
          <p className="font-hanken text-slate-400 text-base md:text-lg max-w-xl mx-auto mb-10 leading-relaxed">
            Ready to browse like no one is watching? Download the latest CI-verified release or compile the project directly from source.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-6 max-w-md mx-auto mb-16">
            <a 
              href="https://github.com/prollyyes/uliveto/actions" 
              target="_blank" 
              rel="noopener noreferrer"
              className="w-full sm:w-auto px-8 py-4 font-semibold text-white bg-gradient-to-r from-[#b25737] to-[#7e3415] rounded-full hover:shadow-[0_0_24px_rgba(178,87,55,0.4)] transition-all flex items-center justify-center gap-2 group"
            >
              <span>Download APK</span>
              <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-4 h-4 group-hover:translate-x-1 transition-transform">
                <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3" />
              </svg>
            </a>
            <a 
              href="https://github.com/prollyyes/uliveto" 
              target="_blank" 
              rel="noopener noreferrer"
              className="w-full sm:w-auto px-8 py-4 font-semibold rounded-full border border-slate-700 hover:border-slate-500 hover:bg-slate-900/40 hover:text-white transition-all flex items-center justify-center"
            >
              Explore Github Repo
            </a>
          </div>

          {/* Quick source code building guide */}
          <div className="p-6 rounded-2xl bg-slate-950/60 border border-slate-800/60 text-left max-w-2xl mx-auto">
            <h3 className="text-sm font-semibold uppercase tracking-wider text-[#b25737] mb-3">Build from Source</h3>
            <pre className="font-mono text-xs text-slate-400 overflow-x-auto leading-5 space-y-1">
              <code>{`# Clone the repository
git clone git@github.com:prollyyes/uliveto.git
cd uliveto

# Compile the debug APK
./gradlew assembleDebug`}</code>
            </pre>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="w-full py-12 bg-slate-950 border-t border-slate-900 text-slate-500 text-xs font-hanken">
        <div className="max-w-6xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-3">
            <img src="/logo.svg" alt="" className="w-6 h-6 opacity-40" />
            <span>&copy; {new Date().getFullYear()} Uliveto. Open Source.</span>
          </div>
          <div className="flex flex-wrap items-center justify-center gap-8">
            <a href="https://github.com/prollyyes/uliveto/blob/main/LICENSE" target="_blank" rel="noopener noreferrer" className="hover:text-slate-350 transition-colors">App License</a>
            <a href="https://github.com/prollyyes/uliveto/blob/main/NOTICE.md" target="_blank" rel="noopener noreferrer" className="hover:text-slate-350 transition-colors">Attributions</a>
            <a href="https://github.com/prollyyes/uliveto" target="_blank" rel="noopener noreferrer" className="hover:text-slate-350 transition-colors">Source Code</a>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default App;
