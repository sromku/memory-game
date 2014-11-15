package com.snatik.matches.common;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.snatik.matches.R;

public class Stats {

	private static Stats mInstance = null;
	private Context mContext;
	private Tracker mTracker;
	
	public static class Event {
		public static final String SCREEN_MENU = "Menu screen";
		public static final String SCREEN_ANIMALS_THEME = "Animals difficulty screen";
		public static final String SCREEN_MONSTERS_THEME = "Monsters difficulty screen";
		public static final String SCREEN_GAME = "Game screen";
		public static final String POPUP_SETTINGS = "Popup settings";
		public static final String POPUP_GOOGLE_PLAY = "Popup google play";
	}

	private Stats() {
	}

	public static Stats getInstance() {
		if (mInstance == null) {
			mInstance = new Stats();
		}
		return mInstance;
	}

	public Stats setContext(Context context) {
		mContext = context;
		return this;
	}

	public synchronized Tracker getTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
			mTracker = analytics.newTracker(R.xml.app_tracker);
			mTracker.enableAdvertisingIdCollection(true);
		}
		return mTracker;
	}

	/**
	 * Send this event when user saw the screen.
	 * 
	 * @param path
	 *            is a String representing the screen name.
	 */
	public void sendScreenView(String screenName) {
		Tracker tracker = getTracker();
		tracker.setScreenName(screenName);
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}
}
