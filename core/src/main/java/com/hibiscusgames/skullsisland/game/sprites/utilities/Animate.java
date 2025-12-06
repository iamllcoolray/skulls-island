package com.hibiscusgames.skullsisland.game.sprites.utilities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Animate {
    private TextureRegion[] frames;

    public Animation<TextureRegion> animate(Texture spriteSheet, int width, int height, int frameCount, float duration){
        frames = new TextureRegion[frameCount];

        for(int i = 0; i < frameCount; i++){
            frames[i] = new TextureRegion(spriteSheet, i * width, 0, width, height);
        }

        return new Animation<>(duration, frames);
    }
}
