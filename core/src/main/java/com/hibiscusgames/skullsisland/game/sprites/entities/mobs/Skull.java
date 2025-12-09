package com.hibiscusgames.skullsisland.game.sprites.entities.mobs;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.hibiscusgames.skullsisland.game.SkullsIsland;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;
import com.hibiscusgames.skullsisland.game.sprites.entities.Player;
import com.hibiscusgames.skullsisland.game.sprites.utilities.Animate;

public class Skull extends Sprite {
    private boolean destroy;

    private GameScreen gameScreen;
    private Player player;

    private final Texture skullSheet;
    private final Animation<TextureRegion> skullAnimation;
    private TextureRegion region;
    private TextureRegion currentFrame;
    private final String SPRITE_PATH = "sprites/mobs/";
    private final int SPRITE_WIDTH = 96;
    private final int SPRITE_HEIGHT = 132;
    private final float spriteMetersSizeWidth;
    private final float spriteMetersSizeHeight;
    private final int FRAME_COUNT = 10;
    private final float DURATION = 0.15f;

    private final float SPEED = 3f;

    private final Animate animate;
    private float animationDuration;

    private Vector2 direction;

    private World world;
    private Body body;


    private final Sound walkingSound;
    private long walkingSoundID;
    private boolean isWalkingSoundPlaying;

    private final Sound voiceSound;

    public Skull(GameScreen gameScreen, Player player, float x, float y){
        this.gameScreen = gameScreen;
        this.world = gameScreen.getWorld();
        this.player = player;

        destroy = false;

        animate = new Animate();

        walkingSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "skeleton-walk.mp3");
        walkingSoundID = -1;
        isWalkingSoundPlaying = false;

        voiceSound = SkullsIsland.assetManager.get(SkullsIsland.SFX_PATH + "skeleton-sounds.ogg");

        skullSheet = new Texture(SPRITE_PATH + "skull.png");
        skullAnimation = animate.animate(skullSheet, SPRITE_WIDTH, SPRITE_HEIGHT, FRAME_COUNT, DURATION);

        direction = new Vector2(0, 0);


        currentFrame = skullAnimation.getKeyFrame(0);
        setRegion(currentFrame);

        spriteMetersSizeWidth = SPRITE_WIDTH / SkullsIsland.PPM;
        spriteMetersSizeHeight = SPRITE_HEIGHT / SkullsIsland.PPM;
        setSize(spriteMetersSizeWidth, spriteMetersSizeHeight);

        setOrigin(spriteMetersSizeWidth / 2, spriteMetersSizeHeight / 2);

        setPosition(x, y);

        createBody(x, y, spriteMetersSizeWidth, spriteMetersSizeHeight);
    }

    private void createBody(float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 3.5f, height / 3.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution = 0.3f;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setUserData("enemy");
    }

    public void followPlayer(){
        direction.x = player.getCenterX() - body.getPosition().x;
        direction.y = player.getCenterY() - body.getPosition().y;

        float length = (float) Math.sqrt(direction.x * direction.x + direction.y * direction.y);
        if (length > 0.5f) {
            direction.x /= length;
            direction.y /= length;

            body.setLinearVelocity(direction.x * SPEED, direction.y * SPEED);
            playWalkingSound();
        } else {
            body.setLinearVelocity(0, 0);
            stopWalkingSound();
        }
    }

    public void update(float delta){
        animationDuration += delta;

        followPlayer();

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);

        currentFrame = skullAnimation.getKeyFrame(animationDuration, true);
        setRegion(currentFrame);
    }

    public void playWalkingSound() {
        if (!isWalkingSoundPlaying) {
            walkingSoundID = walkingSound.loop(0.3f);
            isWalkingSoundPlaying = true;
        }
    }

    public void stopWalkingSound() {
        if (isWalkingSoundPlaying) {
            walkingSound.stop(walkingSoundID);
            isWalkingSoundPlaying = false;
        }
    }

    public void playVoiceSound(){
        voiceSound.play(1f);
    }

    public void draw(SpriteBatch spriteBatch){
        super.draw(spriteBatch);
    }

    public void setDestroy(){
        destroy = true;
    }

    public boolean shouldDestroy(){
        return destroy;
    }

    public Body getBody(){
        return body;
    }

    public void dispose(){
        skullSheet.dispose();
    }
}
