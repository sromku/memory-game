# Follow the song

The second mini-game; special rounds take turns through all the mini-games. It trains working
memory.

## What the child sees

A small party of characters stands on the ground. **Listen!**: the characters sing a song, one
note each, lighting up and hopping as they sing. **Your turn!**: the child taps the characters in
the same order; every tap sings that character's note. When the song is repeated the party hops
for joy and the song grows by one note. A wrong tap plays a soft "oops", the song is sung again
from the start, and the child tries again. Nobody ever loses.

A row of dots at the top shows how long the song is and how much of it is already learned.

## Rules

`FollowTheSong` in `game/minigame` is pure Kotlin: the song is drawn once per round (three
notes on round 10, one more every ten rounds), each turn asks for one more note of it, and a
mistake restarts the turn. Stars: no mistakes three, up to two mistakes two, more one. The party
has three characters until round 30 and four from then on. Unit tested.

## Sounds

One pentatonic note per character position, synthesised (numpy, marimba-like, see
tools/generate-sounds.py) into res/raw/note_1..6.wav, plus wrong.wav for the "oops". The party's
notes are spread over the scale so a song of three characters is a chord of C, E and C'.

## Screen

`FollowTheSongFragment` runs the choreography over `PartySceneView`, which gained a spotlight
(a glow and a hop while a character sings) and taps on characters. `SongProgressView` draws the
dots.
