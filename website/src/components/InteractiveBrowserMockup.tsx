import React, { useState } from 'react';

interface ScreenOption {
  id: string;
  title: string;
  subtitle: string;
  image: string;
  description: string;
  highlights: string[];
}

export const InteractiveBrowserMockup: React.FC = () => {
  const [activeScreen, setActiveScreen] = useState<string>('homepage');

  const screens: ScreenOption[] = [
    {
      id: 'homepage',
      title: 'The Homepage',
      subtitle: 'Warm and Uncluttered',
      image: '/images/screenshot_start.jpg',
      description: 'Your browser is your window, not a billboard. The homepage opens with a personalized greeting in a warm Mediterranean tone, closer to textured paper than fluorescent screens. Zero distraction, zero feed pings, just a clean focus on search.',
      highlights: [
        'Personalized local greeting',
        'Radial warm gradients with low alpha grain',
        'Ergonomic floating search bar and navigation'
      ]
    },
    {
      id: 'active_web',
      title: 'Active Browsing',
      subtitle: 'Ergonomic & Transparent',
      image: '/images/screenshot_browser.jpg',
      description: 'Once you load a page, the chrome gets out of the way. Built on the GeckoView rendering core, pages load quickly and are framed by a frosted-glass bottom bar containing back, forward, URL context, and overflow options.',
      highlights: [
        'Hardened GeckoView core engine',
        'Scroll-to-hide bottom bar for maximum viewport',
        'Ergonomic layout designed for thumb access'
      ]
    },
    {
      id: 'settings',
      title: 'Deep Customization',
      subtitle: 'Harmonious Color Palettes',
      image: '/images/screenshot_settings.jpg',
      description: 'Tailor the browser to your environment. Switch between unique themes like Terracotta, Aegean Blue, Olive Grove, Amalfi gold, or a battery-saving OLED Night mode. Toggle between floating bubbles or classic top/bottom navigation structures.',
      highlights: [
        '6 unique native Mediterranean themes',
        'Hourglass (Bubbles) vs. Classic nav toggles',
        'Explicit and transparent privacy settings'
      ]
    }
  ];

  const current = screens.find(s => s.id === activeScreen) || screens[0];

  return (
    <section id="looks" className="w-full py-20 bg-[#16171d] border-t border-b border-slate-800/60">
      <div className="max-w-6xl mx-auto px-6">
        
        {/* Title */}
        <div className="text-center mb-16">
          <h2 className="font-instrument italic text-4xl md:text-5xl text-white mb-4">
            Designed like an olive tree. Built for the long run.
          </h2>
          <p className="font-hanken text-slate-400 max-w-2xl mx-auto">
            Most browsers are built to track you. Uliveto is built to respect your eyes and your choice, carrying the sensibility of a Mediterranean afternoon.
          </p>
        </div>

        {/* Core Layout */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-16 items-center">
          
          {/* Left Panel: Description and Selector */}
          <div className="lg:col-span-6 flex flex-col justify-center order-2 lg:order-1">
            <div className="flex flex-col gap-3 mb-8">
              {screens.map((s) => (
                <button
                  key={s.id}
                  onClick={() => setActiveScreen(s.id)}
                  className={`w-full text-left p-5 rounded-2xl border transition-all duration-300 ${
                    activeScreen === s.id
                      ? 'bg-[#b25737]/10 border-[#b25737]/40 text-white shadow-[0_4px_20px_rgba(178,87,55,0.1)]'
                      : 'bg-slate-900/40 border-slate-800/50 text-slate-400 hover:border-slate-700/60 hover:text-slate-200'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold font-hanken tracking-wide">{s.title}</span>
                    <span className={`text-xs px-2.5 py-0.5 rounded-full ${
                      activeScreen === s.id ? 'bg-[#b25737]/20 text-[#f5e6d3]' : 'bg-slate-800 text-slate-500'
                    }`}>
                      {s.subtitle}
                    </span>
                  </div>
                </button>
              ))}
            </div>

            {/* Description Area */}
            <div className="min-h-[220px] p-6 rounded-2xl bg-slate-950/40 border border-slate-800/40">
              <h3 className="font-instrument text-2xl italic text-[#f5e6d3] mb-3">{current.title}</h3>
              <p className="font-hanken text-sm text-slate-400 leading-relaxed mb-4">
                {current.description}
              </p>
              <ul className="space-y-2">
                {current.highlights.map((h, i) => (
                  <li key={i} className="flex items-center gap-3 text-xs text-slate-300">
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 text-[#4a5c25] shrink-0">
                      <path fillRule="evenodd" d="M16.704 4.153a.75.75 0 01.143 1.052l-8 10.5a.75.75 0 01-1.127.075l-4.5-4.5a.75.75 0 011.06-1.06l3.894 3.893 7.48-9.817a.75.75 0 011.05-.143z" clipRule="evenodd" />
                    </svg>
                    <span>{h}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Right Panel: Phone Frame Mockup */}
          <div className="lg:col-span-6 flex justify-center order-1 lg:order-2">
            <div className="relative w-[280px] h-[580px] md:w-[310px] md:h-[640px] p-[10px] bg-slate-900 rounded-[48px] border-4 border-slate-700 shadow-2xl overflow-hidden flex items-center justify-center">
              {/* Speaker Bar */}
              <div className="absolute top-3 left-1/2 -translate-x-1/2 w-20 h-1 bg-slate-800 rounded-full z-20" />
              {/* Front Camera Notch */}
              <div className="absolute top-5 left-1/2 -translate-x-1/2 w-6 h-6 bg-black rounded-full z-20 border border-slate-850" />

              {/* Viewport container */}
              <div className="relative w-full h-full rounded-[38px] overflow-hidden bg-slate-950">
                <img
                  src={current.image}
                  alt={`Uliveto Screen - ${current.title}`}
                  className="w-full h-full object-cover transition-opacity duration-300"
                />
              </div>

              {/* Home Indicator */}
              <div className="absolute bottom-3 left-1/2 -translate-x-1/2 w-28 h-1 bg-slate-400/30 rounded-full z-20" />
            </div>
          </div>

        </div>

      </div>
    </section>
  );
};
