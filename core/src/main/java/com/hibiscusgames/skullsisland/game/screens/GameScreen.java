package com.hibiscusgames.skullsisland.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hibiscusgames.skullsisland.game.SkullsIsland;
import com.hibiscusgames.skullsisland.game.sprites.entities.Player;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final SkullsIsland game;

    private OrthographicCamera orthographicCamera;
    private Viewport viewport;
    private final float SPEED = 10f;

    private TmxMapLoader tmxMapLoader;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private MapProperties mapProperties;
    private int tiledMapTileWidth, tiledMapTileHeight, tiledMapPixelWidth, tiledMapPixelHeight;
    private float totaltiledMapPixelsWidth, totaltiledMapPixelsHeight, adjustedTiledMapMetersWidth, adjustedTiledMapMetersHeight;

    private World world;
    private Box2DDebugRenderer box2DDebugRenderer;

    private Player player;

    private Music music;

    private Cursor cursor;
    private Pixmap crosshairs;
    private Pixmap resizedCrosshairs;
    private final byte RESIZE = 64;
    private int offsetX;
    private int offsetY;

    public GameScreen(SkullsIsland game) {
        this.game = game;

        orthographicCamera = new OrthographicCamera();

        viewport = new FillViewport(SkullsIsland.V_WIDTH / SkullsIsland.PPM, SkullsIsland.V_HEIGHT / SkullsIsland.PPM, orthographicCamera);

        tmxMapLoader = new TmxMapLoader();
        tiledMap = tmxMapLoader.load("maps/island.tmx");
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1 / SkullsIsland.PPM);

        mapProperties = tiledMap.getProperties();
        tiledMapTileWidth = mapProperties.get("width", Integer.class);
        tiledMapTileHeight = mapProperties.get("height", Integer.class);
        tiledMapPixelWidth = mapProperties.get("tilewidth", Integer.class);
        tiledMapPixelHeight = mapProperties.get("tileheight", Integer.class);

        totaltiledMapPixelsWidth = tiledMapTileWidth * tiledMapPixelWidth;
        totaltiledMapPixelsHeight = tiledMapTileHeight * tiledMapPixelHeight;
        adjustedTiledMapMetersWidth = totaltiledMapPixelsWidth / SkullsIsland.PPM;
        adjustedTiledMapMetersHeight = totaltiledMapPixelsHeight / SkullsIsland.PPM;

        orthographicCamera.position.set(adjustedTiledMapMetersWidth / 2, adjustedTiledMapMetersHeight / 2, 0);

        player = new Player(this);

        music = SkullsIsland.assetManager.get(SkullsIsland.MUSIC_PATH + "pirate.ogg");
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();
    }

    @Override
    public void show() {
        // Prepare your screen here.
        crosshairs = new Pixmap(Gdx.files.internal("sprites/props/crosshairs.png"));
        resizedCrosshairs = new Pixmap(RESIZE, RESIZE, crosshairs.getFormat());

        offsetX = (RESIZE - crosshairs.getWidth()) / 2;
        offsetY = (RESIZE - crosshairs.getHeight()) / 2;

        resizedCrosshairs.drawPixmap(crosshairs, offsetX, offsetY, 0, 0, crosshairs.getWidth(), crosshairs.getHeight());

        cursor = Gdx.graphics.newCursor(resizedCrosshairs, resizedCrosshairs.getWidth() / 2, resizedCrosshairs.getHeight() / 2);

        Gdx.graphics.setCursor(cursor);

        crosshairs.dispose();
        resizedCrosshairs.dispose();
    }

    private void inputHandler(float delta){
        if(player.currentState != Player.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                player.moveUp(delta, SPEED);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                player.moveLeft(delta, SPEED);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player.moveRight(delta, SPEED);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                player.moveDown(delta, SPEED);
            }
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
                player.throwBall();
            }
        }
    }

    public void update(float delta){
        inputHandler(delta);

        player.update(delta);

        orthographicCamera.position.set(player.getCenterX(), player.getCenterY(), 0);

        orthographicCamera.update();
        orthogonalTiledMapRenderer.setView(orthographicCamera);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        orthogonalTiledMapRenderer.render();

        game.spriteBatch.setProjectionMatrix(orthographicCamera.combined);
        game.spriteBatch.begin();
        player.draw(game.spriteBatch);
        game.spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
        viewport.update(width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        tiledMap.dispose();
        orthogonalTiledMapRenderer.dispose();
        player.dispose();
        cursor.dispose();
    }

    public float getAdjustedTiledMapMetersHeight() {
        return adjustedTiledMapMetersHeight;
    }

    public float getAdjustedTiledMapMetersWidth() {
        return adjustedTiledMapMetersWidth;
    }
}
