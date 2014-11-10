package com.snatik.matches.fragments;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;
import com.snatik.matches.events.ui.SelectGameEvent;
import com.snatik.matches.model.Board;
import com.snatik.matches.model.Game;
import com.snatik.matches.themes.Themes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DifficultySelectFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = LayoutInflater.from(Shared.context).inflate(R.layout.difficulty_select_fragment, container, false);
		View difficulty = view.findViewById(R.id.select_difficulty);
		difficulty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Game game = new Game();
				game.theme = Themes.createMosterTheme();
				game.board = new Board(Board._6);
				Shared.eventBus.notify(new SelectGameEvent(game));
			}
		});
		return view;
	}
}
