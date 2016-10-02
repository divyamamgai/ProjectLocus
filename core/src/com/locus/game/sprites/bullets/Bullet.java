package com.locus.game.sprites.bullets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Ship;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Bullet
 */

public class Bullet extends Sprite {

    public enum Type {

        Normal,
        Medium;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    private Level level;
    public Ship ship;
    public Body body;
    public BulletLoader.Definition definition;
    private boolean isAlive = true;

    public Bullet(Level level, Type type, Ship ship, Vector2 position, float angleRad) {

        this.level = level;
        this.ship = ship;

        definition = level.projectLocus.bulletLoader.get(type);

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        definition.bodyDef.position.set(position);
        definition.bodyDef.angle = angleRad;
        definition.bodyDef.linearVelocity.set(0, definition.speed).rotateRad(angleRad);

        body = level.world.createBody(definition.bodyDef);
        body.setUserData(this);
        body.applyLinearImpulse(ship.body.getLinearVelocity(), position, true);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();

        level.timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void update() {
        Vector2 bodyPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(bodyPosition.x, bodyPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        Vector2 bodyPosition = body.getPosition();
        if (frustum.boundsInFrustum(bodyPosition.x, bodyPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

    public void kill() {
        if (isAlive) {
            isAlive = false;
            level.destroyBulletStack.push(this);
        }
    }

    public void destroy() {
        level.world.destroyBody(body);
        level.bulletList.remove(this);
    }

}
