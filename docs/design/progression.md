# Progression

Each difficulty is a road of 40 rounds. Finishing a round earns up to three stars; the road remembers
the best stars and best time of every round. The next difficulty opens once 10 rounds of the
previous one are done, so a young child is never dropped onto a big board. Nothing is ever locked
behind anything but play.

## Rounds

A round is fully described by its difficulty and its index (1..40), see `RoundSpec`:

- **Board**: the difficulty's card count and grid, unchanged.
- **Time**: the difficulty's time at round 1, tightening steadily to 70% of it by round 40. Stars
  keep the same thresholds relative to the round's time.
- **Special** rounds, every fifth (5, 10, ... 40), are reserved for mini-games and play as normal
  rounds until those exist.
- **Theme** stays the player's choice; it is the skin of the road, not part of progression.

## Progress

`Progress` is an immutable value: a map from (difficulty, index) to the best result of that round.
Everything else is derived: rounds completed on a difficulty, stars on a difficulty, whether a
difficulty is unlocked, the next round to play, the round quick play should pick (the next round of
the highest unlocked difficulty that still has rounds left).

## Storage

`ProgressStore` keeps the progress in one small text file in the app's private storage:

```
memory-game-progress 1
<difficulty level> <round index> <stars> <best time seconds or ->
```

Writes go to a temporary file first and are renamed into place, so a crash mid-write cannot leave a
half-written file. A file that cannot be read is treated as empty progress, never as a crash.

## Migration

Players of version 1.x have best stars and times per theme and difficulty in SharedPreferences.
On the first start with the new store, the best of each difficulty across themes becomes the
result of that difficulty's round 1, so their stars stay visible on the road.

## Level map

Each difficulty has its own map screen, reached from the difficulty buttons: the road winds from
left to right as a gentle wave, one badge per round. Done rounds are blue and carry their stars and
can be replayed; the next round is gold, breathes, and waits under a bobbing arrow; the rest are
grey. Special rounds are eight-pointed badges. The map opens centred on the next round; after a
round is played it opens on that round, pops its stars in, then travels on to the next one.

Difficulty buttons show the road's rounded mean stars and "done / 40"; a road that has not opened
yet is grey with a padlock and shakes its head when tapped. The clock of a round is the round's
own time, so later rounds are judged more strictly.

The map is a single custom view: `RoadGeometry` (pure) places the rounds, `RoadNode` (pure) says
what state each is in, `RoadMapView` paints, scrolls and animates.

## What ships when

- Milestone 1 (this): model, store, migration, recording of results. Screens unchanged.
- Milestone 2 (this): the level map, per-round time, special rounds marked on the map.
- Milestone 3: quick play and the celebration between rounds.
