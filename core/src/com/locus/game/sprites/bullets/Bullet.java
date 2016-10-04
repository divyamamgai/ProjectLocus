package com.locus.game.sprites.bullets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
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

    private Timer timer;
    public Ship ship;
    private Body body;
    public BulletLoader.Definition definition;
    public boolean isAlive = true;

    public Bullet(Level level, Type type, Ship ship, Vector2 position, float angleRad) {

        this.ship = ship;

        timer = new Timer();

        definition = level.projectLocus.bulletLoader.get(type);

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.world.createBody(definition.bodyDef);
        body.setTransform(position.x, position.y, angleRad);
        body.setLinearVelocity((new Vector2(0, definition.speed)).rotateRad(angleRad));
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);

        timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void resurrect(Ship ship, Vector2 position, float angleRad) {

        this.ship = ship;

        isAlive = true;

        body.setActive(true);

        setRotation(angleRad * MathUtils.radiansToDegrees);

        body.setTransform(position.x, position.y, angleRad);
        body.setLinearVelocity((new Vector2(0, definition.speed)).rotateRad(angleRad));

        timer.clear();
        timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void update() {
        Vector2 bodyPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(bodyPosition.x, bodyPosition.y);
        // We do not need the bullets to rotate so why take the extra overhead.
//        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
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
            timer.clear();
            isAlive = false;
        }
    }

    public void killBody() {
        body.setAwake(false);
        body.setActive(false);
    }

}
