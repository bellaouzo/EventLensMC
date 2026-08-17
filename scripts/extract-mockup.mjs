import fs from 'fs';

const c = fs.readFileSync('c:/Users/gregp/Downloads/EventLens Dashboard (standalone).html', 'utf8');
const marker = '"<!DOCTYPE html>\\n';
const idx = c.indexOf(marker);
if (idx < 0) {
  console.error('no template');
  process.exit(1);
}
let pos = idx + 1;
while (pos < c.length) {
  if (c[pos] === '\\') {
    pos += 2;
    continue;
  }
  if (c[pos] === '"') {
    break;
  }
  pos++;
}
const raw = c.slice(idx, pos + 1);
const html = JSON.parse(raw);
const styleMatch = html.match(/<style>([\s\S]*?)<\/style>/);
if (!styleMatch) {
  console.error('no style');
  process.exit(1);
}
const css = styleMatch[1];
const root = css.match(/:root\s*\{[^}]+\}/);
console.log('=== :root ===');
console.log(root ? root[0] : 'none');
const classNames = [...css.matchAll(/\.([a-zA-Z][a-zA-Z0-9_-]*)\s*[,{]/g)].map((m) => m[1]);
const unique = [...new Set(classNames)].sort();
console.log('\n=== CLASSES (' + unique.length + ') ===');
console.log(unique.slice(0, 120).join('\n'));
const outCss = css.replace(/@font-face[\s\S]*?\}/g, '/* font-face omitted */');
fs.writeFileSync('scripts/mockup-extracted.css', outCss);
const bodyMatch = html.match(/<x-dc>([\s\S]*?)<\/x-dc>/);
if (bodyMatch) {
  fs.writeFileSync('scripts/mockup-extracted-body.html', bodyMatch[1].slice(0, 20000));
  console.log('\n=== BODY written ===');
}
