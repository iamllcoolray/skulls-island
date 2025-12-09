package com.hibiscusgames.skullsisland.game.sprites.props;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.hibiscusgames.skullsisland.game.SkullsIsland;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;
import com.hibiscusgames.skullsisland.game.sprites.entities.Player;

public class Ball extends Sprite {

    private Texture ballSprite;
    private final String SPRITE_PATH = "sprites/props/";
    private final byte SPRITE_WIDTH = 96;
    private final byte SPRITE_HEIGHT = 96;
    private final float spriteMetersSize;

    private final float SPEED = 3f;
    private Vector2 direction;
    private Vector2 spawn;
    private Vector3 mousePosition;

    private boolean destroy;

    private GameScreen gameScreen;
    private Player player;

    private World world;
    private Body body;

    private final Sound popSound;
    private final Sound hitSound;

    private float lifetime;
    private final float MAX_LIFETIME = 5f;

    public Ball(GameScreen gameScreen, Player player){
        this.gameScreen = gameScreen;
        this.world = gameScreen.getWorld();
        this.player = player;

        destroy = false;
        lifetime = 0f;

        popSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "cork-pop.ogg");
        hitSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "wet-break-2.wav");

        ballSprite = new Texture(SPRITE_PATH + "ball.png");

        setRegion(new TextureRegion(ballSprite));

        spriteMetersSize = SPRITE_WIDTH / SkullsIsland.PPM;
        setSize(spriteMetersSize, spriteMetersSize);

        setOrigin(spriteMetersSize / 2, spriteMetersSize / 2);

        float x = gameScreen.getAdjustedTiledMapMetersWidth() / 2;
        float y = gameScreen.getAdjustedTiledMapMetersHeight() / 2;

        direction = new Vector2(0, 0);
        spawn = new Vector2(0, 0);
        mousePosition = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);

        gameScreen.getOrthographicCamera().unproject(mousePosition);

        direction.x = mousePosition.x - player.getCenterX();
        direction.y = mousePosition.y - player.getCenterY();


        float length = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        if (length != 0) {
            direction.x /= length;
            direction.y /= length;
        }

        spawn.x = player.getCenterX() + direction.x;
        spawn.y = player.getCenterY() + direction.y;

        setPosition(spawn.x - x,spawn.y - y);

        createBody(spawn.x, spawn.y, spriteMetersSize);

        body.setLinearVelocity(direction.x * SPEED, direction.y * SPEED);
    }

    private void createBody(float x, float y, float size) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(size / 6);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.3f;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(this);
    }

    public void update(float delta){
        setPosition(body.getPosition().x - spriteMetersSize / 2, body.getPosition().y - spriteMetersSize / 2);

        lifetime += delta;
    }

    public void draw(SpriteBatch spriteBatch){
        super.draw(spriteBatch);
    }

    public void playPopSound(){
        popSound.play(0.5f);
    }

    public void playHitSound(){
        hitSound.play(1f);
    }

    public void onBoundaryCollision() {
        System.out.println("Ball collided with boundary!");
        playPopSound();
    }

    public void onEnemyCollision() {
        System.out.println("Ball collided with enemy!");
        playHitSound();
    }

    public void onItemCollision() {
        System.out.println("Ball collected item!");
        playPopSound();
    }

    public void setDestroy(){
        destroy = true;
    }

    public boolean shouldDestroy(){
        return destroy || lifetime > MAX_LIFETIME;
    }

    public Body getBody() {
        return body;
    }

    public void dispose(){
        ballSprite.dispose();
    }
}
