package com.hibiscusgames.skullsisland.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SkullsIsland extends Game {
    public static final float V_WIDTH = 832f;
    public static final float V_HEIGHT = 832f;
    public static final float PPM = 100f;

    public SpriteBatch spriteBatch;

    public static AssetManager assetManager;

    public static final String MUSIC_PATH = "audio/music/";
    public static final String SFX_PATH = "audio/sfx/";

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        assetManager = new AssetManager();

        assetManager.load(MUSIC_PATH + "woodland-fantasy.mp3", Music.class);
        assetManager.load(MUSIC_PATH + "pirate.ogg", Music.class);
        assetManager.load(MUSIC_PATH + "rezoner-pirates-theme.mp3", Music.class);
        assetManager.load(MUSIC_PATH + "game-over-3.mp3", Music.class);
        assetManager.load(SFX_PATH + "walking-on-grass.ogg", Sound.class);
        assetManager.load(SFX_PATH + "throw.wav", Sound.class);
        assetManager.load(SFX_PATH + "wet-break-2.wav", Sound.class);
        assetManager.load(SFX_PATH + "cork-pop.ogg", Sound.class);

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
