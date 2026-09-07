#!/usr/bin/env python3
"""Synthesises the mini-game sounds into app/src/main/res/raw: six pentatonic marimba-like notes
and a soft "oops". Needs numpy. Run from the repository root."""
import numpy as np, wave, os

SR = 22050
OUT = 'app/src/main/res/raw'


def write(name, samples):
    samples = np.clip(samples, -1, 1)
    with wave.open(f'{OUT}/{name}.wav', 'wb') as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((samples * 32767).astype('<i2').tobytes())
    print(name, os.path.getsize(f'{OUT}/{name}.wav'))


# A soft marimba-like note: the fundamental, a quick-fading octave, and a click of the fourth harmonic.
for i, semitones in enumerate([0, 2, 4, 7, 9, 12]):
    f = 261.63 * 2 ** (semitones / 12) * 2
    d = 0.7
    t = np.linspace(0, d, int(SR * d), endpoint=False)
    env = np.exp(-t * 5.5) * (1 - np.exp(-t * 400))
    tone = (np.sin(2 * np.pi * f * t)
            + 0.35 * np.sin(2 * np.pi * 2 * f * t) * np.exp(-t * 9)
            + 0.2 * np.sin(2 * np.pi * 4 * f * t) * np.exp(-t * 25))
    write(f'note_{i + 1}', 0.8 * env * tone / 1.55)

# "Oops": two short low tones stepping down.
d = 0.42
t = np.linspace(0, d, int(SR * d), endpoint=False)
f = np.where(t < 0.2, 196.0, 164.8)
phase = np.cumsum(2 * np.pi * f / SR)
env = np.where(t < 0.2, np.sin(np.pi * t / 0.2), np.sin(np.pi * (t - 0.2) / 0.22)) ** 0.8
write('wrong', 0.55 * env * (np.sin(phase) + 0.3 * np.sin(2 * phase)) / 1.3
