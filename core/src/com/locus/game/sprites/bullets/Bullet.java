package com.locus.game.sprites.bullets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
import com.locus.game.levels.Level;
import com.locus.game.network.BulletState;
import com.locus.game.sprites.entities.Ship;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Bullet
 */

public class Bullet extends Sprite {

    public static short BulletCount = 0;

    public short getID() {
        return ID;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public BulletLoader.Definition getDefinition() {
        return definition;
    }

    public void setDefinition(BulletLoader.Definition definition) {
        this.definition = definition;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public float getDamage() {
        return definition.damage;
    }

    public Bullet.Type getType() {
        return definition.type;
    }

    public enum Type {

        Normal,
        Medium;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    private short ID;
    private Timer timer;
    private Ship ship;
    private Body body;
    private BulletLoader.Definition definition;
    private boolean isAlive = true;
    private BulletState bulletState;

    public Bullet(Level level, Type type, Ship ship, Vector2 position, float angleRad) {

        ID = BulletCount++;

        setShip(ship);
        setDefinition(level.getBulletLoader().get(type));

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.getWorld().createBody(definition.bodyDef);
        body.setTransform(position.x, position.y, angleRad);
        body.setLinearVelocity((new Vector2(0, definition.speed)).rotateRad(angleRad));
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);

        bulletState = new BulletState();
        bulletState.ID = ID;
        bulletState.angleDeg = getRotation();

        update();

        timer = new Timer();
        timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void resurrect(Ship ship, Vector2 position, float angleRad) {

        this.ship = ship;

        isAlive = true;

        body.setActive(true);
        body.setTransform(position.x, position.y, angleRad);
        body.setLinearVelocity((new Vector2(0, definition.speed)).rotateRad(angleRad));

        setRotation(angleRad * MathUtils.radiansToDegrees);

        bulletState.angleDeg = getRotation();

        timer.clear();
        timer.scheduleTask(new BulletDieTask(this), definition.life);

    }

    public void update() {

        Vector2 bodyPosition = body.getPosition();

        bulletState.bodyX = bodyPosition.x;
        bulletState.bodyY = bodyPosition.y;

        Vector2 bulletPosition = bodyPosition.sub(definition.bodyOrigin);

        setPosition(bulletPosition.x, bulletPosition.y);

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
