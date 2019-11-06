package com.snatik.matches.common;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.snatik.matches.R;

public class Music {

	public static boolean OFF = false;
	private static MediaPlayer mediaPlayer;

	public static void playCorrent() {
		if (!OFF) {
			MediaPlayer mp = MediaPlayer.create(Shared.context, R.raw.correct_answer);
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.reset();
					mp.release();
					mp = null;
				}

			});
			mp.start();
		}
	}

	public static void playBackgroundMusic() {
			mediaPlayer = MediaPlayer.create(Shared.context, R.raw.background_music);
			if(isMusicOn()) {
				mediaPlayer.start();
				mediaPlayer.setLooping(true);
			}
			OFF = false;
	}

	public static void showStar() {
		if (!OFF) {
			MediaPlayer mp = MediaPlayer.create(Shared.context, R.raw.star);
			mp.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.reset();
					mp.release();
					mp = null;
				}

			});
			mp.start();
		}
	}

	public static void stopBackgroundMusic(){

	    if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            OFF = true;
        }
	}

	public static boolean isMusicOn(){
		return !OFF;
	}
}
