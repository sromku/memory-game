package com.snatik.matches.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.snatik.matches.databinding.TileViewBinding

/** One card: a face-down back and the picture underneath, switched with a 3D flip. */
class TileView(context: Context) : FrameLayout(context) {

    private val binding = TileViewBinding.inflate(LayoutInflater.from(context), this)

    var isFaceUp: Boolean = false
        private set

    fun setImage(bitmap: Bitmap) = binding.image.setImageBitmap(bitmap)

    fun flipUp() {
        if (isFaceUp) return
        isFaceUp = true
        startAnimation(FlipAnimation(from = binding.imageTop, to = binding.image, forward = true))
    }

    fun flipDown() {
        if (!isFaceUp) return
        isFaceUp = false
        startAnimation(FlipAnimation(from = binding.image, to = binding.imageTop, forward = false))
    }

    /** Shows the picture immediately, used when the board is rebuilt mid-round. */
    fun showFaceUp() {
        isFaceUp = true
        binding.imageTop.isVisible = false
        binding.image.isVisible = true
    }
}
