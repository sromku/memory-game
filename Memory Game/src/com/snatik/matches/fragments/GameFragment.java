package com.snatik.matches.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.snatik.matches.R;
import com.snatik.matches.common.Shared;
import com.snatik.matches.events.engine.FlipDownCardsEvent;
import com.snatik.matches.events.engine.GameWonEvent;
import com.snatik.matches.events.engine.HidePairCardsEvent;
import com.snatik.matches.model.Game;
import com.snatik.matches.ui.BoardView;
import com.snatik.matches.ui.PopupManager;

public class GameFragment extends BaseFragment {

	private BoardView mBoardView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.game_fragment, container, false);
		mBoardView = BoardView.fromXml(getActivity().getApplicationContext(), view);
		view.addView(mBoardView);
		
		// set background
//		ImageView imageView = (ImageView) view.findViewById(R.id.background_image);
//		Game game = Shared.engine.getActiveGame();
//		Bitmap bitmap = Themes.getBackgroundImage(game.theme);
//		imageView.setImageBitmap(bitmap);
		
		// build board
		buildBoard();
		Shared.eventBus.listen(FlipDownCardsEvent.TYPE, this);
		Shared.eventBus.listen(HidePairCardsEvent.TYPE, this);
		Shared.eventBus.listen(GameWonEvent.TYPE, this);
		return view;
	}
	
	@Override
	public void onDestroy() {
		Shared.eventBus.unlisten(FlipDownCardsEvent.TYPE, this);
		Shared.eventBus.unlisten(HidePairCardsEvent.TYPE, this);
		Shared.eventBus.unlisten(GameWonEvent.TYPE, this);
		super.onDestroy();
	}

	private void buildBoard() {
		Game game = Shared.engine.getActiveGame();
		mBoardView.setBoard(game.board, game.boardArrangment);
	}
	
	@Override
	public void onEvent(GameWonEvent event) {
		PopupManager.showPopupWon();
	}
	
	@Override
	public void onEvent(FlipDownCardsEvent event) {
		mBoardView.flipDownAll();
	}
	
	@Override
	public void onEvent(HidePairCardsEvent event) {
		mBoardView.hideCards(event.id1, event.id2);
	}
	
}
