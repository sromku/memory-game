# Shopping list, Peek and find

**Shopping list.** A row of five characters (six from round 50). Cards show two of them (three
from round 30) for a few seconds, then go away, and the child taps those
characters in the row in the same order. A right tap lights the character; a wrong one shakes it
and the list is shown again. The dots at the top count the list. Sequence memory, the visual twin
of "Follow the song". `ShoppingList` holds the rules; the screen sits on `PartyGameFragment`.

**Peek and find.** The round's board of cards is dealt and shown face up for a few seconds, then
turned over. The top asks "Where is…" with the picture; a right card stays open and hops, a wrong
one shows itself and closes again. Three pictures a round, four from round 50. It reuses the
board and the card flips of the main game; `PeekAndFind` holds the rules over a `Board`.

Both are unit tested.
