import React, { useEffect, useRef, useState } from 'react';
import * as THREE from 'three';
import { SVGLoader } from 'three/examples/jsm/loaders/SVGLoader.js';

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
    
    const camera = new THREE.PerspectiveCamera(40, width / height, 0.1, 1000);
    camera.position.set(0, 0, 12);

    // ── 3. Renderer Setup ────────────────────────────────────────────────────
    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    renderer.setSize(width, height);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    renderer.shadowMap.enabled = true;
    containerRef.current.appendChild(renderer.domElement);

    // ── 4. High-Contrast Studio Lighting (Chrome Reflections) ────────────────
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.5);
    scene.add(ambientLight);

    // Key Light: Bright white from top-right
    const keyLight = new THREE.DirectionalLight(0xffffff, 3.5);
    keyLight.position.set(6, 6, 8);
    scene.add(keyLight);

    // Fill Light: Warm Mediterranean terracotta bounce from bottom-left
    const terracottaFill = new THREE.DirectionalLight(0xb25737, 2.5);
    terracottaFill.position.set(-6, -4, 4);
    scene.add(terracottaFill);

    // Rim Light: Aegean blue accent from top-left-back for edge highlights
    const rimLight = new THREE.DirectionalLight(0x1a5e8f, 2.0);
    rimLight.position.set(-4, 5, -6);
    scene.add(rimLight);

    // Specular Catch: Pure white point light directly in front
    const pointLight = new THREE.PointLight(0xffffff, 4.0, 50);
    pointLight.position.set(0, 0, 10);
    scene.add(pointLight);

    // ── 5. Extruded 3D Mesh Group ────────────────────────────────────────────
    const group = new THREE.Group();
    scene.add(group);

    // Load and Parse SVG
    const loader = new SVGLoader();
    loader.load(
      '/logo.svg',
      (data) => {
        const paths = data.paths;
        
        // Shiny Chrome Material settings
        const chromeMaterial = new THREE.MeshStandardMaterial({
          color: 0xf1f5f9,      // Clean silver/white base
          metalness: 1.0,       // Fully metallic
          roughness: 0.08,      // Mirror-like reflection smoothness
          bumpScale: 0.05,
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
        const scale = 7.5 / maxDim;
        group.scale.set(scale, -scale, scale);

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
      
      // Rotate the group on Y-axis
      group.rotation.y += 0.015;
      
      // Subtle pitch bobbing on X-axis for a floating feel
      group.rotation.x = Math.sin(Date.now() * 0.001) * 0.15;
      
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
