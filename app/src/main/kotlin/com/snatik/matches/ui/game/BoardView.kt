package com.snatik.matches.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.LinearLayout
import com.snatik.matches.R
import com.snatik.matches.game.Game
import kotlin.math.max
import kotlin.math.min

/** A grid of [TileView]s sized to fill the space this view was given. */
class BoardView(context: Context) : LinearLayout(context) {

    var onTileClick: ((tile: Int) -> Unit)? = null

    /** Side of a tile in pixels, valid after [setBoard]. */
    var tileSize: Int = 0
        private set

    private val tiles = mutableListOf<TileView>()

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        clipChildren = false
        clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.board_padding)
        setPadding(padding, padding, padding, padding)
        contentDescription = context.getString(R.string.cd_tile)
    }

    /** Builds the tiles for [game]. Must be called after this view has been laid out. */
    fun setBoard(game: Game) {
        removeAllViews()
        tiles.clear()

        val difficulty = game.difficulty
        val density = resources.displayMetrics.density
        val baseMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        val margin = max(density.toInt(), (baseMargin - difficulty.level * 2 * density).toInt())
        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        tileSize = min(
            (availableHeight - difficulty.rows * 2 * margin) / difficulty.rows,
            (availableWidth - difficulty.columns * 2 * margin) / difficulty.columns,
        )

        for (row in 0 until difficulty.rows) {
            val rowLayout = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER
                clipChildren = false
            }
            for (column in 0 until difficulty.columns) {
                val tile = row * difficulty.columns + column
                val tileView = TileView(context).apply {
                    layoutParams = LayoutParams(tileSize, tileSize).apply { setMargins(margin, margin, margin, margin) }
                    contentDescription = context.getString(R.string.cd_tile)
                    setOnClickListener { onTileClick?.invoke(tile) }
                }
                when {
                    game.engine.isMatched(tile) -> tileView.visibility = INVISIBLE
                    game.engine.faceUpTile == tile -> tileView.showFaceUp()
                }
                rowLayout.addView(tileView)
                tiles += tileView
                animateAppear(tileView)
            }
            addView(rowLayout, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
    }

    fun setTileImage(tile: Int, bitmap: Bitmap) = tiles[tile].setImage(bitmap)

    fun flipUp(tile: Int) = tiles[tile].flipUp()

    fun flipDown(vararg toFlip: Int) = toFlip.forEach { tiles[it].flipDown() }

    fun hide(vararg toHide: Int) = toHide.forEach { hide(tiles[it]) }

    private fun animateAppear(view: View) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setInterpolator(BounceInterpolator())
            .withLayer()
            .start()
    }

    private fun hide(view: View) {
        view.animate()
            .alpha(0f)
            .setDuration(100)
            .withLayer()
            .withEndAction { view.visibility = INVISIBLE }
            .start()
    }
}
