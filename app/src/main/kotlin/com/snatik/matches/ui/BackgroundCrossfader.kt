package com.snatik.matches.ui

import android.widget.ImageView
import com.snatik.matches.R
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.image.BitmapLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

    fun loadDefault() {
        scope.launch {
            base.setScene(BitmapLoader.decodeSampled(base.resources, R.drawable.background, screenWidth, screenHeight))
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
