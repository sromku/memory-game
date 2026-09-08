# Who was here?

The first mini-game; special rounds (every fifth) take turns through all the mini-games. It trains visual
recall rather than the card game's spatial memory, with the same characters, ground and sounds.

## What the child sees

1. **Look!** A party of characters pops up onto the ground one after another and stands there
   idling. The clock counts a few seconds down.
2. **Hide.** Everyone drops into the ground.
3. **Who is missing?** The party comes back, one short; a bobbing "?" marks the empty spot.
   Three cards appear at the top: the missing character and two strangers who were not at the party.
4. Tapping the right card sends that character hopping back into the gap and everyone celebrates;
   a wrong card shakes its head and the child simply tries again. Nobody ever loses.

Three turns make a round. Stars come from mistakes: none gives three stars, one gives two, more
gives one. The "level complete" popup and the road behave exactly as after a card round.

## Rules

`WhoWasHere` in `game/minigame` is pure Kotlin: the party grows with the round (3 characters on
rounds 5 and 10, then one more every ten rounds, up to 6), the choices always hold the missing
character once plus strangers, and every turn draws a fresh party. It is unit tested.

## Screen

`WhoWasHereFragment` runs the choreography as one coroutine over the rules; `PartySceneView`
paints the party on the theme's ground line (see `CenterCrop`) and animates entrances, hiding, the
"?" marker and the return. Choices are card frames with the characters drawn as on the board.

Special rounds show a "?" on the map instead of their number.
