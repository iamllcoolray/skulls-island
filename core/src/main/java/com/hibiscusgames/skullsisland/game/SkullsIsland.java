package com.hibiscusgames.skullsisland.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SkullsIsland extends Game {
    public static final float V_WIDTH = 832f;
    public static final float V_HEIGHT = 832f;
    public static final float PPM = 100f;

    public SpriteBatch spriteBatch;

    public static AssetManager assetManager;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        assetManager = new AssetManager();

        assetManager.finishLoading();

        setScreen(new GameScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        assetManager.dispose();
        spriteBatch.dispose();
    }

    @Override
    public void render() {
        super.render();
    }
}
