const fs = require('node:fs');
const path = require('node:path');

const projectRoot = path.resolve(__dirname, '..');
const staticDirectory = path.join(projectRoot, 'src', 'main', 'resources', 'static');
const outputDirectory = path.join(projectRoot, 'dist');
const isVercelBuild = process.env.VERCEL === '1';
const configuredUrl = (process.env.VITE_API_BASE_URL || '').trim();

if (isVercelBuild && !configuredUrl) {
    throw new Error('Set VITE_API_BASE_URL in the Vercel project environment variables.');
}

let apiBaseUrl = '';
if (configuredUrl) {
    const parsedUrl = new URL(configuredUrl);
    if (isVercelBuild && parsedUrl.protocol !== 'https:') {
        throw new Error('VITE_API_BASE_URL must use HTTPS for Vercel deployments.');
    }
    apiBaseUrl = parsedUrl.origin;
}

fs.rmSync(outputDirectory, { recursive: true, force: true });
fs.cpSync(staticDirectory, outputDirectory, { recursive: true });
fs.writeFileSync(
    path.join(outputDirectory, 'runtime-config.js'),
    `window.FH_API_BASE = ${JSON.stringify(apiBaseUrl)};\n`
);