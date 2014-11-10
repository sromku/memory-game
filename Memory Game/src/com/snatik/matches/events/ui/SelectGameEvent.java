package com.snatik.matches.events.ui;

import com.snatik.matches.events.AbstractEvent;
import com.snatik.matches.events.EventObserver;
import com.snatik.matches.model.Game;

/**
 * When the 'back to menu' was pressed.
 */
public class SelectGameEvent extends AbstractEvent {

	public static final String TYPE = SelectGameEvent.class.getName();

	public final Game game;
	
	public SelectGameEvent(Game game) {
		this.game = game;
	}
	
	@Override
	protected void fire(EventObserver eventObserver) {
		eventObserver.onEvent(this);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
