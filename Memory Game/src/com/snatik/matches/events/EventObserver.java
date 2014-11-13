package com.snatik.matches.events;

import com.snatik.matches.events.engine.FlipDownCardsEvent;
import com.snatik.matches.events.engine.GameWonEvent;
import com.snatik.matches.events.engine.HidePairCardsEvent;
import com.snatik.matches.events.ui.BackGameEvent;
import com.snatik.matches.events.ui.FlipCardEvent;
import com.snatik.matches.events.ui.NextGameEvent;
import com.snatik.matches.events.ui.SelectDiffucltyEvent;
import com.snatik.matches.events.ui.SelectGameEvent;
import com.snatik.matches.events.ui.StartEvent;


public interface EventObserver {

	void onEvent(FlipCardEvent event);

	void onEvent(SelectGameEvent event);

	void onEvent(HidePairCardsEvent event);

	void onEvent(FlipDownCardsEvent event);

	void onEvent(StartEvent event);

	void onEvent(SelectDiffucltyEvent event);

	void onEvent(GameWonEvent event);

	void onEvent(BackGameEvent event);

	void onEvent(NextGameEvent event);

}
