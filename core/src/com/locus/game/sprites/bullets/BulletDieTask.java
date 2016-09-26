package com.locus.game.sprites.bullets;

import com.badlogic.gdx.utils.Timer;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * BulletDieTask
 */

class BulletDieTask extends Timer.Task {

    private Bullet bullet;

    BulletDieTask(Bullet bullet) {
        this.bullet = bullet;
    }

    @Override
    public void run() {
        if (bullet != null) {
            bullet.kill();
        }
    }
}
