package com.snatik.matches.ui.character

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A [Character] rasterised once for a given card size, so animating it costs one bitmap draw per
 * frame instead of transforming and filling hundreds of paths. The body (without eyes) and the
 * drop shadow are bitmaps; the eyes stay as paths, they are few and they have to move on their own
 * to blink. Both cards of a pair share one instance; the drawables keep their own animation state.
 */
class RenderedCharacter(
    val character: Character,
    /** Pixels per character unit in [body] and [shadow]. */
    val scale: Float,
    val body: Bitmap,
    val shadow: Bitmap?,
    /** Where [shadow]'s top-left sits, in character units. */
    val shadowOrigin: RectF,
    val eyes: List<Character.Part>,
) {
    companion object {
        suspend fun render(character: Character, sizePx: Int): RenderedCharacter = withContext(Dispatchers.Default) {
            val scale = sizePx / maxOf(character.width, character.height)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val body = createBitmap((character.width * scale).toInt().coerceAtLeast(1), (character.height * scale).toInt().coerceAtLeast(1))
            Canvas(body).apply {
                scale(scale, scale)
                for (part in character.parts) if (part.group == Character.Group.BODY) {
                    paint.color = part.color
                    drawPath(part.path, paint)
                }
            }
            val shadowParts = character.parts.filter { it.group == Character.Group.SHADOW }
            val shadowBounds = shadowParts.map { it.bounds }.reduceOrNull { acc, r -> RectF(acc).apply { union(r) } }
            val shadow = shadowBounds?.let { bounds ->
                createBitmap((bounds.width() * scale).toInt().coerceAtLeast(1), (bounds.height() * scale).toInt().coerceAtLeast(1)).also { bitmap ->
                    Canvas(bitmap).apply {
                        scale(scale, scale)
                        translate(-bounds.left, -bounds.top)
                        for (part in shadowParts) {
                            paint.color = part.color
                            drawPath(part.path, paint)
                        }
                    }
                }
            }
            RenderedCharacter(
                character = character,
                scale = scale,
                body = body,
                shadow = shadow,
                shadowOrigin = shadowBounds ?: RectF(),
                eyes = character.parts.filter { it.group == Character.Group.EYE || it.group == Character.Group.PUPIL },
            )
        }
    }
}
