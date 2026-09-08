package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.snatik.matches.R
import com.snatik.matches.databinding.PopupParentsViewBinding

/** The parents' corner: the privacy policy, and a reset of all progress that must be held to happen. */
@SuppressLint("ViewConstructor") // created in code only
class PopupParentsView(
    context: Context,
    frame: Drawable,
    onPrivacyPolicy: () -> Unit,
    onReset: () -> Unit,
) : LinearLayout(context) {

    private val binding = PopupParentsViewBinding.inflate(LayoutInflater.from(context), this)

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
        binding.privacy.setOnClickListener { onPrivacyPolicy() }
        HoldToOpen(
            binding.reset,
            holdMillis = HOLD_MS,
            onProgress = { binding.resetHold.progress = (it * 100).toInt() },
            onTap = { binding.resetText.setText(R.string.reset_hold); binding.resetText.postDelayed({ binding.resetText.setText(R.string.reset_progress) }, HINT_MS) },
            onHeld = { binding.resetHold.progress = 100; onReset() },
        )
    }

    private companion object {
        const val HOLD_MS = 2000L
        const val HINT_MS = 1600L
    }
}
