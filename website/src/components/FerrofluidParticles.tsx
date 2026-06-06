import React, { useEffect, useRef } from 'react';

export const FerrofluidParticles: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    // Track mouse coordinates
    const mouse = {
      x: width / 2,
      y: height / 2,
      active: false,
      targetX: width / 2,
      targetY: height / 2,
    };

    // Particles array
    interface Particle {
      x: number;
      y: number;
      vx: number;
      vy: number;
      radius: number;
      color: string;
      baseX: number;
      baseY: number;
      noiseOffset: number;
    }

    const particles: Particle[] = [];
    const particleCount = 140;

    // Mediterranean theme colors (blend with terracotta and cream golds)
    const colors = [
      'rgba(178, 87, 55, 0.95)',  // Terracotta
      'rgba(126, 52, 21, 0.95)',  // Deep copper
      'rgba(245, 230, 211, 0.95)', // Cream gold
      'rgba(193, 127, 24, 0.95)',  // Muted amalfi gold
      'rgba(74, 92, 37, 0.85)',    // Olive green
      'rgba(26, 94, 143, 0.85)',   // Aegean blue
    ];

    for (let i = 0; i < particleCount; i++) {
      const x = Math.random() * width;
      const y = Math.random() * height;
      particles.push({
        x,
        y,
        vx: 0,
        vy: 0,
        radius: 10 + Math.random() * 16, // Various sizes to allow metaballs to merge/spill organically
        color: colors[Math.floor(Math.random() * colors.length)],
        baseX: x,
        baseY: y,
        noiseOffset: Math.random() * 1000,
      });
    }

    const handleMouseMove = (e: MouseEvent) => {
      mouse.targetX = e.clientX;
      mouse.targetY = e.clientY;
      mouse.active = true;
    };

    const handleTouchMove = (e: TouchEvent) => {
      if (e.touches.length > 0) {
        mouse.targetX = e.touches[0].clientX;
        mouse.targetY = e.touches[0].clientY;
        mouse.active = true;
      }
    };

    const handleMouseLeave = () => {
      mouse.active = false;
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('touchmove', handleTouchMove);
    document.addEventListener('mouseleave', handleMouseLeave);

    const handleResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener('resize', handleResize);

    // Animation Loop
    let animationId: number;
    let time = 0;

    const animate = () => {
      time += 0.005;
      
      // Draw solid black background. The CSS screen blend-mode will filter it out,
      // showing only the illuminated particles on top of the site page content.
      ctx.fillStyle = '#000000';
      ctx.fillRect(0, 0, width, height);

      // Smoothly interpolate cursor target coordinates to create oil-like inertia
      mouse.x += (mouse.targetX - mouse.x) * 0.1;
      mouse.y += (mouse.targetY - mouse.y) * 0.1;

      // If user isn't moving, simulate a slow swirling magnetic vortex in the background
      if (!mouse.active) {
        mouse.targetX = width / 2 + Math.sin(time * 2.2) * (width * 0.28);
        mouse.targetY = height / 2 + Math.cos(time * 1.3) * (height * 0.25);
      }

      particles.forEach((p) => {
        // 1. Viscous idle float noise
        p.noiseOffset += 0.003;
        const driftX = Math.sin(p.noiseOffset) * 0.25;
        const driftY = Math.cos(p.noiseOffset) * 0.25;

        // 2. Attraction to the cursor (Ferrofluid pull)
        const dx = mouse.x - p.x;
        const dy = mouse.y - p.y;
        const dist = Math.sqrt(dx * dx + dy * dy);

        let fx = 0;
        let fy = 0;

        const maxInfluence = 360;
        if (dist < maxInfluence) {
          // Strong exponential magnetic draw
          const strength = (maxInfluence - dist) / maxInfluence;
          const pull = strength * strength * strength * 0.95;
          
          fx = (dx / dist) * pull;
          fy = (dy / dist) * pull;

          // Add a circular vortex drift around the cursor (magnetic spin)
          const vortex = strength * 0.35;
          fx += (-dy / dist) * vortex;
          fy += (dx / dist) * vortex;

          // Repulsive barrier if too close to avoid compressing into a single point
          if (dist < 50) {
            const push = (50 - dist) * 0.12;
            fx -= (dx / dist) * push;
            fy -= (dy / dist) * push;
          }
        } else {
          // Return gently to original base location if magnet moves away
          const homeDx = p.baseX - p.x;
          const homeDy = p.baseY - p.y;
          const homeDist = Math.sqrt(homeDx * homeDx + homeDy * homeDy);
          if (homeDist > 5) {
            fx = (homeDx / homeDist) * 0.04;
            fy = (homeDy / homeDist) * 0.04;
          }
        }

        // Apply magnetic forces with strong oil viscosity damping
        p.vx = (p.vx + fx + driftX) * 0.88;
        p.vy = (p.vy + fy + driftY) * 0.88;

        p.x += p.vx;
        p.y += p.vy;

        // Soft screen wrapping bounds
        if (p.x < -p.radius) p.x = p.baseX = width + p.radius;
        if (p.x > width + p.radius) p.x = p.baseX = -p.radius;
        if (p.y < -p.radius) p.y = p.baseY = height + p.radius;
        if (p.y > height + p.radius) p.y = p.baseY = -p.radius;

        // Draw radial fade circle
        ctx.beginPath();
        const grad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.radius);
        grad.addColorStop(0, p.color);
        grad.addColorStop(1, 'rgba(0, 0, 0, 0)');
        ctx.fillStyle = grad;
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
        ctx.fill();
      });

      animationId = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      cancelAnimationFrame(animationId);
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('touchmove', handleTouchMove);
      document.removeEventListener('mouseleave', handleMouseLeave);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <div 
      className="fixed inset-0 pointer-events-none z-1" 
      style={{ 
        mixBlendMode: 'screen', 
        filter: 'blur(8px) contrast(15)',
        opacity: 0.40
      }}
    >
      <canvas ref={canvasRef} className="w-full h-full block" />
    </div>
  );
};
