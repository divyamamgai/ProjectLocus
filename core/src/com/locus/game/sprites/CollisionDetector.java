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
    public static final short CATEGORY_PLANET = 0x1;
    // Enable Collision with all.
    public static final short MASK_PLANET = -1;
    public static final short CATEGORY_MOON = 0x2;
    public static final short MASK_MOON = -1;
    public static final short CATEGORY_ASTEROID = 0x4;
    public static final short MASK_ASTEROID = -1;
    public static final short CATEGORY_BULLET = 0x8;
    // Bullets do not collide with each other.
    public static final short MASK_BULLET = ~CATEGORY_BULLET;
    public static final short CATEGORY_SHIP = 0x16;
    public static final short MASK_SHIP = -1;

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
            // We now know with absolute certainty that one is a Bullet.
            Bullet bullet;
            // We technically also know that remaining object is Entity but we try to be sure
            // just in case.
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
            switch (entityA.definition.type) {
                case Ship:
                    float speed2;
                    switch (entityB.definition.type) {
                        case Ship:
                            break;
                        case Planet:
                            speed2 = entityA.body.getLinearVelocity().len2();
                            if ((speed2 > 900f) && ((entityA.health -= 75f) <= 0)) {
                                entityA.kill();
                            } else if ((entityA.health -= 5f) <= 0) {
                                entityA.kill();
                            }
                            break;
                        case Moon:
                            speed2 = entityA.body.getLinearVelocity().len2();
                            if ((speed2 > 900f) && ((entityA.health -= 25f) <= 0)) {
                                entityA.kill();
                            } else if ((entityA.health -= 1f) <= 0) {
                                entityA.kill();
                            }
                            break;
                    }
                    break;
                case Planet:
                    switch (entityB.definition.type) {
                        case Ship:
                            break;
                        case Planet:
                            break;
                        case Moon:
                            break;
                    }
                    break;
                case Moon:
                    switch (entityB.definition.type) {
                        case Ship:
                            break;
                        case Planet:
                            break;
                        case Moon:
                            break;
                    }
                    break;
            }
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