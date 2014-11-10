package com.snatik.matches.fragments;

import android.support.v4.app.Fragment;

import com.snatik.matches.events.EventObserver;
import com.snatik.matches.events.engine.FlipDownCardsEvent;
import com.snatik.matches.events.engine.GameWonEvent;
import com.snatik.matches.events.engine.HidePairCardsEvent;
import com.snatik.matches.events.ui.FlipCardEvent;
import com.snatik.matches.events.ui.SelectDiffucltyEvent;
import com.snatik.matches.events.ui.SelectGameEvent;
import com.snatik.matches.events.ui.StartEvent;

public class BaseFragment extends Fragment implements EventObserver {

	@Override
	public void onEvent(FlipCardEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(SelectGameEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(HidePairCardsEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(FlipDownCardsEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(StartEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(SelectDiffucltyEvent event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onEvent(GameWonEvent event) {
		throw new UnsupportedOperationException();
	}

}
