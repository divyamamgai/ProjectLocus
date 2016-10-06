package com.locus.game.network;

/**
 * Created by Divya Mamgai on 10/5/2016.
 * Ship State
 */

public class ShipState {

    public float x, y, bodyX, bodyY, angleDeg, health;
    public boolean isAlive;

    ShipState() {
        x = 0;
        y = 0;
        bodyX = 0;
        bodyY = 0;
        angleDeg = 0;
        health = 0;
        isAlive = true;
    }

    ShipState(float x, float y, float bodyX, float bodyY, float angleDeg, float health) {
        this.x = x;
        this.y = y;
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.angleDeg = angleDeg;
        this.health = health;
    }

}
