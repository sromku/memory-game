package com.snatik.matches.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.snatik.matches.R;

public class TileView extends FrameLayout {

	private RelativeLayout mTopImage;
	private ImageView mTileImage;
	private boolean mFlippedDown = true;

	public TileView(Context context) {
		this(context, null);
	}

	public TileView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public static TileView fromXml(Context context, ViewGroup parent) {
		return (TileView) LayoutInflater.from(context).inflate(R.layout.tile_view, parent, false);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTopImage = (RelativeLayout) findViewById(R.id.image_top);
		mTileImage = (ImageView) findViewById(R.id.image);
	}

	public void setTileImage(Bitmap bitmap) {
		mTileImage.setImageBitmap(bitmap);
	}

	public void flipUp() {
		mFlippedDown = false;
		mTopImage.setVisibility(View.GONE);
	}

	public void flipDown() {
		mFlippedDown = true;
		mTopImage.setVisibility(View.VISIBLE);
	}
	
	public boolean isFlippedDown() {
		return mFlippedDown;
	}
}
