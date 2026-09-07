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
import com.snatik.matches.game.GameResult
import com.snatik.matches.ui.BackgroundCrossfader
import com.snatik.matches.ui.image.ArtCache
import com.snatik.matches.ui.image.ArtWarmup
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.GameViewModel.UiEvent
import com.snatik.matches.ui.difficulty.DifficultySelectFragment
import com.snatik.matches.ui.game.GameFragment
import com.snatik.matches.ui.menu.MenuFragment
import com.snatik.matches.ui.minigame.FollowTheSongFragment
import com.snatik.matches.ui.minigame.WhoWasHereFragment
import com.snatik.matches.ui.popup.AppLanguages
import com.snatik.matches.ui.popup.PopupHost
import com.snatik.matches.ui.road.RoadMapFragment
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
            if (popups.isWonShown) viewModel.backToRoadMap() else popups.close()
        }
        popups = PopupHost(binding.popupContainer, lifecycleScope) { shown -> popupBackCallback.isEnabled = shown }
        backgrounds = BackgroundCrossfader(binding.backgroundDefault, binding.backgroundTheme, lifecycleScope)
        backgrounds.loadDefault(assets)

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
            viewModel.miniGame?.result?.let { if (currentFragment is WhoWasHereFragment || currentFragment is FollowTheSongFragment) showWonPopup(it) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiEventFlow.collect(::handle) }
                launch { viewModel.selectedTheme.collect(backgrounds::show) }
                launch {
                    // Every screen's art, rendered ahead of time; already-rendered art costs nothing here.
                    viewModel.progress.collect { progress ->
                        ArtCache.warm(applicationContext, ArtWarmup.plan(resources, progress, viewModel::averageStars))
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onScreenShown()
        if (viewModel.selectedTheme.value == null) binding.backgroundDefault.start()
    }

    override fun onStop() {
        binding.backgroundDefault.stop()
        viewModel.onScreenHidden()
        super.onStop()
    }

    private fun handle(event: UiEvent) {
        when (event) {
            UiEvent.OpenThemePicker -> push(ThemeSelectFragment(), BACK_STACK_PICKER)
            UiEvent.OpenDifficultyPicker -> push(DifficultySelectFragment(), BACK_STACK_PICKER)
            UiEvent.ClosePicker -> supportFragmentManager.popBackStack(BACK_STACK_PICKER, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            UiEvent.OpenRoadMap -> push(RoadMapFragment(), BACK_STACK_ROAD)
            UiEvent.OpenGame -> {
                // "Play again" replaces the finished round instead of stacking on top of it.
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(GameFragment(), BACK_STACK_GAME)
            }
            UiEvent.OpenWhoWasHere -> {
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(WhoWasHereFragment(), BACK_STACK_GAME)
            }
            UiEvent.OpenFollowTheSong -> {
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(FollowTheSongFragment(), BACK_STACK_GAME)
            }
            UiEvent.ReturnToRoadMap -> {
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                // A quick game came straight from the menu; its road is shown on the way back.
                if (!isOnBackStack(BACK_STACK_ROAD)) push(RoadMapFragment(), BACK_STACK_ROAD)
            }
            UiEvent.ShowSettings -> popups.showSettings(
                soundEnabled = viewModel.soundEnabled.value,
                onToggleSound = viewModel::toggleSound,
                onRate = ::openStoreListing,
                onPrivacyPolicy = ::openPrivacyPolicy,
                onLanguage = viewModel::openLanguages,
            )
            UiEvent.ShowLanguages -> popups.showLanguages(AppLanguages.chosen) { tag ->
                popups.close()
                AppLanguages.choose(tag) // AppCompat recreates the activity in the new language
            }
            is UiEvent.ShowWon -> showWonPopup(event.result)
            UiEvent.ClosePopup -> popups.close()
        }
    }

    private fun showWonPopup(result: GameResult) {
        popups.showWon(
            result = result,
            onStar = viewModel::playStarSound,
            onBack = viewModel::backToRoadMap,
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

    private fun isOnBackStack(name: String): Boolean =
        (0 until supportFragmentManager.backStackEntryCount).any { supportFragmentManager.getBackStackEntryAt(it).name == name }

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

    private fun openPrivacyPolicy() {
        startActivity(Intent(Intent.ACTION_VIEW, getString(R.string.privacy_policy_url).toUri()))
    }

    private companion object {
        /** The published application id (the debug build adds a suffix). */
        const val PLAY_STORE_PACKAGE = "com.snatik.matches"
        const val BACK_STACK_PICKER = "picker"
        const val BACK_STACK_ROAD = "road"
        const val BACK_STACK_GAME = "game"
    }
}
