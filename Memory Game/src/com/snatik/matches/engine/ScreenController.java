package com.snatik.matches.engine;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;
import com.snatik.matches.fragments.DifficultySelectFragment;
import com.snatik.matches.fragments.GameFragment;
import com.snatik.matches.fragments.MenuFragment;
import com.snatik.matches.fragments.ThemeSelectFragment;

public class ScreenController {

	private static ScreenController mInstance = null;
	private static List<Screen> openedScreens = new ArrayList<Screen>();

	private ScreenController() {
	}

	public static ScreenController getInstance() {
		if (mInstance == null) {
			mInstance = new ScreenController();
		}
		return mInstance;
	}

	public static enum Screen {
		MENU,
		GAME,
		DIFFICULTY,
		THEME_SELECT
	}

	public void openScreen(Screen screen) {
		Log.i("my_tag", "open screen: " + screen);
		Fragment fragment = getFragment(screen);
		FragmentManager fragmentManager = Shared.activity.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, fragment);
		fragmentTransaction.commit();
		openedScreens.add(screen);
	}

	public boolean onBack() {
		if (openedScreens.size() > 0) {
			openedScreens.remove(openedScreens.size() - 1);
			if (openedScreens.size() == 0) {
				return true;
			}
			Screen screen = openedScreens.get(openedScreens.size() - 1);
			openedScreens.remove(openedScreens.size() - 1);
			openScreen(screen);
			return false;
		}
		return true;
	}

	private Fragment getFragment(Screen screen) {
		switch (screen) {
		case MENU:
			return new MenuFragment();
		case DIFFICULTY:
			return new DifficultySelectFragment();
		case GAME:
			return new GameFragment();
		case THEME_SELECT:
			return new ThemeSelectFragment();
		default:
			break;
		}
		return null;
	}
}
