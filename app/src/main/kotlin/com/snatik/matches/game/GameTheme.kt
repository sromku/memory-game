package com.snatik.matches.game

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import com.snatik.matches.R

/**
 * A card set. [id] is persisted in the score store and used as the score multiplier, so the
 * values must stay as they were in the released app.
 */
enum class GameTheme(
    val id: Int,
    @DrawableRes val backgroundRes: Int,
    @ArrayRes val tileImagesRes: Int,
    @ArrayRes val cardImagesRes: Int,
) {
    ANIMALS(id = 1, R.drawable.back_animals, R.array.tiles_animals, R.array.theme_card_animals),
    MONSTERS(id = 2, R.drawable.back_horror, R.array.tiles_monsters, R.array.theme_card_monsters),
    EMOJI(id = 3, R.drawable.background, R.array.tiles_emoji, R.array.theme_card_emoji),
}
