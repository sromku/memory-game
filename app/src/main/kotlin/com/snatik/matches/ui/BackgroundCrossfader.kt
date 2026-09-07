package com.snatik.matches.ui

import android.content.res.AssetManager
import android.widget.ImageView
import com.snatik.matches.R
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.image.BitmapLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import com.snatik.matches.ui.menu.MenuVisitors

/**
 * The default background sits in [base]; a theme's background is decoded in the background and
 * cross-faded in through [overlay].
 */
class BackgroundCrossfader(
    private val base: LivingSceneView,
    private val overlay: ImageView,
    private val scope: CoroutineScope,
) {
    private var shownTheme: GameTheme? = null
    private var loadJob: Job? = null

    private val screenWidth get() = base.resources.displayMetrics.widthPixels
    private val screenHeight get() = base.resources.displayMetrics.heightPixels

    fun loadDefault(assets: AssetManager) {
        scope.launch {
            base.setScene(BitmapLoader.decodeSampled(base.resources, R.drawable.background, screenWidth, screenHeight))
            // The menu's animals: a sixth of the screen tall, rasterised once each, off the main thread.
            val placed = MenuVisitors.visitors.map { visitor ->
                val character = withContext(Dispatchers.IO) { Character.load(assets, visitor.name) } ?: return@map null
                val size = (screenHeight * 0.19f * visitor.scale * 1.2f).toInt()
                visitor to CharacterDrawable(RenderedCharacter.render(character, size))
            }.filterNotNull()
            base.setVisitors(placed)
            base.greet(MenuVisitors.greeting(base.resources.getStringArray(R.array.animal_greetings)))
        }
    }

    fun show(theme: GameTheme?) {
        if (theme == shownTheme) return
        shownTheme = theme
        loadJob?.cancel()
        // The scene only moves while it can be seen; under a theme background it rests.
        if (theme == null) base.start() else base.stop()
        if (theme == null) {
            overlay.animate()
                .alpha(0f)
                .setDuration(FADE_DURATION_MS)
                .withEndAction { if (shownTheme == null) overlay.setImageDrawable(null) }
                .start()
            return
        }
        loadJob = scope.launch {
            val bitmap = BitmapLoader.decodeSampled(overlay.resources, theme.backgroundRes, screenWidth, screenHeight)
            overlay.setImageBitmap(bitmap)
            overlay.animate().alpha(1f).setDuration(FADE_DURATION_MS).start()
        }
    }

    private companion object {
        const val FADE_DURATION_MS = 2000L
    }
}
