package com.snatik.matches.ui;

import com.snatik.matches.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PopupWonView extends LinearLayout {

	public PopupWonView(Context context) {
		this(context, null);
	}
	
	public PopupWonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundResource(R.drawable.level_complete);
	}
	
}
