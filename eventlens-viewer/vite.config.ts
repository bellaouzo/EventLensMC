import { defineConfig } from 'vite';

export default defineConfig({
  base: './',
  build: {
    outDir: '../src/main/resources/dashboard',
    emptyOutDir: true,
    assetsDir: 'assets',
  },
});
