package com.locus.game.sprites;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.locus.game.sprites.bullets.ClientBullet;

/**
 * Created by Divya Mamgai on 10/8/2016.
 * Client Collision Detector
 */

public class ClientCollisionDetector implements ContactListener {

    public ClientCollisionDetector() {
    }

    @Override
    public void beginContact(Contact contact) {
        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();
        boolean isObjectABullet = objectA instanceof ClientBullet;
        boolean isObjectBBullet = objectB instanceof ClientBullet;
        if (isObjectABullet || isObjectBBullet) {
            ClientBullet bullet;
            if (isObjectABullet) {
                bullet = (ClientBullet) objectA;
            } else {
                bullet = (ClientBullet) objectB;
            }
            bullet.kill();
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
