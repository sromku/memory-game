# Friends album

Every ten rounds done on a theme's roads, one of the theme's characters joins the album, in an
order fixed per theme (`Friends.order`, seeded by the theme), so a friend once met never changes.
The album is derived from `Progress` (`friendsOf`), nothing extra is stored.

The album button on the menu opens the album: one section per theme with every character as a
card, collected ones in colour, the rest as shadows. The count in the title is friends met over
all characters, and a line under it says the rule: a new friend every ten rounds.

Tapping a collected friend opens a popup in the settings frame with the character big and alive
and its name on the ribbon (`names_<theme>` string arrays, translated; the monsters have invented
names that are the same everywhere). Tapping a shadow opens the same popup with the shadow and
"N more rounds and we meet": `Progress.roundsToFriend` counts the rounds to the next friend on the
unlocked road closest to giving one, plus ten for every friend queued ahead in the theme's order.
It is the fewest rounds that can do it; playing on several roads at once takes more. The animals collected also become the menu's visitors and
the friend of the day; before three are collected, the other animals fill in.
