package com.snatik.matches.ui;

import com.snatik.matches.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DifficultyView extends LinearLayout {

	public DifficultyView(Context context) {
		this(context, null);
	}
	
	public DifficultyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public static DifficultyView fromXml(Context context, ViewGroup parent) {
		return (DifficultyView) LayoutInflater.from(context).inflate(R.layout.difficult_view, parent, false);
	}

}
