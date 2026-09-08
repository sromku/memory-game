#!/usr/bin/env python3
"""Generates the art of a new world with the OpenAI image model, in the style of the existing tiles:
one square transparent PNG per character into art/original/tiles/<world>_<n>.png (400 x 400) and
a 1024 x 512 background into art/original/back_<world>.png. Everything else (tracing, cards,
densities) is the art pipeline's job afterwards.

The key is read from the file named by OPENAI_KEY_FILE (a line "OPENAI-KEY=sk-..."); it is never
printed. Usage, from the repository root:

    OPENAI_KEY_FILE=~/keys python3 tools/generate-world.py ocean "clownfish" "crab" ...

Generation happens here, on a computer; the app never talks to any server."""
import argparse, base64, concurrent.futures, io, json, os, re, sys, time, urllib.request, uuid

API = 'https://api.openai.com/v1/images/edits'
MODEL = 'gpt-image-1'
REFERENCES = ['art/original/tiles/animals_1.png', 'art/original/tiles/animals_5.png', 'art/original/tiles/animals_12.png']
BACKGROUND_REFERENCE = 'art/original/back_animals.png'

CHARACTER_PROMPT = (
    'A cute cartoon {creature} as a game character, drawn exactly in the style of the reference '
    'pictures: flat vector shapes, rounded and chubby, no outlines, two to four flat saturated '
    'colours, a very small soft grey drop shadow directly under it, big round white eyes with '
    'plain black pupils, a friendly smile, facing the viewer, centred, filling most of the square, '
    'on a fully transparent background, nothing else in the picture, no text.'
)
BACKGROUND_PROMPT = (
    'A bright cheerful underwater scenery for a children\'s game, drawn exactly in the style of the '
    'reference picture: flat vector shapes, soft colours, a light blue sea with rays of light and a '
    'few bubbles at the top, rounded coral and seaweed in the middle distance, and a flat sandy sea '
    'floor as a band across the whole bottom fifth of the picture with a straight top edge, '
    'nothing standing on the sand, no characters, no text.'
)


def key():
    path = os.path.expanduser(os.environ.get('OPENAI_KEY_FILE', '~/keys'))
    m = re.search(r'sk-[A-Za-z0-9_\-]{20,}', open(path).read())
    if not m:
        sys.exit(f'no OpenAI key in {path}')
    return m.group(0)


def multipart(fields, files):
    boundary = uuid.uuid4().hex
    body = io.BytesIO()
    for name, value in fields.items():
        body.write(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    for name, path in files:
        body.write(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"; filename="{os.path.basename(path)}"\r\nContent-Type: image/png\r\n\r\n'.encode())
        body.write(open(path, 'rb').read())
        body.write(b'\r\n')
    body.write(f'--{boundary}--\r\n'.encode())
    return body.getvalue(), f'multipart/form-data; boundary={boundary}'


def generate(prompt, references, size, background, api_key, attempts=3):
    fields = {'model': MODEL, 'prompt': prompt, 'size': size, 'quality': 'medium', 'n': '1', 'output_format': 'png'}
    if background:
        fields['background'] = background
    data, content_type = multipart(fields, [('image[]', r) for r in references])
    for attempt in range(attempts):
        req = urllib.request.Request(API, data=data, headers={'Authorization': 'Bearer ' + api_key, 'Content-Type': content_type})
        try:
            with urllib.request.urlopen(req, timeout=300) as r:
                return base64.b64decode(json.load(r)['data'][0]['b64_json'])
        except Exception as e:  # rate limits and hiccups: wait and try again
            if attempt == attempts - 1:
                raise
            time.sleep(10 * (attempt + 1))


def clean_alpha(image, threshold=48, halo=128, reach=4):
    """Drops the haze the model leaves around a transparent picture, which would trace as a pale
    rectangle on the card: everything fainter than [threshold], and everything fainter than [halo]
    that is not within [reach] pixels of solid paint. Edges keep their anti-aliasing."""
    import numpy as np
    from PIL import Image, ImageFilter
    a = np.array(image)
    alpha = a[..., 3]
    solid = Image.fromarray(((alpha >= 200) * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(2 * reach + 1))
    near = np.array(solid) > 0
    a[(alpha < threshold) | ((alpha < halo) & ~near)] = 0
    return Image.fromarray(a, 'RGBA')


def character(world, index, creature, api_key):
    from PIL import Image
    out = f'art/original/tiles/{world}_{index}.png'
    if os.path.exists(out):
        return f'{out} exists'
    png = generate(CHARACTER_PROMPT.format(creature=creature), REFERENCES, '1024x1024', 'transparent', api_key)
    image = clean_alpha(Image.open(io.BytesIO(png)).convert('RGBA'))
    image.resize((400, 400), Image.LANCZOS).save(out)
    return f'{out} ({creature})'


def background(world, api_key):
    from PIL import Image
    out = f'art/original/back_{world}.png'
    if os.path.exists(out):
        return f'{out} exists'
    png = generate(BACKGROUND_PROMPT, [BACKGROUND_REFERENCE], '1536x1024', None, api_key)
    image = Image.open(io.BytesIO(png)).convert('RGB')
    # 3:2 to 2:1: keep the full width, take the band that holds the sea floor at the bottom
    w, h = image.size
    band = int(w / 2)
    image.crop((0, h - band, w, h)).resize((1024, 512), Image.LANCZOS).save(out)
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('world')
    parser.add_argument('creatures', nargs='+')
    parser.add_argument('--workers', type=int, default=4)
    parser.add_argument('--no-background', action='store_true')
    args = parser.parse_args()
    api_key = key()
    os.makedirs('art/original/tiles', exist_ok=True)
    jobs = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as pool:
        if not args.no_background:
            jobs.append(pool.submit(background, args.world, api_key))
        for index, creature in enumerate(args.creatures, start=1):
            jobs.append(pool.submit(character, args.world, index, creature, api_key))
        for job in concurrent.futures.as_completed(jobs):
            try:
                print(job.result(), flush=True)
            except Exception as e:
                print('failed:', str(e)[:200], flush=True)


if __name__ == '__main__':
    main()
