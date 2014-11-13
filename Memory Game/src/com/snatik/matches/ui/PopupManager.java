package com.snatik.matches.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;
import com.snatik.matches.model.GameState;
import com.snatik.matches.utils.Utils;

public class PopupManager {

	public static void showPopupSettings() {
		RelativeLayout popupContainer = (RelativeLayout) Shared.activity.findViewById(R.id.popup_container);
		popupContainer.removeAllViews();

		// background
		ImageView imageView = new ImageView(Shared.context);
		imageView.setBackgroundColor(Color.parseColor("#88555555"));
		imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imageView.setClickable(true);
		popupContainer.addView(imageView);

		// popup
		PopupSettingsView popupSettingsView = new PopupSettingsView(Shared.context);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Utils.px(300), Utils.px(200));
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		popupContainer.addView(popupSettingsView, params);

		// animate all together
		ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(popupSettingsView, "scaleX", 0f, 1f);
		ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(popupSettingsView, "scaleY", 0f, 1f);
		ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
		animatorSet.setDuration(500);
		animatorSet.setInterpolator(new DecelerateInterpolator(2));
		animatorSet.start();
	}

	public static void showPopupWon(GameState gameState) {
		RelativeLayout popupContainer = (RelativeLayout) Shared.activity.findViewById(R.id.popup_container);
		popupContainer.removeAllViews();

		// popup
		PopupWonView popupWonView = new PopupWonView(Shared.context);
		popupWonView.setGameState(gameState);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(Utils.px(240), Utils.px(300));
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		popupContainer.addView(popupWonView, params);

		// animate all together
		ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(popupWonView, "scaleX", 0f, 1f);
		ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(popupWonView, "scaleY", 0f, 1f);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
		animatorSet.setDuration(500);
		animatorSet.setInterpolator(new DecelerateInterpolator(2));
		animatorSet.start();
	}

	public static void closePopup() {
		final RelativeLayout popupContainer = (RelativeLayout) Shared.activity.findViewById(R.id.popup_container);
		int childCount = popupContainer.getChildCount();
		if (childCount > 0) {
			View background = null;
			View viewPopup = null;
			if (childCount == 1) {
				viewPopup = popupContainer.getChildAt(0);
			} else {
				background = popupContainer.getChildAt(0);
				viewPopup = popupContainer.getChildAt(1);
			}

			AnimatorSet animatorSet = new AnimatorSet();
			ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(viewPopup, "scaleX", 0f);
			ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(viewPopup, "scaleY", 0f);
			if (childCount > 1) {
				ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(background, "alpha", 0f);
				animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator);
			} else {
				animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
			}
			animatorSet.setDuration(300);
			animatorSet.setInterpolator(new AccelerateInterpolator(2));
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					popupContainer.removeAllViews();
				}
			});
			animatorSet.start();
		}
	}

	public static boolean isShown() {
		RelativeLayout popupContainer = (RelativeLayout) Shared.activity.findViewById(R.id.popup_container);
		return popupContainer.getChildCount() > 0;
	}
}
