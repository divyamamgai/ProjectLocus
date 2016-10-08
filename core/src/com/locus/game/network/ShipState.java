package com.locus.game.network;

/**
 * Created by Divya Mamgai on 10/5/2016.
 * Ship State
 */

public class ShipState {

    public short ID, score;
    public float bodyX, bodyY, angleDeg, health;
    public boolean isFireEnabled;

    public ShipState() {
        bodyX = 0;
        bodyY = 0;
        angleDeg = 0;
        health = 0;
        score = 0;
        isFireEnabled = false;
    }

    ShipState(short ID, float bodyX, float bodyY, float angleDeg, float health) {
        this.ID = ID;
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.angleDeg = angleDeg;
        this.health = health;
        score = 0;
        isFireEnabled = false;
    }

}
