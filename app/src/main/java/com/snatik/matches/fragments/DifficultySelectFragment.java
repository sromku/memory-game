package com.snatik.matches.fragments;

import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import com.snatik.matches.R;
import com.snatik.matches.common.Memory;
import com.snatik.matches.common.Shared;
import com.snatik.matches.events.ui.DifficultySelectedEvent;
import com.snatik.matches.themes.Theme;
import com.snatik.matches.ui.DifficultyView;

public class DifficultySelectFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = LayoutInflater.from(Shared.context).inflate(R.layout.difficulty_select_fragment, container, false);
		Theme theme = Shared.engine.getSelectedTheme();

		DifficultyView difficulty1 = (DifficultyView) view.findViewById(R.id.select_difficulty_1);
		difficulty1.setDifficulty(1, Memory.getHighStars(theme.id, 1));
		setOnClick(difficulty1, 1);

		DifficultyView difficulty2 = (DifficultyView) view.findViewById(R.id.select_difficulty_2);
		difficulty2.setDifficulty(2, Memory.getHighStars(theme.id, 2));
		setOnClick(difficulty2, 2);

		DifficultyView difficulty3 = (DifficultyView) view.findViewById(R.id.select_difficulty_3);
		difficulty3.setDifficulty(3, Memory.getHighStars(theme.id, 3));
		setOnClick(difficulty3, 3);

		DifficultyView difficulty4 = (DifficultyView) view.findViewById(R.id.select_difficulty_4);
		difficulty4.setDifficulty(4, Memory.getHighStars(theme.id, 4));
		setOnClick(difficulty4, 4);

		DifficultyView difficulty5 = (DifficultyView) view.findViewById(R.id.select_difficulty_5);
		difficulty5.setDifficulty(5, Memory.getHighStars(theme.id, 5));
		setOnClick(difficulty5, 5);

		DifficultyView difficulty6 = (DifficultyView) view.findViewById(R.id.select_difficulty_6);
		difficulty6.setDifficulty(6, Memory.getHighStars(theme.id, 6));
		setOnClick(difficulty6, 6);

		animate(difficulty1, difficulty2, difficulty3, difficulty4, difficulty5, difficulty6);

		return view;
	}

	private void animate(View... view) {
		AnimatorSet animatorSet = new AnimatorSet();
		Builder builder = animatorSet.play(new AnimatorSet());
		for (int i = 0; i < view.length; i++) {
			ObjectAnimator scaleX = ObjectAnimator.ofFloat(view[i], "scaleX", 0.8f, 1f);
			ObjectAnimator scaleY = ObjectAnimator.ofFloat(view[i], "scaleY", 0.8f, 1f);
			builder.with(scaleX).with(scaleY);
		}
		animatorSet.setDuration(500);
		animatorSet.setInterpolator(new BounceInterpolator());
		animatorSet.start();
	}

	private void setOnClick(View view, final int difficulty) {
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Shared.eventBus.notify(new DifficultySelectedEvent(difficulty));
			}
		});
	}
}
