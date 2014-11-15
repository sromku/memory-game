package com.snatik.matches.common;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.snatik.matches.R;

/**
 * @author sromku
 * @see https://developers.google.com/mobile-ads-sdk/docs/admob/fundamentals
 */
public class Ads {

	private static final String AD_UNIT_ID = "ca-app-pub-8150157395642941/8649170010";
	private static Ads mInstnace = null;
	private AdView mAdView;
	private List<String> keywords;
	private boolean enableAds = true;
	private final boolean mGooglePlayServiceAvailable;

	private Ads() {
		mGooglePlayServiceAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(Shared.context) == ConnectionResult.SUCCESS;
	}

	public static Ads getInstance() {
		if (mInstnace == null) {
			mInstnace = new Ads();
		}
		return mInstnace;
	}

	public Ads setEnabled(boolean enabled) {
		enableAds = enabled;
		return this;
	}

	public Ads setBannerView(AdView adView) {
		mAdView = adView;
		mAdView.setAdUnitId(AD_UNIT_ID);
		String[] stringArray = Shared.context.getResources().getStringArray(R.array.ad_kewords);
		keywords = Arrays.asList(stringArray);
		return this;
	}

	public static InterstitialAd createInterstitialAd(Context context) {
		InterstitialAd interstitialAd = new InterstitialAd(context);
		interstitialAd.setAdUnitId(AD_UNIT_ID);
		return interstitialAd;
	}

	public void showBannerAd() {
		if (enableAds) {
			mAdView.loadAd(createAdRequest());
		} else {
			if (mAdView != null) {
				mAdView.setVisibility(View.GONE);
			}
		}
	}

	public void showInterstitialAd() {
		if (enableAds && mGooglePlayServiceAvailable) {
			final InterstitialAd interstitialAd = createInterstitialAd(Shared.activity);
			interstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
					interstitialAd.show();
				}
			});
			interstitialAd.loadAd(createAdRequest());
		}
	}
	
	private AdRequest createAdRequest() {
		Builder builder = new AdRequest.Builder();
		for (String keyword : keywords) {
			builder.addKeyword(keyword);
		}
		AdRequest adRequest = builder.build();
		return adRequest;
	}
}
