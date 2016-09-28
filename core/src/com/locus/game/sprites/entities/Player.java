package com.locus.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.screens.PlayScreen;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.tools.InputController;

/**
 * Created by Divya Mamgai on 9/21/2016.
 * Player
 */

public class Player extends Ship implements InputController.InputCallBack {

    public Player(PlayScreen playScreen, Ship.Type shipType, float x, float y) {
        super(playScreen, shipType, x, y);
    }

    @Override
    public void applyRotation(boolean isClockwise) {
        body.applyAngularImpulse((isClockwise ? -1 : 1) * ANGULAR_IMPULSE, true);
    }

    @Override
    public void applyThrust(boolean isForward) {
        Vector2 playerPosition = body.getPosition();
        float angleRad = body.getAngle();
        if (isForward) {
            body.applyLinearImpulse(THRUST_VELOCITY.set(0, THRUST_SPEED).rotateRad(angleRad),
                    playerPosition, true);
        } else {
            body.applyLinearImpulse(THRUST_VELOCITY.set(0, THRUST_SPEED).rotateRad(angleRad).scl(-1f),
                    playerPosition, true);
        }
    }

    @Override
    public void fire() {
        launchBullet(Bullet.Type.Small);
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            applyThrust(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            applyThrust(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            applyRotation(false);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            applyRotation(true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            launchBullet(Bullet.Type.Small);
        }
    }

}
