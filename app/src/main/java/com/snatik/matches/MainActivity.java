package com.snatik.matches;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.snatik.matches.common.Shared;
import com.snatik.matches.engine.Engine;
import com.snatik.matches.engine.ScreenController;
import com.snatik.matches.engine.ScreenController.Screen;
import com.snatik.matches.events.EventBus;
import com.snatik.matches.events.ui.BackGameEvent;
import com.snatik.matches.services.BackgroundMusicService;
import com.snatik.matches.ui.PopupManager;
import com.snatik.matches.utils.Utils;

public class MainActivity extends FragmentActivity {

	private ImageView mBackgroundImage;
	private Intent mBackgroundMusic = new Intent(this, BackgroundMusicService.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Shared.context = getApplicationContext();
		Shared.engine = Engine.getInstance();
		Shared.eventBus = EventBus.getInstance();

		setContentView(R.layout.activity_main);
		mBackgroundImage = (ImageView) findViewById(R.id.background_image);

		Shared.activity = this;
		Shared.engine.start();
		Shared.engine.setBackgroundImageView(mBackgroundImage);

		// set background
		setBackgroundImage();

		// set menu
		ScreenController.getInstance().openScreen(Screen.MENU);


	}

	@Override
	protected void onDestroy() {
		Shared.engine.stop();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (PopupManager.isShown()) {
			PopupManager.closePopup();
			if (ScreenController.getLastScreen() == Screen.GAME) {
				Shared.eventBus.notify(new BackGameEvent());
			}
		} else if (ScreenController.getInstance().onBack()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		playBackgroundMusic();
	}

	@Override
	protected void onStop() {
		super.onStop();
		stopService(new Intent(MainActivity.this, BackgroundMusicService.class));
	}

	private void setBackgroundImage() {
		Bitmap bitmap = Utils.scaleDown(R.drawable.background, Utils.screenWidth(), Utils.screenHeight());
		bitmap = Utils.crop(bitmap, Utils.screenHeight(), Utils.screenWidth());
		bitmap = Utils.downscaleBitmap(bitmap, 2);
		mBackgroundImage.setImageBitmap(bitmap);
	}

	public void playBackgroundMusic() {
		Intent intent = new Intent(MainActivity.this, BackgroundMusicService.class);
		startService(intent);
	}

}
