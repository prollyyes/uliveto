import React from 'react';

interface HeroProps {
  onExploreMockup: () => void;
}

export const Hero: React.FC<HeroProps> = ({ onExploreMockup }) => {
  return (
    <header className="relative w-full overflow-hidden pt-12 pb-24 md:pt-20 md:pb-36 bg-radial-[at_top] from-[#4a5c25]/30 via-[#121214] to-[#121214]">
      {/* Decorative Warm Gradients */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full max-w-7xl h-[400px] bg-gradient-to-b from-[#b25737]/15 to-transparent blur-3xl pointer-events-none" />

      <div className="max-w-6xl mx-auto px-6 relative z-10 flex flex-col items-center text-center">
        {/* Nav Bar */}
        <nav className="w-full flex items-center justify-between mb-16 md:mb-24">
          <div className="flex items-center gap-3">
            <img 
              src="/logo.svg" 
              alt="Uliveto Logo" 
              className="w-8 h-8 animate-slow-spin" 
            />
            <span className="font-hanken font-bold text-xl tracking-wide text-white">Uliveto</span>
          </div>
          <div className="hidden md:flex items-center gap-8 text-sm font-medium text-slate-300">
            <a href="#looks" className="hover:text-[#b25737] transition-colors">Aesthetics</a>
            <a href="#privacy" className="hover:text-[#b25737] transition-colors">Provable Privacy</a>
            <a href="#features" className="hover:text-[#b25737] transition-colors">Features</a>
          </div>
          <div>
            <a 
              href="https://github.com/prollyyes/uliveto" 
              target="_blank" 
              rel="noopener noreferrer"
              className="px-4 py-2 text-xs font-semibold rounded-full border border-slate-700 hover:border-slate-500 hover:text-white transition-all bg-slate-900/60"
            >
              GitHub
            </a>
          </div>
        </nav>

        {/* Rotating Main Logo */}
        <div className="relative mb-8 group cursor-pointer" onClick={onExploreMockup}>
          {/* Outer glow */}
          <div className="absolute inset-0 bg-[#b25737]/25 rounded-full blur-2xl group-hover:bg-[#b25737]/40 transition-all duration-500" />
          <div className="relative w-36 h-36 md:w-44 md:h-44 p-6 bg-slate-900/40 rounded-full border border-slate-800 glass-surface flex items-center justify-center">
            <img 
              src="/logo.svg" 
              alt="Uliveto Rotating Olive Branch Logo" 
              className="w-full h-full object-contain animate-slow-spin"
              style={{ animationDuration: '30s' }}
            />
          </div>
        </div>

        {/* Headline */}
        <h1 className="font-instrument italic font-normal text-5xl md:text-7xl lg:text-8xl text-white tracking-tight leading-[1.05] max-w-4xl mb-6">
          The web, as it was <br className="hidden md:inline" />
          <span className="text-[#f5e6d3] font-sans font-bold not-italic tracking-normal bg-gradient-to-r from-[#f5e6d3] to-[#b25737] bg-clip-text text-transparent">
            always meant to feel.
          </span>
        </h1>

        {/* Subtitle */}
        <p className="font-hanken text-lg md:text-xl text-slate-400 max-w-2xl leading-relaxed mb-10">
          A privacy-first, design-led Android browser. Zero telemetry. <br className="hidden sm:inline" />
          Ergonomic bottom navigation. Mediterranean spirit.
        </p>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row items-center gap-4 mb-16">
          <a 
            href="https://github.com/prollyyes/uliveto/actions" 
            target="_blank" 
            rel="noopener noreferrer"
            className="w-full sm:w-auto px-8 py-4 font-semibold text-white bg-gradient-to-r from-[#b25737] to-[#7e3415] rounded-full hover:shadow-[0_0_24px_rgba(178,87,55,0.4)] transition-all flex items-center justify-center gap-2 group"
          >
            <span>Download latest APK</span>
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-4 h-4 group-hover:translate-y-1 transition-transform">
              <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3" />
            </svg>
          </a>
          <button 
            onClick={onExploreMockup}
            className="w-full sm:w-auto px-8 py-4 font-semibold rounded-full border border-slate-700 hover:border-slate-500 hover:bg-slate-900/40 hover:text-white transition-all flex items-center justify-center gap-2"
          >
            <span>Explore UI Screens</span>
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-4 h-4">
              <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
            </svg>
          </button>
        </div>

        {/* Quick Badge */}
        <div className="flex flex-wrap items-center justify-center gap-4 md:gap-8 text-xs font-semibold uppercase tracking-widest text-slate-500 border-t border-slate-800/60 pt-8 w-full max-w-3xl">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#4a5c25]" />
            <span>Zero Telemetry Verified</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-[#b25737]" />
            <span>GeckoView Core</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-amber-600" />
            <span>Mediterranean Aesthetics</span>
          </div>
        </div>
      </div>
    </header>
  );
};
