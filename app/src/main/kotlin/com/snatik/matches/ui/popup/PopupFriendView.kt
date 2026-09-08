package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.TextViewCompat
import com.snatik.matches.R
import com.snatik.matches.ui.character.CharacterDrawable

/**
 * One friend from the album in the settings frame: the character big and alive under its name on
 * the ribbon, or, for a friend still to meet, its shadow over a line saying how many rounds away
 * it is ([hint]). Tapping the character makes it hop.
 */
@SuppressLint("ViewConstructor") // created in code only
class PopupFriendView(
    context: Context,
    frame: Drawable,
    private val character: CharacterDrawable,
    hint: String?,
) : LinearLayout(context) {

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        background = frame
        setPadding(0, resources.getDimensionPixelSize(R.dimen.popup_friend_padding_top), 0, 0)
        val image = ImageView(context).apply {
            val size = resources.getDimensionPixelSize(if (hint == null) R.dimen.popup_friend_size else R.dimen.popup_friend_shadow_size)
            layoutParams = LayoutParams(size, size)
            setImageDrawable(character)
            contentDescription = context.getString(if (hint == null) R.string.cd_friend_found else R.string.cd_friend_hidden)
            if (hint == null) {
                setOnClickListener { character.start(); character.hop() }
            } else {
                character.colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                character.alpha = CharacterDrawable.SHADOW_ALPHA
            }
        }
        addView(image)
        if (hint != null) {
            addView(TextView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    val side = resources.getDimensionPixelSize(R.dimen.popup_friend_text_side)
                    setMargins(side, resources.getDimensionPixelSize(R.dimen.popup_friend_text_gap), side, 0)
                }
                typeface = ResourcesCompat.getFont(context, R.font.game)
                text = hint
                gravity = Gravity.CENTER
                maxLines = 2
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setShadowLayer(3f, 3f, 3f, ContextCompat.getColor(context, R.color.text_shadow))
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(this, 11, 17, 1, android.util.TypedValue.COMPLEX_UNIT_DIP)
            })
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        character.start()
    }

    override fun onDetachedFromWindow() {
        character.stop()
        super.onDetachedFromWindow()
    }
}
