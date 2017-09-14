package com.snatik.matches.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;

import static android.R.attr.id;
import static android.R.id.edit;
import static android.content.Context.MODE_PRIVATE;


public class Memory {

	private static final String SHARED_PREFERENCES_NAME = "com.snatik.matches";
	private static String highStartKey = "theme_%d_difficulty_%d";
	private static String bestTimeKey = "themeTime_%d_difficultyTime_%d";

	public static void save(int theme, int difficulty, int stars) {
		Log.i("In save()","");
		int highStars = getHighStars(theme, difficulty);

		if (stars > highStars) {
			Log.i("New best stars","");
			SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			SharedPreferences.Editor edit = sharedPreferences.edit();
			String key = String.format(highStartKey, theme, difficulty);
			edit.putInt(key, stars).commit();
		}
	}

	public static void saveTime(int theme,int difficulty,int passedSecs) {

		int bestTime = getBestTime(theme,difficulty);

		if(passedSecs<bestTime && bestTime != -1) {
			SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			String timeKey = String.format(bestTimeKey, theme, difficulty);
			editor.putInt(timeKey,passedSecs);
			editor.commit();
		}
		if( bestTime == -1) {
			SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME,MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			String timeKey = String.format(bestTimeKey, theme, difficulty);
			editor.putInt(timeKey,passedSecs);
			editor.commit();
		}
	}

	public static int getHighStars(int theme, int difficulty) {

		SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		String key = String.format(highStartKey, theme, difficulty);
		return sharedPreferences.getInt(key, 0);
	}

	public static int getBestTime(int theme,int difficulty) {

		SharedPreferences sharedPreferences = Shared.context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		String key = String.format(bestTimeKey, theme, difficulty);
		return sharedPreferences.getInt(key, -1);
	}

	public static String getBestTimeForStage(int theme,int difficulty) {

		int bestTime = getBestTime(theme,difficulty);
		if(bestTime != -1) {
			Log.i("Best time for diff", ""+bestTime);
			int minutes = (bestTime % 3600) / 60;
			int seconds = (bestTime) % 60;
			String result = String.format("BEST : %02d:%02d", minutes, seconds);
			return result;
		} else {
			String result = "BEST : -";
			return result;
		}
	}
}
