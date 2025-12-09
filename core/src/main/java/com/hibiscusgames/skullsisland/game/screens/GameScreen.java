package com.hibiscusgames.skullsisland.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hibiscusgames.skullsisland.game.SkullsIsland;
import com.hibiscusgames.skullsisland.game.sprites.entities.Player;
import com.hibiscusgames.skullsisland.game.sprites.props.Ball;
import com.hibiscusgames.skullsisland.game.sprites.utilities.CollisionListener;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private final SkullsIsland game;

    private final OrthographicCamera orthographicCamera;
    private final Viewport viewport;
    private final float SPEED = 10f;

    private final TmxMapLoader tmxMapLoader;
    private final TiledMap tiledMap;
    private final OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;
    private final MapProperties mapProperties;
    private final int tiledMapTileWidth;
    private final int tiledMapTileHeight;
    private final int tiledMapPixelWidth;
    private final int tiledMapPixelHeight;
    private final float totaltiledMapPixelsWidth;
    private final float totaltiledMapPixelsHeight;
    private final float adjustedTiledMapMetersWidth;
    private final float adjustedTiledMapMetersHeight;

    private final World world;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private MapLayer boundaryLayer;

    private final Player player;
    private Array<Ball> balls;
    private Array<Body> bodiesToDestroy;

    private final Music music;

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

        world = new World(new Vector2(0, 0), true);
        box2DDebugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new CollisionListener());

        createBoundaries();

        player = new Player(this);
        balls = new Array<>();
        bodiesToDestroy = new Array<>();

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

    private void createBoundaries(){
        boundaryLayer = tiledMap.getLayers().get("Map Boundaries");

        if (boundaryLayer == null){
            System.out.println("Boundary layers not found.");
            return;
        }

        for (MapObject mapObject : boundaryLayer.getObjects()){
            if(mapObject instanceof RectangleMapObject){
                createRectangleBody((RectangleMapObject) mapObject);
            } else if (mapObject instanceof CircleMapObject) {
                createCircleBody((CircleMapObject) mapObject);
            }
        }
    }

    private void createRectangleBody(RectangleMapObject rectangleObject) {
        Rectangle rectangle = rectangleObject.getRectangle();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;

        float x = (rectangle.x + rectangle.width / 2) / SkullsIsland.PPM;
        float y = (rectangle.y + rectangle.height / 2) / SkullsIsland.PPM;
        bodyDef.position.set(x, y);

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            rectangle.width / 2 / SkullsIsland.PPM,
            rectangle.height / 2 / SkullsIsland.PPM
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.0f;

        body.createFixture(fixtureDef);

        String type = rectangleObject.getProperties().get("type", String.class);
        if (type == null) {
            type = "boundary";
        }

        body.setUserData(type);

        shape.dispose();
    }

    private void createCircleBody(CircleMapObject circleObject) {
        Circle circle = circleObject.getCircle();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
            circle.x / SkullsIsland.PPM,
            circle.y / SkullsIsland.PPM
        );

        Body body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(circle.radius / SkullsIsland.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;

        body.createFixture(fixtureDef);
        shape.dispose();
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

    public void addBall(Ball ball){
        balls.add(ball);
    }

    public void update(float delta){
        inputHandler(delta);
        world.step(1/60f, 6, 2);

        player.update(delta);

        for (int i = balls.size - 1; i >= 0; i--) {
            Ball ball = balls.get(i);
            ball.update(delta);

            if (ball.shouldDestroy()) {
                bodiesToDestroy.add(ball.getBody());
                ball.dispose();
                balls.removeIndex(i); // ✓ Now has index parameter
            }
        }

        for (Body body : bodiesToDestroy) {
            world.destroyBody(body);
        }
        bodiesToDestroy.clear();

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
        for(Ball ball : balls){
            ball.draw(game.spriteBatch);
        }
        game.spriteBatch.end();

        box2DDebugRenderer.render(world, orthographicCamera.combined);
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

    public float getAdjustedTiledMapMetersHeight() {
        return adjustedTiledMapMetersHeight;
    }

    public float getAdjustedTiledMapMetersWidth() {
        return adjustedTiledMapMetersWidth;
    }

    public World getWorld(){
        return  world;
    }

    public OrthographicCamera getOrthographicCamera(){
        return orthographicCamera;
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        world.dispose();
        box2DDebugRenderer.dispose();
        tiledMap.dispose();
        orthogonalTiledMapRenderer.dispose();
        player.dispose();
        cursor.dispose();
        for (Ball ball : balls) {
            ball.dispose();
        }
    }
}
