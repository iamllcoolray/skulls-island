package com.hibiscusgames.skullsisland.game.sprites.entities.mobs;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.World;
import com.hibiscusgames.skullsisland.game.screens.GameScreen;

public class Skull extends Sprite {
    private GameScreen gameScreen;

    private World world;

    public Skull(GameScreen gameScreen){
        this.gameScreen = gameScreen;
        this.world = gameScreen.getWorld();
    }
}
