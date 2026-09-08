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
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.BackgroundCrossfader
import com.snatik.matches.ui.image.ArtCache
import com.snatik.matches.ui.image.ArtWarmup
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.GameViewModel.UiEvent
import com.snatik.matches.ui.album.AlbumFragment
import com.snatik.matches.ui.difficulty.DifficultySelectFragment
import com.snatik.matches.ui.game.GameFragment
import com.snatik.matches.ui.menu.MenuFragment
import com.snatik.matches.game.progression.MiniGame
import com.snatik.matches.ui.minigame.FollowTheSongFragment
import com.snatik.matches.ui.minigame.OddOneOutFragment
import com.snatik.matches.ui.minigame.ShadowMatchFragment
import com.snatik.matches.ui.minigame.PartyGameFragment
import com.snatik.matches.ui.minigame.PeekAndFindFragment
import com.snatik.matches.ui.minigame.ShoppingListFragment
import com.snatik.matches.ui.minigame.WhatChangedFragment
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
        backgrounds.loadDefault(assets, viewModel.progress.value.friendsOf(GameTheme.ANIMALS))

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
            viewModel.miniGame?.result?.let { if (currentFragment is PartyGameFragment || currentFragment is PeekAndFindFragment) showWonPopup(it) }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiEventFlow.collect(::handle) }
                launch { viewModel.selectedTheme.collect(backgrounds::show) }
                launch {
                    // Every screen's art, rendered ahead of time; already-rendered art costs nothing here.
                    viewModel.progress.collect { progress ->
                        ArtCache.warm(applicationContext, ArtWarmup.plan(resources, progress, viewModel.themeForArt))
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
            UiEvent.OpenAlbum -> push(AlbumFragment(), BACK_STACK_ALBUM)
            UiEvent.OpenGame -> {
                // "Play again" replaces the finished round instead of stacking on top of it.
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(GameFragment(), BACK_STACK_GAME)
            }
            is UiEvent.OpenMiniGame -> {
                supportFragmentManager.popBackStack(BACK_STACK_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                push(miniGameFragment(event.kind), BACK_STACK_GAME)
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
                onParents = viewModel::openParents,
                onLanguage = viewModel::openLanguages,
            )
            is UiEvent.ShowFriend -> popups.showFriend(event.theme, event.image, viewModel.progress.value.roundsToFriend(event.theme, event.image))
            UiEvent.ShowParents -> popups.showParents(onPrivacyPolicy = ::openPrivacyPolicy, onReset = viewModel::resetProgress)
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

    private fun miniGameFragment(kind: MiniGame): Fragment = when (kind) {
        MiniGame.WHO_WAS_HERE -> WhoWasHereFragment()
        MiniGame.FOLLOW_THE_SONG -> FollowTheSongFragment()
        MiniGame.WHAT_CHANGED -> WhatChangedFragment()
        MiniGame.SHADOW_MATCH -> ShadowMatchFragment()
        MiniGame.ODD_ONE_OUT -> OddOneOutFragment()
        MiniGame.SHOPPING_LIST -> ShoppingListFragment()
        MiniGame.PEEK_AND_FIND -> PeekAndFindFragment()
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
        const val BACK_STACK_ALBUM = "album"
        const val BACK_STACK_GAME = "game"
    }
}
