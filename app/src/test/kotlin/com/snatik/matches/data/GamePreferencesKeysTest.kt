package com.snatik.matches.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The key formats and file name are a compatibility contract with the version of the game that is
 * on players' devices (versionCode 1007). Changing them would wipe everyone's stars.
 */
class GamePreferencesKeysTest {

    @Test
    fun `file name matches the released app`() {
        assertEquals("com.snatik.matches", GamePreferences.FILE_NAME)
    }

    @Test
    fun `star key matches the released format`() {
        assertEquals("theme_2_difficulty_5", GamePreferences.starsKey(themeId = 2, level = 5))
    }

    @Test
    fun `best time key matches the released format`() {
        assertEquals("themetime_3_difficultytime_1", GamePreferences.timeKey(themeId = 3, level = 1))
    }
}
