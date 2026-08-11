import react from "@vitejs/plugin-react";
import {defineConfig} from "vite";

export default defineConfig({
  plugins: [react()],
  define: {
    "process.env.NODE_ENV": JSON.stringify("production"),
    VITE_API_URL: JSON.stringify(process.env.VITE_API_URL ?? ""),
    VITE_BACKEND_URL: JSON.stringify(process.env.VITE_BACKEND_URL ?? ""),
  },
  build: {
    lib: {
      entry: "src/plugin.tsx",
      formats: ["es"],
      fileName: () => "plugin.js",
    },
    outDir: "plugin-dist",
    emptyOutDir: true,
    cssCodeSplit: false,
    rollupOptions: {
      output: {
        assetFileNames: (assetInfo) => {
          if (assetInfo.name?.endsWith(".css")) {
            return "plugin.css";
          }
          return assetInfo.name || "asset-[hash]";
        },
        inlineDynamicImports: true,
      },
    },
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: "modern-compiler",
      },
    },
  },
});
