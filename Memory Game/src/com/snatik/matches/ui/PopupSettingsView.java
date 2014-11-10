package com.snatik.matches.ui;

import com.snatik.matches.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PopupSettingsView extends LinearLayout {

	public PopupSettingsView(Context context) {
		this(context, null);
	}
	
	public PopupSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundResource(R.drawable.popup_settings);
	}
	
}
