package com.snatik.matches;

import com.snatik.matches.common.Shared;
import com.snatik.matches.engine.Engine;
import com.snatik.matches.events.EventBus;
import com.snatik.matches.utils.FontLoader;

import android.app.Application;

public class GameApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		Shared.context = this;
		Shared.engine = Engine.getInstance();
		Shared.eventBus = EventBus.getInstance();
		FontLoader.loadFonts(Shared.context);
	}
}
