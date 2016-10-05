package com.locus.game.network;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Divya Mamgai on 10/5/2016.
 * Ship State
 */

public class ShipState {

    public Vector2 position;
    public float angleRad;
    public Vector2 velocity;
    public float angularVelocity;
    public boolean isThrustEnabled;
    public boolean thrustDirection;
    public boolean isRotationEnabled;
    public boolean rotationDirection;
    public boolean isFireEnabled;

    public boolean isAlive;
    public float health;
    public long score;

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
