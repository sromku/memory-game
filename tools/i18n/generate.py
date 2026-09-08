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
keys = re.findall(r'<string name="([a-z_0-9]+)">', english)  # translatable ones only (the others carry translatable="false")

data = {}
for path in sorted(glob.glob(f'{S}/translations_*.py')):
    spec = importlib.util.spec_from_file_location('t', path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    data.update(mod.LANGS)

names_spec = importlib.util.spec_from_file_location('names', f'{S}/names.py')
names_mod = importlib.util.module_from_spec(names_spec)
names_spec.loader.exec_module(names_mod)
NAMES, EXTRA, MONSTERS = names_mod.NAMES, names_mod.EXTRA, names_mod.MONSTERS


def names_xml(names, monsters=None):  # the monsters' names are not translated: default resources only
    lines = ['<?xml version="1.0" encoding="utf-8"?>', '<resources>']
    for theme, key in (('animals', 'animals'), ('mosters', None), ('emoji', 'emoji'), ('ocean', 'ocean')):
        if key is None and monsters is None:
            continue
        items = monsters if key is None else names[key]
        attr = ' translatable="false"' if key is None else ''
        lines.append(f'    <string-array name="names_{theme}"{attr}>')
        lines += [f'        <item>{esc(n)}</item>' for n in items]
        lines.append('    </string-array>')
    lines.append('</resources>')
    return '\n'.join(lines) + '\n'


write(R + 'values/names.xml', names_xml(NAMES['en'], MONSTERS))

missing = [l for l in LOCALES if l not in data]
if missing:
    print('no data yet for', missing)

for loc in LOCALES:
    if loc not in data or 'strings' not in data[loc]:
        continue
    t = dict(data[loc])
    t['strings'] = dict(t['strings'], **EXTRA[loc]['strings'])
    for theme in ('animals', 'emoji', 'ocean'):
        assert len(NAMES[loc][theme]) == len(NAMES['en'][theme]), (loc, theme, len(NAMES[loc][theme]))
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
    lines.append('    <plurals name="friend_rounds_left" tools:ignore="MissingQuantity,UnusedQuantity">')
    for q, text in EXTRA[loc]['plurals'].items():
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
    write(R + f'values-{folder}/names.xml', names_xml(NAMES[loc]))
    print('wrote', loc)
