package com.snatik.matches.ui;

import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;

public class DifficultyView extends LinearLayout {

	private ImageView mTitle;
	private ImageView mStars;
	
	public DifficultyView(Context context) {
		this(context, null);
	}
	
	public DifficultyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.difficult_view, this, true);
		setOrientation(LinearLayout.VERTICAL);
		mTitle = (ImageView) findViewById(R.id.title);
		mStars = (ImageView) findViewById(R.id.starts);
	}
	
	public void setDifficulty(int difficulty) {
		String titleResource = String.format(Locale.US, "button_difficulty_%d", difficulty);
		int drawableResourceId = Shared.context.getResources().getIdentifier(titleResource, "drawable", Shared.context.getPackageName());
		mTitle.setImageResource(drawableResourceId);
	}
	
	public void setStart(int stars) {
		String titleResource = String.format(Locale.US, "button_difficulty_bottom_star_%d", stars);
		int drawableResourceId = Shared.context.getResources().getIdentifier(titleResource, "drawable", Shared.context.getPackageName());
		mStars.setImageResource(drawableResourceId);
	}

}
