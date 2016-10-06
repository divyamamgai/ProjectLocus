package com.locus.game.network;

/**
 * Created by Divya Mamgai on 10/5/2016.
 * Ship State
 */

public class MoonState {

    public float x, y, bodyX, bodyY, angleDeg;

    public MoonState() {

    }

    public MoonState(float x, float y, float bodyX, float bodyY, float angleDeg) {
        this.x = x;
        this.y = y;
        this.bodyX = bodyX;
        this.bodyY = bodyY;
        this.angleDeg = angleDeg;
    }

}
