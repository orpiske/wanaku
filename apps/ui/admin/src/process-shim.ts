if (typeof globalThis.process === "undefined") {
  (globalThis as Record<string, unknown>).process = { env: { NODE_ENV: "production" } };
}
