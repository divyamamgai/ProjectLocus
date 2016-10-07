package com.locus.game.network;

/**
 * Created by Divya Mamgai on 10/6/2016.
 * Bullet State
 */

public class BulletState {

    public short ID;
    public float bodyX, bodyY, angleDeg;

    public BulletState() {
        ID = 0;
        bodyX = 0;
        bodyY = 0;
        angleDeg = 0;
    }

    BulletState(short ID, float bodyX, float bodyY, float angleDeg) {
        this.ID = ID;
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.angleDeg = angleDeg;
    }

}
