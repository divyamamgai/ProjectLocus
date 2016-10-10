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
                if (bullet.getShip() != entity) {
                    switch (entity.getType()) {
                        case Planet:
                        case Moon:
                            break;
                        case Asteroid:
                            entity.reduceHealth(bullet.getDamage());
                            switch (bullet.getType()) {
                                case Normal:
                                    bullet.getShip().getShipState().score += 5;
                                    break;
                                default:
                                    bullet.getShip().getShipState().score += 15;
                                    break;
                            }
                            break;
                        default:
                            entity.reduceHealth(bullet.getDamage());
                            switch (bullet.getType()) {
                                case Normal:
                                    bullet.getShip().getShipState().score += 10;
                                    break;
                                default:
                                    bullet.getShip().getShipState().score += 30;
                                    break;
                            }
                    }
                    bullet.kill();
                }
            }
        } else {
            if (objectA != null && objectB != null) {
                Entity entityA = (Entity) objectA;
                Entity entityB = (Entity) objectB;
                switch (entityA.getDefinition().type) {
                    case Ship:
                        switch (entityB.getDefinition().type) {
                            case Ship:
                                break;
                            case Planet:
                                entityA.reduceHealth(
                                        entityA.getBody().getLinearVelocity().len2()
                                                > 900f ? 75f : 25f);
                                break;
                            case Moon:
                                entityA.reduceHealth(entityA.getBody().getLinearVelocity().len2()
                                        > 900f ? 30f : 15f);
                                break;
                            case Asteroid:
                                entityA.reduceHealth(entityA.getBody().getLinearVelocity().len2()
                                        > 900f ? 50f : 25f);
                                entityB.kill();
                                break;
                        }
                        break;
                    case Planet:
                        switch (entityB.getDefinition().type) {
                            case Ship:
                                entityB.reduceHealth(entityB.getBody().getLinearVelocity().len2()
                                        > 900f ? 75f : 25f);
                                break;
                            case Planet:
                                break;
                            case Moon:
                                break;
                            case Asteroid:
                                entityB.kill();
                                break;
                        }
                        break;
                    case Moon:
                        switch (entityB.getDefinition().type) {
                            case Ship:
                                entityB.reduceHealth(entityB.getBody().getLinearVelocity().len2()
                                        > 900f ? 30f : 15f);
                                break;
                            case Planet:
                                break;
                            case Moon:
                                break;
                            case Asteroid:
                                entityB.kill();
                                break;
                        }
                        break;
                    case Asteroid:
                        switch (entityB.getDefinition().type) {
                            case Ship:
                                entityB.reduceHealth(entityB.getBody().getLinearVelocity().len2()
                                        > 900f ? 50f : 25f);
                            case Planet:
                            case Moon:
                            case Asteroid:
                                entityA.kill();
                                break;
                        }
                        break;
                }
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