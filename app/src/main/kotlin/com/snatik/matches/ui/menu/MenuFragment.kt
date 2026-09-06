package com.snatik.matches.ui.menu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.snatik.matches.R
import com.snatik.matches.databinding.MenuFragmentBinding
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.dp

class MenuFragment : Fragment(R.layout.menu_fragment) {

    private val viewModel: GameViewModel by activityViewModels()
    private val runningAnimators = mutableListOf<Animator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MenuFragmentBinding.bind(view)
        binding.settingsGameButton.isSoundEffectsEnabled = false
        binding.settingsGameButton.setOnClickListener { viewModel.openSettings() }
        binding.googlePlayButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.leaderboards_coming_soon, Toast.LENGTH_LONG).show()
        }
        binding.startGameButton.setOnClickListener { button ->
            button.isEnabled = false
            animateAllAssetsOff(binding) { viewModel.startPressed() }
        }
        startLightsAnimation(binding)
        startTooltipAnimation(binding)
    }

    override fun onDestroyView() {
        runningAnimators.forEach { it.removeAllListeners(); it.cancel() }
        runningAnimators.clear()
        super.onDestroyView()
    }

    private fun animateAllAssetsOff(binding: MenuFragmentBinding, onEnd: () -> Unit) {
        val slideDuration = 300L
        val slide = AccelerateInterpolator(2f)
        val title = ObjectAnimator.ofFloat(binding.title, View.TRANSLATION_Y, (-200).dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        val lightsX = ObjectAnimator.ofFloat(binding.startGameButtonLights, View.SCALE_X, 0f)
        val lightsY = ObjectAnimator.ofFloat(binding.startGameButtonLights, View.SCALE_Y, 0f)
        val tooltip = ObjectAnimator.ofFloat(binding.tooltip, View.ALPHA, 0f).apply { duration = 100 }
        val settings = ObjectAnimator.ofFloat(binding.settingsGameButton, View.TRANSLATION_Y, 120.dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        val googlePlay = ObjectAnimator.ofFloat(binding.googlePlayButton, View.TRANSLATION_Y, 120.dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        val start = ObjectAnimator.ofFloat(binding.startGameButton, View.TRANSLATION_Y, 130.dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        AnimatorSet().apply {
            playTogether(title, lightsX, lightsY, tooltip, settings, googlePlay, start)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) = onEnd()
            })
            runningAnimators += this
            start()
        }
    }

    private fun startTooltipAnimation(binding: MenuFragmentBinding) {
        val squash = ObjectAnimator.ofFloat(binding.tooltip, View.SCALE_Y, 0.8f).apply { duration = 200 }
        val bounceBack = ObjectAnimator.ofFloat(binding.tooltip, View.SCALE_Y, 1f).apply {
            duration = 500
            interpolator = BounceInterpolator()
        }
        val set = AnimatorSet().apply {
            startDelay = 1000
            playSequentially(squash, bounceBack)
        }
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                set.startDelay = 2000
                set.start()
            }
        })
        runningAnimators += set
        set.start()
    }

    private fun startLightsAnimation(binding: MenuFragmentBinding) {
        ObjectAnimator.ofFloat(binding.startGameButtonLights, View.ROTATION, 0f, 360f).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 6000
            repeatCount = ValueAnimator.INFINITE
            runningAnimators += this
            start()
        }
    }
}
