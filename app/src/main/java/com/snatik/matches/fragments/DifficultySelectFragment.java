package com.snatik.matches.fragments;

import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;

import com.snatik.matches.R;
import com.snatik.matches.common.Memory;
import com.snatik.matches.common.SQLiteDB;
import com.snatik.matches.common.Shared;
import com.snatik.matches.events.ui.DifficultySelectedEvent;
import com.snatik.matches.themes.Theme;
import com.snatik.matches.ui.DifficultyView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.R.id.text1;

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

		Typeface type = Typeface.createFromAsset(Shared.context.getAssets(),"fonts/grobold.ttf");


        TextView text1 = (TextView) view.findViewById(R.id.time_difficulty_1);
		text1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text1.setTypeface(type);

        TextView text2 = (TextView) view.findViewById(R.id.time_difficulty_2);
		text2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text2.setTypeface(type);

        TextView text3 = (TextView) view.findViewById(R.id.time_difficulty_3);
		text3.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text3.setTypeface(type);

        TextView text4 = (TextView) view.findViewById(R.id.time_difficulty_4);
		text4.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text4.setTypeface(type);

        TextView text5 = (TextView) view.findViewById(R.id.time_difficulty_5);
		text5.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text5.setTypeface(type);

        TextView text6 = (TextView) view.findViewById(R.id.time_difficulty_6);
		text6.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		text6.setTypeface(type);



		SQLiteDB db = new SQLiteDB(Shared.context,null,null,1);
        List <Integer> times = new ArrayList<>();

		for(int i = 1; i<=6 ; i++) {
			if(db.getBestTimeForStage(theme.id, i)!= 0) {
				times.add(db.getBestTimeForStage(theme.id, i));
			} else {
				times.add(-1);
			}
			System.out.println(times.get(i-1));
		}

		if(times.get(0) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(0) * 1000);
			text1.setText(" BEST : " + dateFormat.format(date));

		} else {
			text1.setText("BEST :  - ");
		}

		if(times.get(1) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(1) * 1000);
			text2.setText(" BEST : " + dateFormat.format(date));

		} else {
			text2.setText("BEST :  - ");
		}

		if(times.get(2) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(2) * 1000);
			text3.setText(" BEST : " + dateFormat.format(date));

		} else {
			text3.setText("BEST :  - ");
		}

		if(times.get(3) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(3) * 1000);
			text4.setText(" BEST : " + dateFormat.format(date));

		} else {
			text4.setText("BEST :  - ");
		}

		if(times.get(4) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(4) * 1000);
			text5.setText(" BEST : " + dateFormat.format(date));

		} else {
			text5.setText("BEST :  - ");
		}

		if(times.get(5) != -1) {

			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(times.get(5) * 1000);
			text6.setText(" BEST : " + dateFormat.format(date));

		} else {
			text6.setText("BEST :  - ");
		}


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
