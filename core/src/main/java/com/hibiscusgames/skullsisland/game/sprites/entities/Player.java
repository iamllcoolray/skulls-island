package com.hibiscusgames.skullsisland.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    private float spriteMetersSize;
    private final int FRAME_COUNT = 4;
    private final float DURATION = 0.15f;

    private Animate animate;
    private float animationDuration;

    private GameScreen gameScreen;

    private boolean isDead;

    private final Sound walkingSound;
    private long walkingSoundID;
    private boolean isWalkingSoundPlaying;

    private final Sound throwSound;

    private float throwCooldown;
    private float throwCooldownTime = 5.0f;

    public Player(GameScreen gameScreen) {
        this.gameScreen = gameScreen;

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

        setPosition(gameScreen.getAdjustedTiledMapMetersWidth() / 2, gameScreen.getAdjustedTiledMapMetersHeight() / 2);

        currentState = State.STANDING;
        previousState = State.STANDING;

        animationDuration = 0;

        walkingSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "walking-on-grass.ogg");
        walkingSoundID = -1;
        isWalkingSoundPlaying = false;

        throwSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "throw.wav");

        throwCooldown = 0f;
    }

    public void update(float delta){
        previousState = currentState;
        animationDuration += delta;

        if (throwCooldown > 0) {
            throwCooldown -= delta;
        }

        if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.S) && !Gdx.input.isKeyPressed(Input.Keys.D)) {
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
        return getX() + getWidth() / 2;
    }

    public float getCenterY() {
        return getY() + getHeight() / 2;
    }

    public void moveUp(float delta, float speed){
        setY(getY() + speed * delta);
        currentState = State.WALKING;
    }

    public void moveLeft(float delta, float speed){
        setX(getX() - speed * delta);
        currentState = State.WALKING;
    }

    public void moveRight(float delta, float speed){
        setX(getX() + speed * delta);
        currentState = State.WALKING;
    }

    public void moveDown(float delta, float speed){
        setY(getY() - speed * delta);
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

    public void dispose(){
        playerWalkUpSheet.dispose();
        playerWalkLeftSheet.dispose();
        playerWalkRightSheet.dispose();
        playerWalkDownSheet.dispose();
    }
}
