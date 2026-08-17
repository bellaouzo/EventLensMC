import fs from 'fs';

const c = fs.readFileSync('c:/Users/gregp/Downloads/EventLens Dashboard (standalone).html', 'utf8');
const marker = '"<!DOCTYPE html>\\n';
const idx = c.indexOf(marker);
let pos = idx + 1;
while (pos < c.length) {
  if (c[pos] === '\\') {
    pos += 2;
    continue;
  }
  if (c[pos] === '"') break;
  pos++;
}
const html = JSON.parse(c.slice(idx, pos + 1));
const body = html.match(/<x-dc>([\s\S]*?)<\/x-dc>/)[1];
const style2 = body.match(/<style>\s*\n:root\{([\s\S]*?)<\/style>/);
console.log(':root content length', style2 ? style2[1].length : 0);

const snippets = [
  'Top offenders',
  'Recent trace feed',
  'navOverview',
  'border-left:3px',
  'surface-hover',
  'Timeline',
  'Flame graph',
  'priority',
  'pill',
  'LOWEST',
];
for (const s of snippets) {
  const i = body.indexOf(s);
  console.log(s, i >= 0 ? body.slice(Math.max(0, i - 80), i + 400).replace(/\n/g, ' ').slice(0, 480) : 'NOT FOUND');
  console.log('---');
}
fs.writeFileSync('scripts/mockup-full-body.html', body);
