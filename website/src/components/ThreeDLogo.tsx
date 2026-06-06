import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { SVGLoader } from 'three/examples/jsm/loaders/SVGLoader.js';

// Helper to create a procedural reflection map for the chrome effect
const createReflectiveEnvMap = (): THREE.CanvasTexture => {
  const canvas = document.createElement('canvas');
  canvas.width = 512;
  canvas.height = 256;
  const ctx = canvas.getContext('2d');
  if (ctx) {
    // 1. Draw a high-contrast dark grey studio background gradient
    const grad = ctx.createLinearGradient(0, 0, 0, canvas.height);
    grad.addColorStop(0, '#0c0d10');
    grad.addColorStop(0.4, '#1b1d24');
    grad.addColorStop(0.5, '#040405'); // sharp horizon split
    grad.addColorStop(0.6, '#181a21');
    grad.addColorStop(1, '#060607');
    ctx.fillStyle = grad;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 2. Add white studio softbox panels for bright shiny reflections
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(30, 20, 90, 80);   // Upper left panel
    ctx.fillRect(390, 30, 90, 70);  // Upper right panel

    // 3. Bright vertical and horizontal neon lines for razor-sharp chrome highlights
    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 14;
    ctx.beginPath();
    ctx.moveTo(0, 128);
    ctx.lineTo(canvas.width, 128); // Horizon light
    ctx.stroke();

    ctx.strokeStyle = '#ffffff';
    ctx.lineWidth = 8;
    ctx.beginPath();
    ctx.moveTo(160, 0);
    ctx.lineTo(160, canvas.height);
    ctx.moveTo(320, 0);
    ctx.lineTo(320, canvas.height);
    ctx.stroke();

    // 4. Subtle Mediterranean colors to paint copper and blue sheens into the metal
    ctx.fillStyle = '#b25737'; // Terracotta
    ctx.fillRect(150, 150, 100, 50);

    ctx.fillStyle = '#1a5e8f'; // Aegean Blue
    ctx.fillRect(300, 160, 80, 45);
  }

  const texture = new THREE.CanvasTexture(canvas);
  texture.mapping = THREE.EquirectangularReflectionMapping;
  return texture;
};

