package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.snatik.matches.R

/** The list of languages inside the settings frame; the current one is gold. */
@SuppressLint("ViewConstructor") // created in code only
class PopupLanguageView(
    context: Context,
    frame: Drawable,
    chosen: String?,
    onPick: (tag: String?) -> Unit,
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        background = frame
        setPadding(
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_left),
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_top),
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_left),
            resources.getDimensionPixelSize(R.dimen.popup_language_padding_bottom),
        )
        val list = LinearLayout(context).apply { orientation = VERTICAL }
        val entries = listOf<Pair<String?, String>>(null to context.getString(R.string.language_phone)) +
            AppLanguages.all.map { it.tag to it.name }
        for ((tag, name) in entries) {
            list.addView(row(name, selected = tag == chosen) { onPick(tag) })
        }
        addView(ScrollView(context).apply { addView(list) }, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
    }

    private fun row(name: String, selected: Boolean, onClick: () -> Unit): TextView = TextView(context).apply {
        text = name
        typeface = ResourcesCompat.getFont(context, R.font.game)
        paint.isFakeBoldText = resources.getBoolean(R.bool.game_text_fake_bold)
        setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.popup_settings_sound_text_size))
        setTextColor(ContextCompat.getColor(context, if (selected) R.color.road_gold else R.color.white))
        setShadowLayer(4f, 4f, 4f, ContextCompat.getColor(context, R.color.text_shadow))
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        val pad = resources.getDimensionPixelSize(R.dimen.popup_language_row_padding)
        setPadding(pad * 3, pad, pad * 3, pad)
        contentDescription = name
        setOnClickListener { onClick() }
    }
}
