package com.locus.game.network;

import com.locus.game.sprites.bullets.Bullet;

/**
 * Created by Divya Mamgai on 10/6/2016.
 * Bullet State
 */

public class BulletState {

    public short ID;
    public float bodyX, bodyY, angleDeg;
    public Bullet.Type type;

    public BulletState() {
        ID = 0;
        bodyX = 0;
        bodyY = 0;
        angleDeg = 0;
        type = Bullet.Type.Normal;
    }

    BulletState(short ID, float bodyX, float bodyY, float angleDeg, Bullet.Type type) {
        this.ID = ID;
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.angleDeg = angleDeg;
        this.type = type;
    }

}
