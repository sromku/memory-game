package com.snatik.matches.game

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import com.snatik.matches.R

/**
 * A card set. [id] is persisted in the score store and used as the score multiplier, so the
 * values must stay as they were in the released app. [characters] are the names of the vector
 * characters in assets/characters. [groundLine] is where feet stand on the background art, as a
 * fraction of the image height.
 */
enum class GameTheme(
    val id: Int,
    @DrawableRes val backgroundRes: Int,
    val groundLine: Float,
    @ArrayRes val cardImagesRes: Int,
    val characters: List<String>,
) {
    ANIMALS(id = 1, R.drawable.back_animals, groundLine = 0.83f, R.array.theme_card_animals, characters("animals", 28)),
    MONSTERS(id = 2, R.drawable.back_horror, groundLine = 0.81f, R.array.theme_card_monsters, characters("mosters", 40)),
    EMOJI(id = 3, R.drawable.background, groundLine = 0.86f, R.array.theme_card_emoji, characters("emoji", 48)),
}

private fun characters(prefix: String, count: Int) = List(count) { "${prefix}_${it + 1}" }
