package com.snatik.matches.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.snatik.matches.game.Difficulty
import com.snatik.matches.game.GameTheme

/**
 * Best stars and times per theme and difficulty, plus the sound switch.
 *
 * The file name and key formats are those of the originally released app, so players keep their
 * progress when upgrading.
 */
class GamePreferences(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE))

    fun highStars(theme: GameTheme, difficulty: Difficulty): Int =
        prefs.getInt(starsKey(theme.id, difficulty.level), 0)

    /** Best completion time in seconds, or null if the level was never completed. */
    fun bestTimeSeconds(theme: GameTheme, difficulty: Difficulty): Int? =
        prefs.getInt(timeKey(theme.id, difficulty.level), NO_TIME).takeIf { it != NO_TIME }

    /** Average star count over all difficulties, 0..3, used for the theme card art. */
    fun averageStars(theme: GameTheme): Int =
        Difficulty.entries.sumOf { highStars(theme, it) } / Difficulty.entries.size

    fun recordResult(theme: GameTheme, difficulty: Difficulty, stars: Int, passedSeconds: Int) {
        val best = bestTimeSeconds(theme, difficulty)
        prefs.edit {
            if (stars > highStars(theme, difficulty)) {
                putInt(starsKey(theme.id, difficulty.level), stars)
            }
            if (best == null || passedSeconds < best) {
                putInt(timeKey(theme.id, difficulty.level), passedSeconds)
            }
        }
    }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_SOUND_ENABLED, value) }

    companion object {
        const val FILE_NAME = "com.snatik.matches"
        private const val NO_TIME = -1
        private const val KEY_SOUND_ENABLED = "sound_enabled"

        internal fun starsKey(themeId: Int, level: Int) = "theme_${themeId}_difficulty_$level"
        internal fun timeKey(themeId: Int, level: Int) = "themetime_${themeId}_difficultytime_$level"
    }
}
