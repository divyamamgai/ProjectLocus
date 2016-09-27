package com.locus.game.sprites.entities;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.screens.PlayScreen;
import com.locus.game.sprites.bullets.Bullet;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class Ship extends Entity {

    public enum Type {

        Human,
        Alien;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    //    static final float VELOCITY_IMPULSE = 8f;
    static final float THRUST_SPEED = 24f;
    static final Vector2 THRUST_VELOCITY = new Vector2(0, THRUST_SPEED);
    private static final float MAX_SPEED = 64f;
    private static final float MAX_SPEED2 = MAX_SPEED * MAX_SPEED;
    static final float ANGULAR_IMPULSE = 16f;

    private static final float LINEAR_DAMPING = 1f;
    private static final float ANGULAR_DAMPING = 4f;

    private Vector2 bulletPosition;
    private short bulletsFired;

    public Ship(PlayScreen playScreen, Ship.Type type, float x, float y) {

        this.playScreen = playScreen;

        definition = playScreen.entityLoader.get(Entity.Type.Ship, type.ordinal());

        setTexture(definition.texture);
        setRegion(0, 0, definition.texture.getWidth(), definition.texture.getHeight());
        setSize(definition.width, definition.height);

        body = playScreen.gameWorld.createBody(definition.bodyDef);

        body.setTransform(x, y, 0);
        body.setLinearDamping(LINEAR_DAMPING);
        body.setAngularDamping(ANGULAR_DAMPING);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update();

        bulletPosition = new Vector2(0, 0);
        bulletsFired = 0;
        health = definition.health;

    }

    void fire(Bullet.Type type) {
        if (bulletsFired <= 2) {
            Vector2 shipPosition = body.getPosition();
            float angleRad = body.getAngle();
            for (Vector2 weaponPosition : definition.weaponPositionMap.get(type)) {
                playScreen.bulletList.add(new Bullet(playScreen, type, this,
                        bulletPosition.set(weaponPosition).rotateRad(angleRad).add(shipPosition),
                        angleRad));
            }
        } else if (bulletsFired >= 6) {
            bulletsFired = -1;
        }
        bulletsFired++;
    }

    @Override
    public void drawHealth() {
        Vector2 shipPosition = body.getPosition();
        playScreen.game.spriteBatch.draw(playScreen.healthBackgroundTexture, shipPosition.x - 3f, shipPosition.y + 3f, 6f, 0.5f);
        playScreen.game.spriteBatch.draw(playScreen.healthForegroundTexture, shipPosition.x - 3f, shipPosition.y + 3f, 6f * (health / 200f), 0.5f);
    }

    @Override
    public void update() {
        Vector2 linearVelocity = body.getLinearVelocity();
        float speed2 = linearVelocity.len2();
        if (speed2 > MAX_SPEED2) {
            body.setLinearVelocity(linearVelocity.scl(MAX_SPEED2 / speed2));
        }
        Vector2 playerPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(playerPosition.x, playerPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public boolean inFrustum(Frustum frustum) {
        Vector2 playerPosition = body.getPosition();
        return frustum.boundsInFrustum(playerPosition.x, playerPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0);
    }

    @Override
    public void kill() {
        if (isAlive) {
            isAlive = false;
            playScreen.destroyEntityStack.push(this);
        }
    }

    @Override
    public void destroy() {
        playScreen.gameWorld.destroyBody(body);
        playScreen.entityList.remove(this);
    }

}
