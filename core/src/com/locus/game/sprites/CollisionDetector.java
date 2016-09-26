package com.locus.game.sprites;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.entities.Entity;

/**
 * Created by Divya Mamgai on 9/21/2016.
 * Collision Detector
 */

public class CollisionDetector implements ContactListener {

    // Only powers of 2 up to 16th.
    public static final short CATEGORY_PLANET = 0x0001;
    public static final short MASK_PLANET = ~CATEGORY_PLANET;
    public static final short CATEGORY_ASTEROID = 0x0002;
    public static final short MASK_ASTEROID = ~CATEGORY_ASTEROID;
    public static final short CATEGORY_BULLET = 0x0004;
    public static final short MASK_BULLET = ~CATEGORY_BULLET;
    public static final short CATEGORY_SHIP = 0x0008;
    public static final short MASK_SHIP = ~CATEGORY_SHIP;

    public CollisionDetector() {
    }

    @Override
    public void beginContact(Contact contact) {
        Object objectA = contact.getFixtureA().getBody().getUserData();
        Object objectB = contact.getFixtureB().getBody().getUserData();
        boolean isObjectABullet = objectA instanceof Bullet;
        boolean isObjectBBullet = objectB instanceof Bullet;
        // Both cannot be Bullet due to Category masking.
        if (isObjectABullet || isObjectBBullet) {
            Bullet bullet;
            Entity entity = null;
            if (isObjectABullet) {
                bullet = (Bullet) objectA;
                if (objectB instanceof Entity) {
                    entity = (Entity) objectB;
                }
            } else {
                bullet = (Bullet) objectB;
                if (objectA instanceof Entity) {
                    entity = (Entity) objectA;
                }
            }
            if (entity != null) {
                if (bullet.ship != entity) {
                    switch (entity.definition.type) {
                        case Planet:
                            break;
                        case Moon:
                            break;
                        default:
                            if ((entity.health -= bullet.definition.damage) <= 0) {
                                entity.kill();
                            }
                    }
                    bullet.kill();
                }
            }
        } else {
            Entity entityA = (Entity) objectA;
            Entity entityB = (Entity) objectB;
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
