#!/usr/bin/env python3
"""Writes app/src/main/res/values-<locale>/strings.xml and phrases.xml from translations_*.py.
Run from the repository root after editing a translation; the English source is values/strings.xml."""
import glob, importlib.util, os, re
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
S = os.path.dirname(os.path.abspath(__file__))
os.chdir(ROOT)
R = 'app/src/main/res/'
LOCALES = ['es', 'pt-rBR', 'fr', 'de', 'it', 'ru', 'uk', 'pl', 'nl', 'tr', 'ar', 'hi', 'id', 'vi', 'th', 'ja', 'ko', 'zh-rTW', 'zh-rCN', 'ms']

def write(p, s):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, 'w').write(s)

# ---------------------------------------------------------------- generate values-<locale> from the data files
def esc(s):
    return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace("'", "\\'").replace('"', '\\"')

english = open(R + 'values/strings.xml').read()
keys = re.findall(r'<string name="([a-z_]+)">', english)  # translatable ones only (the others carry translatable="false")

data = {}
for path in sorted(glob.glob(f'{S}/translations_*.py')):
    spec = importlib.util.spec_from_file_location('t', path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    data.update(mod.LANGS)

missing = [l for l in LOCALES if l not in data]
if missing:
    print('no data yet for', missing)

for loc in LOCALES:
    if loc not in data:
        continue
    t = data[loc]
    absent = [k for k in keys if k not in t['strings']]
    assert not absent, (loc, absent)
    assert len(t['phrases']) == 100, (loc, len(t['phrases']))
    assert len(t['greetings']) == 7, (loc, len(t['greetings']))
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources xmlns:tools="http://schemas.android.com/tools">']
    for k in keys:
        lines.append(f'    <string name="{k}">{esc(t["strings"][k])}</string>')
    lines.append('    <plurals name="cd_round_done" tools:ignore="MissingQuantity,UnusedQuantity">')
    for q, text in t['plurals'].items():
        lines.append(f'        <item quantity="{q}">{esc(text)}</item>')
    lines.append('    </plurals>')
    lines.append('</resources>')
    folder = {'id': 'in'}.get(loc, loc)
    write(R + f'values-{folder}/strings.xml', '\n'.join(lines) + '\n')
    plines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources xmlns:tools="http://schemas.android.com/tools">',
              '    <string-array name="animal_phrases" tools:ignore="Typos">']
    plines += [f'        <item>{esc(p)}</item>' for p in t['phrases']]
    plines += ['    </string-array>', '    <string-array name="animal_greetings">']
    plines += [f'        <item>{esc(p)}</item>' for p in t['greetings']]
    plines += ['    </string-array>', '</resources>']
    write(R + f'values-{folder}/phrases.xml', '\n'.join(plines) + '\n')
    print('wrote', loc)
