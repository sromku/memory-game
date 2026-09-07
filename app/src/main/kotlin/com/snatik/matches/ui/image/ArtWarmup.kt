package com.snatik.matches.ui.image

import android.content.res.Resources
import com.snatik.matches.R
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameResult
import com.snatik.matches.game.GameTheme
import com.snatik.matches.game.progression.Progress

/** Which art the coming screens will ask for, given where the player is, in the order they will ask. */
object ArtWarmup {

    fun plan(resources: Resources, progress: Progress, themeStars: (GameTheme) -> Int): List<Int> = buildList {
        add(R.drawable.button_start)
        add(R.drawable.button_map)
        add(R.drawable.button_settings)
        add(R.drawable.tooltip_play)
        add(R.drawable.button_back)
        for (theme in GameTheme.entries) add(typedArrayId(resources, theme.cardImagesRes, themeStars(theme)))
        for (difficulty in Difficulty.entries) {
            val stars = if (progress.isUnlocked(difficulty)) progress.averageStars(difficulty) else 0
            add(typedArrayId(resources, R.array.difficulty_buttons, (difficulty.level - 1) * (GameResult.MAX_STARS + 1) + stars))
        }
        add(R.drawable.level_complete_star)
        add(R.drawable.time_bar)
        add(R.drawable.level_complete)
        add(R.drawable.button_again)
        add(R.drawable.settings_popup)
        add(R.drawable.button_music_on)
        add(R.drawable.button_music_off)
        add(R.drawable.button_rate)
    }

    private fun typedArrayId(resources: Resources, arrayRes: Int, index: Int): Int {
        val array = resources.obtainTypedArray(arrayRes)
        try {
            return array.getResourceId(index, 0)
        } finally {
            array.recycle()
        }
    }
}
