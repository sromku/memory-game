package com.snatik.matches

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.snatik.matches.databinding.ActivityMainBinding
import com.snatik.matches.ui.BackgroundCrossfader
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.GameViewModel.UiEvent
import com.snatik.matches.ui.difficulty.DifficultySelectFragment
import com.snatik.matches.ui.game.GameFragment
import com.snatik.matches.ui.menu.MenuFragment
import com.snatik.matches.ui.popup.PopupHost
import com.snatik.matches.ui.theme.ThemeSelectFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var popups: PopupHost
    private lateinit var backgrounds: BackgroundCrossfader
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enterImmersiveMode()

        val popupBackCallback = onBackPressedDispatcher.addCallback(this, enabled = false) {
            if (popups.isWonShown) viewModel.backToDifficultySelect() else popups.close()
        }
        popups = PopupHost(binding.popupContainer) { shown -> popupBackCallback.isEnabled = shown }
        backgrounds = BackgroundCrossfader(binding.backgroundDefault, binding.backgroundTheme, lifecycleScope)
        backgrounds.loadDefault()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, MenuFragment())
            }
        } else if (viewModel.selectedTheme.value == null) {
            // Restored after process death: the fragments came back but the round did not. Start over.
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            viewModel.game?.result?.let { if (currentFragment is GameFragment) showWonPopup(it) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiEventFlow.collect(::handle) }
                launch { viewModel.selectedTheme.collect(backgrounds::show) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenShown()
    }

    override fun onStop() {
        viewModel.onScreenHidden()
        super.onStop()
    }

    private fun handle(event: UiEvent) {
        when (event) {
            UiEvent.OpenThemeSelect -> push(ThemeSelectFragment(), BACK_STACK_THEME)
            UiEvent.OpenDifficultySelect -> push(DifficultySelectFragment(), BACK_STACK_DIFFICULTY)
            UiEvent.OpenGame -> {
                // "Play again" replaces the finished round instead of stacking on top of it.
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(GameFragment(), BACK_STACK_GAME)
            }
            UiEvent.ReturnToDifficultySelect ->
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            UiEvent.ShowSettings -> popups.showSettings(
                soundEnabled = viewModel.soundEnabled.value,
                onToggleSound = viewModel::toggleSound,
                onRate = ::openStoreListing,
            )
            is UiEvent.ShowWon -> showWonPopup(event.result)
            UiEvent.ClosePopup -> popups.close()
        }
    }

    private fun showWonPopup(result: com.snatik.matches.game.GameResult) {
        popups.showWon(
            result = result,
            onStar = viewModel::playStarSound,
            onBack = viewModel::backToDifficultySelect,
            onNext = viewModel::nextGame,
        )
    }

    private fun push(fragment: Fragment, name: String) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container, fragment)
            addToBackStack(name)
        }
    }

    private val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container)

    private fun enterImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun openStoreListing() {
        val market = Intent(Intent.ACTION_VIEW, "market://details?id=$PLAY_STORE_PACKAGE".toUri())
        try {
            startActivity(market)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$PLAY_STORE_PACKAGE".toUri()))
        }
    }

    private companion object {
        /** The published application id (the debug build adds a suffix). */
        const val PLAY_STORE_PACKAGE = "com.snatik.matches"
        const val BACK_STACK_THEME = "theme"
        const val BACK_STACK_DIFFICULTY = "difficulty"
        const val BACK_STACK_GAME = "game"
    }
}
