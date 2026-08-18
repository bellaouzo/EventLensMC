import { defineConfig } from 'vite';

export default defineConfig({
  base: './',
  build: {
    outDir: '../eventlens-paper/src/main/resources/dashboard',
    emptyOutDir: true,
    assetsDir: 'assets',
    modulePreload: false,
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        format: 'iife',
        inlineDynamicImports: true,
        entryFileNames: 'assets/index.js',
        assetFileNames: 'assets/[name][extname]',
      },
    },
  },
});
