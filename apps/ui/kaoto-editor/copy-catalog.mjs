import { cpSync, existsSync, mkdirSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const src = resolve(__dirname, 'node_modules', '@kaoto', 'camel-catalog', 'dist', 'camel-catalog');
const dest = resolve(__dirname, 'public', 'camel-catalog');

if (!existsSync(src)) {
  console.warn('Warning: @kaoto/camel-catalog dist not found at', src);
  process.exit(0);
}

mkdirSync(dest, { recursive: true });
cpSync(src, dest, { recursive: true });
console.log('Copied camel-catalog to public/camel-catalog');
