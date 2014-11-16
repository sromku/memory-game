package com.snatik.matches;

import android.app.Application;

import com.snatik.matches.common.Stats;
import com.snatik.matches.utils.FontLoader;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;

public class GameApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		FontLoader.loadFonts(this);
		Stats.getInstance().setContext(this);

		Permission[] permissions = new Permission[] { Permission.PUBLIC_PROFILE };
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
			.setAppId("1537893283094745")
			.setNamespace("memory_game_kids")
			.setPermissions(permissions)
			.build();
		SimpleFacebook.setConfiguration(configuration);
	}
}
