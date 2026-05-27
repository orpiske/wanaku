import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  base: '/kaoto/',
  server: {
    port: 5174,
  },
  build: {
    outDir: 'dist',
  },
  css: {
    preprocessorOptions: {
      scss: {
        loadPaths: [resolve(__dirname, 'node_modules')],
      },
    },
  },
  resolve: {
    alias: {
      '~@patternfly': resolve(__dirname, 'node_modules/@patternfly'),
      '@kaoto/kaoto/lib/providers': resolve(__dirname, 'node_modules/@kaoto/kaoto/lib/providers/index.js'),
      '@kaoto/kaoto/lib/dynamic-catalog/catalog.provider': resolve(__dirname, 'node_modules/@kaoto/kaoto/lib/dynamic-catalog/catalog.provider.js'),
      '@kaoto/kaoto/lib/components/Visualization/Canvas/controller.service': resolve(__dirname, 'node_modules/@kaoto/kaoto/lib/components/Visualization/Canvas/controller.service.js'),
      '@kaoto/kaoto/lib/utils': resolve(__dirname, 'node_modules/@kaoto/kaoto/lib/utils/index.js'),
    },
  },
});
