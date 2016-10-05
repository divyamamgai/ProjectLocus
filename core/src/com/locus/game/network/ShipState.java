package com.locus.game.network;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Divya Mamgai on 10/5/2016.
 * Ship State
 */

public class ShipState {

    Vector2 position;
    float angleRad;
    Vector2 velocity;
    float angularVelocity;
    boolean isThrustEnabled;
    boolean thrustDirection;
    boolean isRotationEnabled;
    boolean rotationDirection;
    boolean isFireEnabled;

    boolean isAlive;
    float health;
    long score;

    ShipState() {
        position = new Vector2(0, 0);
        angleRad = 0;
        velocity = new Vector2(0, 0);
        angularVelocity = 0;
        isThrustEnabled = false;
        thrustDirection = false;
        isRotationEnabled = false;
        rotationDirection = false;
        isFireEnabled = false;
        isAlive = true;
        health = 0;
        score = 0;
    }

}
