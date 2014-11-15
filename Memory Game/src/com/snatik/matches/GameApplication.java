package com.snatik.matches;

import android.app.Application;

import com.snatik.matches.common.Stats;
import com.snatik.matches.utils.FontLoader;

public class GameApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		FontLoader.loadFonts(this);
		Stats.getInstance().setContext(this);
	}
}
