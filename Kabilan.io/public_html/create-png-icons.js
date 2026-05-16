// Node.js script to create PNG icons
// Run with: node create-png-icons.js

const fs = require('fs');
const { createCanvas } = require('canvas');

function createIcon(size) {
    const canvas = createCanvas(size, size);
    const ctx = canvas.getContext('2d');
    
    // Background gradient
    const gradient = ctx.createLinearGradient(0, 0, size, size);
    gradient.addColorStop(0, '#010103');
    gradient.addColorStop(0.5, '#1c1c21');
    gradient.addColorStop(1, '#2a2a35');
    
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, size, size);
    
    // Add border
    ctx.strokeStyle = '#3b82f6';
    ctx.lineWidth = Math.max(2, size * 0.008);
    ctx.strokeRect(0, 0, size, size);
    
    // Add inner glow
    const innerGradient = ctx.createRadialGradient(size/2, size/2, 0, size/2, size/2, size/2);
    innerGradient.addColorStop(0, 'rgba(59, 130, 246, 0.1)');
    innerGradient.addColorStop(1, 'rgba(59, 130, 246, 0)');
    ctx.fillStyle = innerGradient;
    ctx.fillRect(0, 0, size, size);
    
    // Draw "K" letter
    ctx.fillStyle = '#ffffff';
    ctx.font = `bold ${size * 0.55}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('K', size / 2, size / 2);
    
    return canvas;
}

// Create icons
const sizes = [72, 96, 128, 144, 152, 192, 384, 512];

sizes.forEach(size => {
    const canvas = createIcon(size);
    const buffer = canvas.toBuffer('image/png');
    fs.writeFileSync(`./assets/icon-${size}x${size}.png`, buffer);
    console.log(`Created icon-${size}x${size}.png`);
});

console.log('All icons created successfully!');
