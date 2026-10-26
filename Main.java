package com.kilonzo;

import com.badlogic.gdx.Game;

/*
 * This class controls which game screen
 * is being shown (pause screen, gameplay screen,
 * game over screen, inventory screen, etc.)
 */
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new GameplayScreen());
    }
}
