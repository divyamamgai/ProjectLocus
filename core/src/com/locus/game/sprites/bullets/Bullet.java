package com.locus.game.sprites.bullets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.locus.game.screens.PlayScreen;
import com.locus.game.sprites.entities.Ship;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Bullet
 */

public class Bullet extends Sprite {

    public enum Type {

        Small,
        Medium;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    private PlayScreen playScreen;
    public Ship ship;
    public Body body;
    public BulletLoader.Definition definition;
    private boolean isAlive = true;

    public Bullet(PlayScreen playScreen, Type type, Ship ship, Vector2 position, float angleRad) {

        this.playScreen = playScreen;
        this.ship = ship;

        definition = playScreen.bulletLoader.get(type);

        setTexture(definition.texture);
        setRegion(0, 0, definition.texture.getWidth(), definition.texture.getHeight());
        setSize(definition.width, definition.height);

        definition.bodyDef.position.set(position);
        definition.bodyDef.angle = angleRad;
        definition.bodyDef.linearVelocity.set(0, definition.speed).rotateRad(angleRad);

        body = playScreen.gameWorld.createBody(definition.bodyDef);
        body.setUserData(this);
        body.applyLinearImpulse(ship.body.getLinearVelocity(), position, true);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();

        playScreen.timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void update() {
        Vector2 bodyPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(bodyPosition.x, bodyPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    public boolean inFrustum(Frustum frustum) {
        Vector2 bodyPosition = body.getPosition();
        return frustum.boundsInFrustum(bodyPosition.x, bodyPosition.y, 0, definition.halfWidth, definition.halfHeight, 0);
    }

    public void kill() {
        if (isAlive) {
            isAlive = false;
            playScreen.destroyBulletStack.push(this);
        }
    }

    public void destroy() {
        playScreen.gameWorld.destroyBody(body);
        playScreen.bulletList.remove(this);
    }

}
