package com.snatik.matches.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.snatik.matches.R
import com.snatik.matches.databinding.PopupSettingsViewBinding

@SuppressLint("ViewConstructor") // created in code only
class PopupSettingsView(
    context: Context,
    soundEnabled: Boolean,
    private val onToggleSound: () -> Boolean,
    onRate: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) : LinearLayout(context) {

    private val binding = PopupSettingsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        scaleX = 0f
        scaleY = 0f
        orientation = VERTICAL
        gravity = Gravity.START or Gravity.TOP
        setBackgroundResource(R.drawable.settings_popup)
        setPadding(
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_left),
            resources.getDimensionPixelSize(R.dimen.popup_settings_padding_top),
            0,
            0,
        )
        binding.soundOff.setOnClickListener { render(onToggleSound()) }
        binding.rate.setOnClickListener { onRate() }
        binding.privacyLink.setOnClickListener { onPrivacyPolicy() }
        render(soundEnabled)
    }

    private fun render(soundEnabled: Boolean) {
        binding.soundOffText.setText(if (soundEnabled) R.string.sound_on else R.string.sound_off)
        binding.soundImage.setImageResource(if (soundEnabled) R.drawable.button_music_on else R.drawable.button_music_off)
    }
}
