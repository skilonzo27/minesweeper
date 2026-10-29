package com.kilonzo;

import com.badlogic.gdx.Game;

/*
 * This class controls which game screen is shown.
 */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
