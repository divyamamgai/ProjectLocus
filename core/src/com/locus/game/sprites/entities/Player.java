package com.locus.game.sprites.entities;

import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.Level;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.tools.InputController;

/**
 * Created by Divya Mamgai on 9/21/2016.
 * Player
 */

public class Player extends Ship implements InputController.InputCallBack {

    public Player(Level level, Ship.Type shipType, float x, float y) {
        super(level, shipType, x, y);
    }

    @Override
    public void applyRotation(boolean isClockwise) {
        body.applyAngularImpulse((isClockwise ? -1 : 1) * definition.rotationSpeed, true);
    }

    @Override
    public void applyThrust(boolean isForward) {
        Vector2 playerPosition = body.getPosition();
        float angleRad = body.getAngle();
        if (isForward) {
            body.applyLinearImpulse(thrustVelocity.set(0, definition.thrustSpeed)
                    .rotateRad(angleRad), playerPosition, true);
        } else {
            body.applyLinearImpulse(thrustVelocity.set(0, definition.thrustSpeed)
                    .rotateRad(angleRad).scl(-1f), playerPosition, true);
        }
    }

    @Override
    public void fire() {
        fireBullet(Bullet.Type.Small);
    }

}
