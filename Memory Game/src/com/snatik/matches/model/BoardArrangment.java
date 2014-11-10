package com.snatik.matches.model;

import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.snatik.matches.common.Shared;
import com.snatik.matches.themes.Themes;

public class BoardArrangment {

	// like {0-2, 4-3, 1-5}
	public Map<Integer, Integer> pairs;
	// like {0-mosters_20, 1-mosters_12, 2-mosters_20, ...}
	public Map<Integer, String> tileUrls;
	
	public Bitmap getTileBitmap(int id) {
		String string = tileUrls.get(id);
		if (string.contains(Themes.URI_DRAWABLE)) {
			String drawableResourceName = string.substring(Themes.URI_DRAWABLE.length());
			int drawableResourceId = Shared.context.getResources().getIdentifier(drawableResourceName, "drawable", Shared.context.getPackageName());
			return BitmapFactory.decodeResource(Shared.context.getResources(), drawableResourceId);
		}
		return null;
	}
	
	public boolean isPair(int id1, int id2) {
		return pairs.get(id1).equals(id2);
	}

}
