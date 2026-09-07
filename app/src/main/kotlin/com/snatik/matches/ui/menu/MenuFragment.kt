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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.launch
import com.snatik.matches.R
import com.snatik.matches.databinding.MenuFragmentBinding
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.dp

class MenuFragment : Fragment(R.layout.menu_fragment) {

    private val viewModel: GameViewModel by activityViewModels()
    private val runningAnimators = mutableListOf<Animator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = MenuFragmentBinding.bind(view)
        viewLifecycleOwner.lifecycleScope.launch {
            // Traced vectors: inflate them off the main thread so the first frame is not held up.
            binding.startGameButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_start))
            binding.settingsGameButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_settings))
            binding.mapGameButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_map))
            binding.tooltip.setImageDrawable(requireContext().loadDrawable(R.drawable.tooltip_play))
        }
        binding.settingsGameButton.isSoundEffectsEnabled = false
        binding.settingsGameButton.setOnClickListener { viewModel.openSettings() }
        // The big button plays straight away; the map button is the way to themes and roads.
        binding.startGameButton.setOnClickListener { leave(binding, viewModel::quickPlay) }
        binding.mapGameButton.setOnClickListener { leave(binding, viewModel::openThemes) }
        startLightsAnimation(binding)
        startTooltipAnimation(binding)
    }

    override fun onStart() {
        super.onStart()
        // Also reached by going back from a road or a quick game; the background returns to default.
        viewModel.clearTheme()
    }

    private fun leave(binding: MenuFragmentBinding, then: () -> Unit) {
        binding.startGameButton.isEnabled = false
        binding.mapGameButton.isEnabled = false
        animateAllAssetsOff(binding, then)
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
        val map = ObjectAnimator.ofFloat(binding.mapGameButton, View.TRANSLATION_Y, 120.dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        val start = ObjectAnimator.ofFloat(binding.startGameButton, View.TRANSLATION_Y, 130.dp(binding.root)).apply {
            interpolator = slide
            duration = slideDuration
        }
        AnimatorSet().apply {
            playTogether(title, lightsX, lightsY, tooltip, settings, map, start)
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
