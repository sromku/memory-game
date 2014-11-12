package com.snatik.matches.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;
import com.snatik.matches.events.ui.SelectGameEvent;
import com.snatik.matches.model.BoardConfiguration;
import com.snatik.matches.model.Game;
import com.snatik.matches.themes.Themes;
import com.snatik.matches.ui.DifficultyView;

public class DifficultySelectFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = LayoutInflater.from(Shared.context).inflate(R.layout.difficulty_select_fragment, container, false);
		DifficultyView difficulty1 = (DifficultyView) view.findViewById(R.id.select_difficulty_1);
		difficulty1.setDifficulty(1);
		difficulty1.setStart(0);
		setOnClick(difficulty1, BoardConfiguration._6);
		
		DifficultyView difficulty2 = (DifficultyView) view.findViewById(R.id.select_difficulty_2);
		difficulty2.setDifficulty(2);
		difficulty2.setStart(0);
		setOnClick(difficulty2, BoardConfiguration._12);
		
		DifficultyView difficulty3 = (DifficultyView) view.findViewById(R.id.select_difficulty_3);
		difficulty3.setDifficulty(3);
		difficulty3.setStart(0);
		setOnClick(difficulty3, BoardConfiguration._20);
		
		DifficultyView difficulty4 = (DifficultyView) view.findViewById(R.id.select_difficulty_4);
		difficulty4.setDifficulty(4);
		difficulty4.setStart(0);
		setOnClick(difficulty4, BoardConfiguration._30);
		
		DifficultyView difficulty5 = (DifficultyView) view.findViewById(R.id.select_difficulty_5);
		difficulty5.setDifficulty(5);
		difficulty5.setStart(0);
		setOnClick(difficulty5, BoardConfiguration._40);
		
		DifficultyView difficulty6 = (DifficultyView) view.findViewById(R.id.select_difficulty_6);
		difficulty6.setDifficulty(6);
		difficulty6.setStart(0);
		setOnClick(difficulty6, BoardConfiguration._54);
		
		return view;
	}
	
	private void setOnClick(View view, final int boardNumber) {
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Game game = new Game();
				game.theme = Themes.createMosterTheme();
				game.boardConfiguration = new BoardConfiguration(boardNumber);
				Shared.eventBus.notify(new SelectGameEvent(game));
			}
		});
	}
}
