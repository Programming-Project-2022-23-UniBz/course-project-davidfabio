package com.davidfabio;

import com.badlogic.gdx.Game;
import com.davidfabio.ui.GameScreen;
import com.davidfabio.ui.MainMenuScreen;

public class Duality extends Game {
	@Override
	public void create() {
		//setScreen(new MainMenuScreen());
		setScreen(new GameScreen());
	}
}
