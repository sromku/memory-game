# Shadow match, Odd one out

Two small party games on the special rounds.

**Shadow match.** A character stands on the ground as a black shadow; three cards show characters,
one of them its owner. The right card makes the shadow turn into the character, who hops; a wrong
card shakes. Three shadows a round, four from round 50. `ShadowMatch` holds the rules. The shadow
is the character's own drawable under a black colour filter, so every silhouette is exact.

**Odd one out.** A row of four (five from round 50) of the same character, except one: another
character, or from round 30 sometimes the same character in the wrong colours (its colour
channels rotated). The child taps the odd one; it lights up and everyone hops. `OddOneOut` holds
the rules. Trains attention rather than memory, which suits the age.

Both are unit tested and sit on `PartyGameFragment`.
