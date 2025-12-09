package com.hibiscusgames.skullsisland.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.hibiscusgames.skullsisland.game.SkullsIsland;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;
import com.hibiscusgames.skullsisland.game.sprites.utilities.Animate;

public class Player extends Sprite {
    public enum State {STANDING, WALKING, DEAD}
    public State currentState;
    public State previousState;

    private final Texture player;
    private final Texture playerWalkUpSheet;
    private final Texture playerWalkLeftSheet;
    private final Texture playerWalkRightSheet;
    private final Texture playerWalkDownSheet;
    private final Animation<TextureRegion> playerWalkUp;
    private final Animation<TextureRegion> playerWalkLeft;
    private final Animation<TextureRegion> playerWalkRight;
    private final Animation<TextureRegion> playerWalkDown;
    private TextureRegion region;
    private TextureRegion currentFrame;
    private final String SPRITE_PATH = "sprites/player/";
    private final byte SPRITE_WIDTH = 96;
    private final byte SPRITE_HEIGHT = 96;
    private final float spriteMetersSize;
    private final int FRAME_COUNT = 4;
    private final float DURATION = 0.15f;

    private final Animate animate;
    private float animationDuration;

    private final GameScreen gameScreen;

    private final World world;
    private Body body;

    private boolean isDead;

    private final Sound walkingSound;
    private long walkingSoundID;
    private boolean isWalkingSoundPlaying;

    private final Sound throwSound;

    private float throwCooldown;
    private final float throwCooldownTime = 5.0f;

    public Player(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.world = gameScreen.getWorld();

        animate = new Animate();

        player = new Texture(SPRITE_PATH + "player.png");
        playerWalkUpSheet = new Texture(SPRITE_PATH + "walkUp.png");
        playerWalkLeftSheet = new Texture(SPRITE_PATH + "walkLeft.png");
        playerWalkRightSheet = new Texture(SPRITE_PATH + "walkRight.png");
        playerWalkDownSheet = new Texture(SPRITE_PATH + "walkDown.png");

        playerWalkUp = animate.animate(playerWalkUpSheet, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, DURATION);
        playerWalkLeft = animate.animate(playerWalkLeftSheet, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, DURATION);
        playerWalkRight = animate.animate(playerWalkRightSheet, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, DURATION);
        playerWalkDown = animate.animate(playerWalkDownSheet, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, DURATION);

        setRegion(new TextureRegion(player));

        spriteMetersSize = SPRITE_WIDTH / SkullsIsland.PPM;
        setSize(spriteMetersSize, spriteMetersSize);

        setOrigin(spriteMetersSize / 2, spriteMetersSize / 2);

        float x = gameScreen.getAdjustedTiledMapMetersWidth() / 2;
        float y = gameScreen.getAdjustedTiledMapMetersHeight() / 2;

        setPosition(x, y);

        createBody(x, y, spriteMetersSize);

        currentState = State.STANDING;
        previousState = State.STANDING;

        animationDuration = 0;

        walkingSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "walking-on-grass.ogg");
        walkingSoundID = -1;
        isWalkingSoundPlaying = false;

        throwSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "throw.wav");

        throwCooldown = 0f;
    }

    private void createBody(float x, float y, float size) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size / 3.25f, size / 2.25f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.0f;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData(this);
    }

    public void update(float delta){
        previousState = currentState;
        animationDuration += delta;

        setPosition(
            body.getPosition().x - getWidth() / 2,
            body.getPosition().y - getHeight() / 2
        );

        if (throwCooldown > 0) {
            throwCooldown -= delta;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.S) && !Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.setLinearVelocity(0, 0);
            currentState = State.STANDING;
        }

        currentFrame = getFrame();
        setRegion(currentFrame);
    }

    public void playWalkingSound(){
        if (!isWalkingSoundPlaying) {
            walkingSoundID = walkingSound.loop(1f);
            isWalkingSoundPlaying = true;
        }
    }

    public void stopWalkingSound(){
        if (isWalkingSoundPlaying) {
            walkingSound.stop(walkingSoundID);
            isWalkingSoundPlaying = false;
        }
    }

    public void playThrowSound(){
        throwSound.play(1f);
    }

    private TextureRegion getFrame() {
        switch (currentState) {
            case WALKING:
                if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                    region = playerWalkUp.getKeyFrame(animationDuration, true);
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    region = playerWalkLeft.getKeyFrame(animationDuration, true);
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    region = playerWalkRight.getKeyFrame(animationDuration, true);
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                    region = playerWalkDown.getKeyFrame(animationDuration, true);
                }
                playWalkingSound();
                break;
            case DEAD:
            default:
                region = new TextureRegion(player);
                stopWalkingSound();
                break;
        }

        return region;
    }

    public float getCenterX() {
        return body.getPosition().x;
    }

    public float getCenterY() {
        return body.getPosition().y;
    }

    public void moveUp(float delta, float speed){
        body.setLinearVelocity(body.getLinearVelocity().x, speed);
        currentState = State.WALKING;
    }

    public void moveLeft(float delta, float speed){
        body.setLinearVelocity(-speed, body.getLinearVelocity().y);
        currentState = State.WALKING;
    }

    public void moveRight(float delta, float speed){
        body.setLinearVelocity(speed, body.getLinearVelocity().y);
        currentState = State.WALKING;
    }

    public void moveDown(float delta, float speed){
        body.setLinearVelocity(body.getLinearVelocity().x, -speed);
        currentState = State.WALKING;
    }

    public void throwBall() {
        if (throwCooldown <= 0){
            playThrowSound();
            spawnBall();
            throwCooldown = throwCooldownTime;
        }
    }

    public void spawnBall(){
        System.out.println("Ball was thrown!!!");
    }

    public boolean canThrow(){
        return throwCooldown <= 0;
    }

    public void draw(SpriteBatch spriteBatch){
        super.draw(spriteBatch);
    }

    public State getState(){
        if(isDead){
            return State.DEAD;
        }else{
            return State.STANDING;
        }
    }

    public void onBoundaryCollision() {
        System.out.println("Player collided with boundary!");
        // Play sound, take damage, etc.
    }

    public void onEnemyCollision() {
        System.out.println("Player collided with enemy!");
        // Take damage, play hurt sound
    }

    public void onItemCollision() {
        System.out.println("Player collected item!");
        // Add to inventory, play pickup sound
    }

    public void dispose(){
        playerWalkUpSheet.dispose();
        playerWalkLeftSheet.dispose();
        playerWalkRightSheet.dispose();
        playerWalkDownSheet.dispose();
    }
}
