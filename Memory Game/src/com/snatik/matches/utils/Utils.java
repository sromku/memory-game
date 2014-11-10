package com.snatik.matches.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.snatik.matches.common.Shared;

public class Utils {

	public static int px(int dp) {
		return (int) (Shared.context.getResources().getDisplayMetrics().density * dp);
	}
	
	public static int screenWidth() {
		return Shared.context.getResources().getDisplayMetrics().widthPixels;
	}
	
	public static int screenHeight() {
		return Shared.context.getResources().getDisplayMetrics().heightPixels;
	}
	
	public static Bitmap scaleDown(int resource, int reqWidth, int reqHeight) {
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inJustDecodeBounds = true;
         BitmapFactory.decodeResource(Shared.context.getResources(), resource);

         // Calculate inSampleSize
         options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

         // Decode bitmap with inSampleSize set
         options.inJustDecodeBounds = false;
         return BitmapFactory.decodeResource(Shared.context.getResources(), resource, options);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
}