export const ThreeDLogo: React.FC = () => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!containerRef.current) return;

    // ── 1. Dimensions ────────────────────────────────────────────────────────
    const width = containerRef.current.clientWidth || 192;
    const height = containerRef.current.clientHeight || 192;

    // ── 2. Scene & Camera Setup ──────────────────────────────────────────────
    const scene = new THREE.Scene();

    // Set procedural environment map for true mirror reflections
    const envTexture = createReflectiveEnvMap();
    scene.environment = envTexture;

    const camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 1000);
    camera.position.set(0, 0, 12);
    camera.lookAt(0, 0, 0);

    // ── 3. Renderer Setup ────────────────────────────────────────────────────
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.shadowMap.enabled = true;
    
    // Force the canvas to match the CSS boundaries of its parent container
    renderer.domElement.style.width = '100%';
    renderer.domElement.style.height = '100%';
    renderer.domElement.style.display = 'block';
    
    containerRef.current.appendChild(renderer.domElement);

    // ── 4. High-Contrast Studio Lighting (Chrome Reflections) ────────────────
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8); // Brighter ambient to lift shadows
    scene.add(ambientLight);

    // Key Light: Bright white from top-right
    const keyLight = new THREE.DirectionalLight(0xffffff, 4.0);
    keyLight.position.set(6, 6, 8);
    scene.add(keyLight);

    // Front Light: To ensure the front faces look bright silver and not black
    const frontLight = new THREE.DirectionalLight(0xffffff, 2.5);
    frontLight.position.set(0, 0, 10);
    scene.add(frontLight);

    // Fill Light: Warm Mediterranean terracotta bounce from bottom-left
    const terracottaFill = new THREE.DirectionalLight(0xb25737, 3.0);
    terracottaFill.position.set(-6, -4, 4);
    scene.add(terracottaFill);

    // Rim Light: Aegean blue accent from top-left-back for edge highlights
    const rimLight = new THREE.DirectionalLight(0x1a5e8f, 2.5);
    rimLight.position.set(-4, 5, -6);
    scene.add(rimLight);

    // Specular Catch: Pure white point light directly in front
    const pointLight = new THREE.PointLight(0xffffff, 4.0, 50);
    pointLight.position.set(0, 0, 10);
    scene.add(pointLight);

    // ── 4b. Floating Embers (Antigravity Particles) ──────────────────────────
    const particlesCount = 45;
    const particlesGeometry = new THREE.BufferGeometry();
    const positions = new Float32Array(particlesCount * 3);
    const speeds = new Float32Array(particlesCount);
    const waveOffsets = new Float32Array(particlesCount);

    for (let i = 0; i < particlesCount; i++) {
      positions[i * 3] = (Math.random() - 0.5) * 8;     // X
      positions[i * 3 + 1] = (Math.random() - 0.5) * 8; // Y
      positions[i * 3 + 2] = (Math.random() - 0.5) * 4 - 1.5; // Z (mostly slightly behind the logo)
      
      speeds[i] = 0.003 + Math.random() * 0.006;         // Drift speed
      waveOffsets[i] = Math.random() * Math.PI * 2;
    }

    particlesGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

    // Particle texture (soft warm cream/terracotta circular glow)
    const pCanvas = document.createElement('canvas');
    pCanvas.width = 16;
    pCanvas.height = 16;
    const pCtx = pCanvas.getContext('2d');
    if (pCtx) {
      const pGrad = pCtx.createRadialGradient(8, 8, 0, 8, 8, 8);
      pGrad.addColorStop(0, 'rgba(255, 255, 255, 1)'); // White core
      pGrad.addColorStop(0.3, 'rgba(245, 230, 211, 0.8)'); // Cream edge
      pGrad.addColorStop(0.6, 'rgba(178, 87, 55, 0.4)'); // Terracotta halo
      pGrad.addColorStop(1, 'rgba(0, 0, 0, 0)');
      pCtx.fillStyle = pGrad;
      pCtx.fillRect(0, 0, 16, 16);
    }
    const pTexture = new THREE.CanvasTexture(pCanvas);

    const particlesMaterial = new THREE.PointsMaterial({
      size: 0.22,
      map: pTexture,
      transparent: true,
      blending: THREE.AdditiveBlending,
      depthWrite: false,
      opacity: 0.65,
    });

    const particleSystem = new THREE.Points(particlesGeometry, particlesMaterial);
    scene.add(particleSystem);

    // Mouse reactivity removed to keep the branch spinning strictly on Y-axis

    // ── 5. Extruded 3D Mesh Group ────────────────────────────────────────────
    const group = new THREE.Group();
    scene.add(group);

    // Load and Parse SVG
    const loader = new SVGLoader();
    loader.load(
      '/logo.svg',
      (data) => {
        const paths = data.paths;

        // High-fidelity reflective Chrome Material (mirror silver)
        const chromeMaterial = new THREE.MeshPhysicalMaterial({
          color: 0xffffff,       // Pure white base for maximum silver reflectivity
          metalness: 1.0,        // Fully metallic
          roughness: 0.015,      // Ultra-smooth mirror-like finish
          clearcoat: 1.0,        // Outer glossy varnish layer
          clearcoatRoughness: 0.015,
        });

        const meshes: THREE.Mesh[] = [];

        paths.forEach((path) => {
          const shapes = path.toShapes(true);
          shapes.forEach((shape) => {
            // Extrude the 2D path into a 3D geometry
            const geometry = new THREE.ExtrudeGeometry(shape, {
              depth: 18,            // Noticeable thickness
              bevelEnabled: true,
              bevelSegments: 6,
              steps: 2,
              bevelSize: 1.8,
              bevelThickness: 1.8
            });

            const mesh = new THREE.Mesh(geometry, chromeMaterial);
            meshes.push(mesh);
            group.add(mesh);
          });
        });

        // ── 6. Centering and Scaling ─────────────────────────────────────────
        // Calculate the bounding box of the group
        const box = new THREE.Box3().setFromObject(group);
        const center = new THREE.Vector3();
        box.getCenter(center);
        const size = new THREE.Vector3();
        box.getSize(size);

        // Center all children relative to the group's origin
        group.children.forEach((child) => {
          child.position.x -= center.x;
          child.position.y -= center.y;
          child.position.z -= center.z;
        });

        // Scale the group to fit the viewport and invert the Y-axis (SVG vs Three.js)
        const maxDim = Math.max(size.x, size.y);
        const scale = 5.2 / maxDim; // Restored to a larger visual scale (now that canvas is scaled properly and margins offset it)
        group.scale.set(scale, -scale, scale);

        // Position the group close to WebGL center. Since we shifted the container itself
        // up in the HTML layout, we only need a minor visual center Y adjustment (0.1 units).
        group.position.y = 0.1;

        setLoading(false);
      },
      undefined,
      (error) => {
        console.error('An error occurred loading the logo SVG:', error);
      }
    );

    // ── 7. Animation Loop ────────────────────────────────────────────────────
    let animationFrameId: number;

    const animate = () => {
      animationFrameId = requestAnimationFrame(animate);

      // Rotate slowly on Y-axis
      group.rotation.y += 0.015;

      // Subtle pitch bobbing on X-axis (slow tilt oscillation over time)
      group.rotation.x = Math.sin(Date.now() * 0.0008) * 0.12;

      // Subtle physical float bobbing (Y-axis drift)
      group.position.y = 0.1 + Math.sin(Date.now() * 0.0015) * 0.06;

      // Animate floating ember particles
      const positionsArr = particlesGeometry.attributes.position.array as Float32Array;
      for (let i = 0; i < particlesCount; i++) {
        // Drift upwards
        positionsArr[i * 3 + 1] += speeds[i];
        
        // Sway horizontally
        positionsArr[i * 3] += Math.sin(Date.now() * 0.001 + waveOffsets[i]) * 0.002;

        // Reset particle if it drifts past the top of the viewport
        if (positionsArr[i * 3 + 1] > 4.5) {
          positionsArr[i * 3 + 1] = -4.5;
          positionsArr[i * 3] = (Math.random() - 0.5) * 8;
        }
      }
      particlesGeometry.attributes.position.needsUpdate = true;

      renderer.render(scene, camera);
    };

    animate();

    // ── 8. Responsive Resize Listener ────────────────────────────────────────
    const handleResize = () => {
      if (!containerRef.current) return;
      const w = containerRef.current.clientWidth;
      const h = containerRef.current.clientHeight;

      camera.aspect = w / h;
      camera.updateProjectionMatrix();

      renderer.setSize(w, h);
    };

    window.addEventListener('resize', handleResize);

    // ── 9. Cleanup ───────────────────────────────────────────────────────────
    return () => {
      window.removeEventListener('resize', handleResize);
      cancelAnimationFrame(animationFrameId);

      if (containerRef.current && renderer.domElement) {
        containerRef.current.removeChild(renderer.domElement);
      }

      // Dispose geometries & materials to prevent memory leaks
      group.children.forEach((child) => {
        if (child instanceof THREE.Mesh) {
          child.geometry.dispose();
          if (Array.isArray(child.material)) {
            child.material.forEach((m) => m.dispose());
          } else {
            child.material.dispose();
          }
        }
      });

      // Mouse listener cleanup removed

      // Dispose particles resources
      particlesGeometry.dispose();
      particlesMaterial.dispose();
      pTexture.dispose();

      // Dispose environment map texture
      envTexture.dispose();

      renderer.dispose();
    };
  }, []);

  return (
    <div className="relative w-full h-full flex items-center justify-center">
      {loading && (
        <div className="absolute inset-0 flex items-center justify-center">
          {/* Elegant loader */}
          <div className="w-8 h-8 rounded-full border-2 border-slate-700 border-t-[#b25737] animate-spin" />
        </div>
      )}
      <div
        ref={containerRef}
        className="w-full h-full"
        style={{ background: 'transparent' }}
      />
    </div>
  );
};
