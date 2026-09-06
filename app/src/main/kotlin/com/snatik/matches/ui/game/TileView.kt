package com.snatik.matches.ui.game

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.snatik.matches.databinding.TileViewBinding
import com.snatik.matches.ui.character.CharacterDrawable

/** One card: a face-down back and the picture underneath, switched with a 3D flip. */
class TileView(context: Context) : FrameLayout(context) {

    private val binding = TileViewBinding.inflate(LayoutInflater.from(context), this)

    var isFaceUp: Boolean = false
        private set

    private var character: CharacterDrawable? = null

    /** A vector character instead of a bitmap: it idles while face up and hops when matched. */
    fun setCharacter(drawable: CharacterDrawable) {
        character = drawable
        binding.image.setImageDrawable(drawable)
        if (isFaceUp) drawable.start()
    }

    fun celebrate() = character?.hop()

    /** Scales the star on the card back with the card, so it reads the same on phones and tablets. */
    fun setTileSize(sizePx: Int) {
        val density = resources.displayMetrics.density
        val star = (sizePx * BACK_STAR_FRACTION).toInt().coerceIn((24 * density).toInt(), (90 * density).toInt())
        binding.backStar.updateLayoutParams { width = star }
    }

    fun flipUp() {
        if (isFaceUp) return
        isFaceUp = true
        startAnimation(FlipAnimation(from = binding.imageTop, to = binding.image, forward = true))
        character?.start()
    }

    fun flipDown() {
        if (!isFaceUp) return
        isFaceUp = false
        startAnimation(FlipAnimation(from = binding.image, to = binding.imageTop, forward = false))
        character?.stop()
    }

    override fun onDetachedFromWindow() {
        character?.stop()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val BACK_STAR_FRACTION = 0.4f
    }

    /** Shows the picture immediately, used when the board is rebuilt mid-round. */
    fun showFaceUp() {
        isFaceUp = true
        binding.imageTop.isVisible = false
        binding.image.isVisible = true
        character?.start()
    }
}
