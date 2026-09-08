# What changed?

The third mini-game. A party stands on the ground; the child looks for a few seconds; everyone
ducks into the ground and comes back, and one thing is different: either two characters have
swapped places, or one of them has been replaced by a stranger. The child taps who changed (for a
swap, either of the two). A wrong tap makes that character shake its head and the child tries
again; the right one lights up and everyone hops.

Three turns make a round; stars come from mistakes as in the other games. The party has three
characters at first, one more every twenty rounds, up to five. `WhatChanged` in `game/minigame`
holds the rules and is unit tested; the screen is `WhatChangedFragment` on `PartyGameFragment`,
the base every party game shares (scene, prompt, countdown, cards, taps).
