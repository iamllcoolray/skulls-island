package com.hibiscusgames.skullsisland.game.sprites.utilities;

import com.badlogic.gdx.physics.box2d.*;
import com.hibiscusgames.skullsisland.game.sprites.entities.Player;
import com.hibiscusgames.skullsisland.game.sprites.props.Ball;

public class CollisionListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getBody().getUserData();
        Object userDataB = fixtureB.getBody().getUserData();

        if (userDataA == null || userDataB == null) {
            return;
        }

        if ((userDataA instanceof Player && userDataB instanceof Ball || (userDataB instanceof Player && userDataA instanceof Ball))){
            contact.setEnabled(false);
            return;
        }

        Player player = null;
        Ball ball = null;
        String otherType = null;

        if (userDataA instanceof Player) {
            player = (Player) userDataA;
            otherType = (userDataB instanceof String) ? (String) userDataB : null;
        } else if (userDataB instanceof Player) {
            player = (Player) userDataB;
            otherType = (userDataA instanceof String) ? (String) userDataA : null;
        }

        if (userDataA instanceof Ball) {
            ball = (Ball) userDataA;
            otherType = (userDataB instanceof String) ? (String) userDataB : null;
        } else if (userDataB instanceof Ball) {
            ball = (Ball) userDataB;
            otherType = (userDataA instanceof String) ? (String) userDataA : null;
        }

        if (player != null && otherType != null) {
            switch (otherType) {
                case "boundary":
                    System.out.println("Player hit boundary!");
                    player.onBoundaryCollision();
                    break;
                case "enemy":
                    System.out.println("Player hit enemy!");
                    player.onEnemyCollision();
                    break;
                case "item":
                    System.out.println("Player collected item!");
                    player.onItemCollision();
                    break;
            }
        }

        if (ball != null && otherType != null) {
            switch (otherType) {
                case "boundary":
                    System.out.println("Ball hit boundary!");
                    ball.onBoundaryCollision();
                    ball.setDestroy();
                    break;
                case "enemy":
                    System.out.println("Ball hit enemy!");
                    ball.onEnemyCollision();
                    ball.setDestroy();
                    break;
                case "item":
                    System.out.println("Ball collected item!");
                    ball.onItemCollision();
                    ball.setDestroy();
                    break;
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
