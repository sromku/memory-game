package com.snatik.matches.themes;

import java.util.ArrayList;
import java.util.List;

import com.snatik.matches.common.Shared;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Themes {

	public static String URI_DRAWABLE = "drawable://";

	public List<Theme> getThemes() {
		List<Theme> themes = new ArrayList<Theme>();
		themes.add(createMosterTheme());
		return themes;
	}

	public static Theme createMosterTheme() {
		Theme theme = new Theme();
		theme.name = "Mosters";
		theme.backgroundImageUrl = URI_DRAWABLE + "back_horror";
		theme.tileImageUrls = new ArrayList<String>();
		for (int i = 1; i <= 40; i++) {
			theme.tileImageUrls.add(URI_DRAWABLE + String.format("mosters_%d", i));
		}
		return theme;
	}

	public static Bitmap getBackgroundImage(Theme theme) {
		String drawableResourceName = theme.backgroundImageUrl.substring(Themes.URI_DRAWABLE.length());
		int drawableResourceId = Shared.context.getResources().getIdentifier(drawableResourceName, "drawable", Shared.context.getPackageName());
		return BitmapFactory.decodeResource(Shared.context.getResources(), drawableResourceId);
	}
	
}
