package com.locus.game.sprites.bullets;

import com.badlogic.gdx.utils.Timer;

/**
 * Created by Divya Mamgai on 10/8/2016.
 * Client Bullet Die Task
 */

class ClientBulletDieTask extends Timer.Task {

    private ClientBullet bullet;

    ClientBulletDieTask(ClientBullet bullet) {
        this.bullet = bullet;
    }

    @Override
    public void run() {
        if (bullet != null) {
            bullet.kill();
        }
    }
}
