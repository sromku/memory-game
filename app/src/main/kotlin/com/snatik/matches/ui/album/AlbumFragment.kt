package com.snatik.matches.ui.album

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.snatik.matches.R
import com.snatik.matches.databinding.AlbumFragmentBinding
import com.snatik.matches.databinding.AlbumSectionBinding
import com.snatik.matches.game.GameTheme
import com.snatik.matches.ui.GameViewModel
import com.snatik.matches.ui.character.Character
import com.snatik.matches.ui.character.CharacterDrawable
import com.snatik.matches.ui.character.RenderedCharacter
import com.snatik.matches.ui.image.loadDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The friends album: every character of every theme as a card, the collected ones in colour
 * (tap one and it hops), the rest as shadows waiting for their ten rounds.
 */
class AlbumFragment : Fragment(R.layout.album_fragment) {

    private val viewModel: GameViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = AlbumFragmentBinding.bind(view)
        binding.backButton.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        viewLifecycleOwner.lifecycleScope.launch { binding.backButton.setImageDrawable(requireContext().loadDrawable(R.drawable.button_back)) }
        val progress = viewModel.progress.value
        val total = GameTheme.entries.sumOf { it.characters.size }
        val collected = GameTheme.entries.sumOf { progress.friendsOf(it).size }
        binding.title.text = getString(R.string.album_title_format, getString(R.string.album_title), collected, total)
        binding.sections.doOnLayout {
            for (theme in GameTheme.entries) addSection(binding, theme, progress.friendsOf(theme).toSet())
        }
    }

    private fun addSection(binding: AlbumFragmentBinding, theme: GameTheme, friends: Set<Int>) {
        val section = AlbumSectionBinding.inflate(LayoutInflater.from(requireContext()), binding.sections, true)
        section.name.text = getString(THEME_NAMES.getValue(theme))
        section.count.text = getString(R.string.road_progress_format, friends.size, theme.characters.size)
        val card = resources.getDimensionPixelSize(R.dimen.album_card)
        val padding = resources.getDimensionPixelSize(R.dimen.album_card_padding)
        val gap = resources.getDimensionPixelSize(R.dimen.road_header_gap)
        section.grid.columnCount = ((binding.sections.width - binding.sections.paddingLeft - binding.sections.paddingRight) / (card + gap)).coerceAtLeast(1)
        val cards = theme.characters.indices.map { image ->
            ImageView(requireContext()).apply {
                setBackgroundResource(R.drawable.tile)
                setPadding(padding, padding, padding, padding)
                contentDescription = getString(if (image in friends) R.string.cd_friend_found else R.string.cd_friend_hidden)
                section.grid.addView(this, GridLayout.LayoutParams().apply { width = card; height = card; setMargins(gap / 2, gap / 2, gap / 2, gap / 2) })
            }
        }
        val assets = requireContext().assets
        viewLifecycleOwner.lifecycleScope.launch {
            theme.characters.forEachIndexed { image, name ->
                val character = withContext(Dispatchers.IO) { Character.load(assets, name) } ?: return@forEachIndexed
                val drawable = CharacterDrawable(RenderedCharacter.render(character, card - 2 * padding))
                val view = cards[image]
                if (image in friends) {
                    view.setImageDrawable(drawable)
                    view.setOnClickListener { drawable.start(); drawable.hop() }
                } else {
                    drawable.colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
                    drawable.alpha = SHADOW_ALPHA
                    view.setImageDrawable(drawable)
                }
            }
        }
    }

    private companion object {
        const val SHADOW_ALPHA = 110
        val THEME_NAMES = mapOf(
            GameTheme.ANIMALS to R.string.theme_animals,
            GameTheme.MONSTERS to R.string.theme_monsters,
            GameTheme.EMOJI to R.string.theme_emoji,
        )
    }
}
