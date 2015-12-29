package com.snatik.matches.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class Memory {

	private static final String SHARED_PREFERENCES_NAME = "com.snatik.matches";
	private static String highStartKey = "theme_%d_difficulty_%d";

	public static void save(int theme, int difficulty, int stars) {
		int highStars = getHighStars(theme, difficulty);
		if (stars > highStars) {
			SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			Editor edit = sharedPreferences.edit();
			String key = String.format(highStartKey, theme, difficulty);
			edit.putInt(key, stars).commit();
		}
	}

	public static int getHighStars(int theme, int difficulty) {
		SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		String key = String.format(highStartKey, theme, difficulty);
		return sharedPreferences.getInt(key, 0);
	}
}
