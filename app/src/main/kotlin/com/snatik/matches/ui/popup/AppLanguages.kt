package com.snatik.matches.ui.popup

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/** The languages the app is translated into, each named in itself, and the player's current choice. */
object AppLanguages {

    class Language(val tag: String, val name: String)

    val all: List<Language> = listOf(
        Language("en", "English"),
        Language("es", "Español"),
        Language("pt-BR", "Português (Brasil)"),
        Language("fr", "Français"),
        Language("de", "Deutsch"),
        Language("it", "Italiano"),
        Language("nl", "Nederlands"),
        Language("pl", "Polski"),
        Language("tr", "Türkçe"),
        Language("ru", "Русский"),
        Language("uk", "Українська"),
        Language("ar", "العربية"),
        Language("hi", "हिन्दी"),
        Language("th", "ไทย"),
        Language("vi", "Tiếng Việt"),
        Language("id", "Bahasa Indonesia"),
        Language("ms", "Bahasa Melayu"),
        Language("ja", "日本語"),
        Language("ko", "한국어"),
        Language("zh-CN", "简体中文"),
        Language("zh-TW", "繁體中文"),
    )

    /** The tag chosen in the game, or null when the phone's language is followed. */
    val chosen: String?
        get() = AppCompatDelegate.getApplicationLocales().toLanguageTags().takeIf { it.isNotEmpty() }

    /** Switches the app's language; null returns to the phone's. The activity is recreated by AppCompat. */
    fun choose(tag: String?) {
        AppCompatDelegate.setApplicationLocales(if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
    }
}
