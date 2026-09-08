package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.snatik.matches.R
import com.snatik.matches.databinding.PopupSettingsViewBinding

@SuppressLint("ViewConstructor") // created in code only
class PopupSettingsView(
    context: Context,
    frame: Drawable,
    private val soundOnIcon: Drawable,
    private val soundOffIcon: Drawable,
    rateIcon: Drawable,
    soundEnabled: Boolean,
    private val onToggleSound: () -> Boolean,
    onRate: () -> Unit,
    onParents: () -> Unit,
    onLanguage: () -> Unit,
) : LinearLayout(context) {

    private val binding = PopupSettingsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.START or Gravity.TOP
        background = frame
        setPadding(
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_left),
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_top),
            0,
            0,
        )
        binding.soundOff.setOnClickListener { render(onToggleSound()) }
        binding.rate.setOnClickListener { onRate() }
        // The parents' corner opens only when the row is held; a tap just says so.
        HoldToOpen(
            binding.parents,
            holdMillis = HOLD_MS,
            onProgress = { binding.parentsText.alpha = 1f - 0.5f * it },
            onTap = { hint() },
            onHeld = onParents,
        )
        binding.language.setOnClickListener { onLanguage() }
        binding.rateImage.setImageDrawable(rateIcon)
        render(soundEnabled)
    }

    /** "Hold to open", shown for a moment on the parents row after a tap. */
    private fun hint() {
        binding.parentsText.setText(R.string.parents_hold)
        binding.parentsText.removeCallbacks(restoreParentsLabel)
        binding.parentsText.postDelayed(restoreParentsLabel, HINT_MS)
    }

    private val restoreParentsLabel = Runnable { binding.parentsText.setText(R.string.parents) }

    private fun render(soundEnabled: Boolean) {
        binding.soundOffText.setText(if (soundEnabled) R.string.sound_on else R.string.sound_off)
        binding.soundImage.setImageDrawable(if (soundEnabled) soundOnIcon else soundOffIcon)
    }

    private companion object {
        const val HOLD_MS = 1500L
        const val HINT_MS = 1600L
    }
}
