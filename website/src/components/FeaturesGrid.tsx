import React from 'react';

interface FeatureCard {
  title: string;
  description: string;
  icon: React.ReactNode;
}

export const FeaturesGrid: React.FC = () => {
  const cards: FeatureCard[] = [
    {
      title: 'Hourglass Navigation',
      description: 'An ergonomic bottom control shape modeled after natural hand positioning. Accessible, floating, and fades away dynamically as you scroll.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
        </svg>
      )
    },
    {
      title: 'GeckoView Engine',
      description: 'Built on Mozilla’s independent layout engine. You get a real, modern rendering system that isn’t controlled by the Chromium monopoly.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M14.25 9.75 16.5 12l-2.25 2.25m-4.5 0L7.5 12l2.25-2.25M6 20.25h12A2.25 2.25 0 0 0 20.25 18V6A2.25 2.25 0 0 0 18 3.75H6A2.25 2.25 0 0 0 3.75 6v12A2.25 2.25 0 0 0 6 20.25Z" />
        </svg>
      )
    },
    {
      title: 'WebExtension Support',
      description: 'Designed to support Firefox WebExtensions natively. Load robust network blockers directly on your mobile device.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M9.594 3.94c.09-.542.56-.94 1.11-.94h1.593c.55 0 1.02.398 1.11.94l.213 1.281c.063.374.313.686.645.87.074.04.147.083.22.127.324.196.72.257 1.075.124l1.217-.456a1.125 1.125 0 0 1 1.37.49l1.296 2.247a1.125 1.125 0 0 1-.26 1.43l-1.003.828c-.293.241-.438.613-.43.992a7.723 7.723 0 0 1 0 .255c-.008.378.137.75.43.991l1.004.827c.424.35.534.954.26 1.43l-1.298 2.247a1.125 1.125 0 0 1-1.369.491l-1.217-.456c-.355-.133-.75-.072-1.076.124a6.47 6.47 0 0 1-.22.128c-.331.183-.581.495-.644.869l-.213 1.281c-.09.543-.56.94-1.11.94h-1.594c-.55 0-1.019-.398-1.11-.94l-.213-1.281c-.062-.374-.312-.686-.644-.87a6.52 6.52 0 0 1-.22-.127c-.325-.196-.72-.257-1.076-.124l-1.217.456a1.125 1.125 0 0 1-1.369-.49l-1.297-2.247a1.125 1.125 0 0 1 .26-1.43l1.004-.827c.292-.24.437-.613.43-.991a6.936 6.936 0 0 1 0-.255c.007-.38-.138-.751-.43-.992l-1.004-.827a1.125 1.125 0 0 1-.26-1.43l1.297-2.247a1.125 1.125 0 0 1 1.37-.491l1.216.456c.356.133.751.072 1.076-.124.072-.044.146-.086.22-.128.332-.183.582-.495.644-.869l.214-1.28Z" />
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
        </svg>
      )
    },
    {
      title: 'Local Privacy Database',
      description: 'Your tabs, history, and bookmarks live strictly on your device inside a local Room database. No cloud sync, no remote servers, no account required.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 6.375c0 2.278-3.694 4.125-8.25 4.125S3.75 8.653 3.75 6.375m16.5 0c0-2.278-3.694-4.125-8.25-4.125S3.75 4.097 3.75 6.375m16.5 0v11.25c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125V6.375m16.5 0v3.75m-16.5-3.75v3.75m16.5 0v3.75C20.25 16.153 16.556 18 12 18s-8.25-1.847-8.25-4.125v-3.75m16.5 0v3.75" />
        </svg>
      )
    },
    {
      title: 'Excluded Glean Classpath',
      description: 'Hard protection against trackers. We physically exclude Mozilla Glean, Nimbus, and Firebase from compilation. Telemetry cannot run if its libraries do not exist.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m0-10.036A11.959 11.959 0 0 1 3.598 6 11.99 11.99 0 0 0 3 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.152c-3.196 0-6.1-1.249-8.25-3.286Zm0 13.036h.008v.008H12v-.008Z" />
        </svg>
      )
    },
    {
      title: 'OLED Night Mode',
      description: 'True black styling that lets AMOLED screens switch pixels off entirely. Reduces battery consumption and treats your eyes with respect during night browsing.',
      icon: (
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-6 h-6 text-[#b25737]">
          <path strokeLinecap="round" strokeLinejoin="round" d="M21.752 15.002A9.72 9.72 0 0 1 18 15.75c-5.385 0-9.75-4.365-9.75-9.75 0-1.33.266-2.597.748-3.752A9.753 9.753 0 0 0 3 11.25C3 16.635 7.365 21 12.75 21a9.753 9.753 0 0 0 9.002-5.998Z" />
        </svg>
      )
    }
  ];

  return (
    <section id="features" className="w-full py-20 bg-[#16171d] text-white">
      <div className="max-w-6xl mx-auto px-6">
        
        {/* Title */}
        <div className="text-center mb-16">
          <span className="text-xs font-semibold uppercase tracking-widest text-[#b25737] block mb-2">Capabilities</span>
          <h2 className="font-instrument italic text-4xl md:text-5xl text-white">
            Built from scratch. Hardened by design.
          </h2>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {cards.map((card, index) => (
            <div 
              key={index}
              className="relative p-8 rounded-2xl bg-slate-900/45 border border-slate-800/50 glass-surface hover:border-[#b25737]/50 hover:shadow-[0_15px_45px_rgba(178,87,55,0.08)] transition-all duration-500 group overflow-hidden"
            >
              {/* Dynamic hover sheen effect overlay */}
              <div className="absolute inset-0 bg-gradient-to-tr from-transparent via-white/4 to-transparent w-[200%] h-[200%] -translate-x-[100%] -translate-y-[100%] rotate-45 transition-transform duration-1000 ease-out group-hover:translate-x-[100%] group-hover:translate-y-[100%] pointer-events-none" />

              <div className="w-12 h-12 rounded-xl bg-slate-950/60 flex items-center justify-center mb-6 group-hover:scale-110 transition-transform border border-slate-800 group-hover:border-[#b25737]/50">
                {card.icon}
              </div>
              <h3 className="font-hanken font-bold text-lg text-white mb-3 tracking-wide">{card.title}</h3>
              <p className="font-hanken text-sm text-slate-400 leading-relaxed">
                {card.description}
              </p>
            </div>
          ))}
        </div>

      </div>
    </section>
  );
};
