package com.locus.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.ShipState;
import com.locus.game.sprites.bullets.Bullet;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class ClientShip extends ClientEntity {

    private Vector2 bulletPosition;
    private short primaryBulletCount, secondaryBulletCount;
    private Bullet.Type primaryBulletType;
    private Bullet.Type secondaryBulletType;

    public ClientShip(ClientLevel level, Ship.Property property, ShipState shipState) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Ship, property.type.ordinal()));
        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setColor(property.color);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        ID = shipState.ID;
        bodyX = toBodyX = shipState.bodyX;
        bodyY = toBodyY = shipState.bodyY;
        angleDeg = toAngleDeg = shipState.angleDeg;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);

        body = level.getWorld().createBody(definition.bodyDef);
        body.setTransform(bodyX, bodyY, MathUtils.degreesToRadians * angleDeg);
        definition.attachFixture(body);

        primaryBulletType = Bullet.Type.Normal;
        switch (property.type) {
            case Fighter:
                secondaryBulletType = Bullet.Type.Fighter;
                break;
            case SuperSonic:
                secondaryBulletType = Bullet.Type.SuperSonic;
                break;
            case Bomber:
                secondaryBulletType = Bullet.Type.Bomber;
                break;
        }
        bulletPosition = new Vector2();
        primaryBulletCount = secondaryBulletCount = 0;

    }

    public void resurrect(Color color, ShipState shipState) {

        setColor(color);

        bodyX = toBodyX = shipState.bodyX;
        bodyY = toBodyY = shipState.bodyY;
        angleDeg = toAngleDeg = shipState.angleDeg;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);
        body.setTransform(bodyX, bodyY, angleDeg * MathUtils.degreesToRadians);

    }

    public void update(ShipState shipState) {
        toBodyX = shipState.bodyX;
        toBodyY = shipState.bodyY;
        toAngleDeg = shipState.angleDeg;
        health = shipState.health;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

        if ((health > 0) && frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {

            super.draw(spriteBatch);

            spriteBatch.draw(level.getBarBackgroundTexture(),
                    bodyX - definition.halfWidth - 0.2f, bodyY + 2.8f,
                    definition.width + 0.4f, 0.9f);
            spriteBatch.draw(level.getBarForegroundTexture(),
                    bodyX - definition.halfWidth, bodyY + 3f,
                    definition.width * (health / definition.maxHealth), 0.5f);

        }

    }

    public void firePrimaryBullet() {
        if (primaryBulletCount == 0) {
            float angleRad = body.getAngle(), delta = Gdx.graphics.getDeltaTime() * 1.25f;
            for (Vector2 weaponPosition : definition.weaponPositionMap.get(primaryBulletType)) {
                level.addBulletAlive(primaryBulletType, this,
                        bulletPosition.set(weaponPosition).rotateRad(angleRad)
                                .add(bodyX + (toBodyX - bodyX) *
                                                ProjectLocus.INTERPOLATION_FACTOR * delta,
                                        bodyY + (toBodyY - bodyY) *
                                                ProjectLocus.INTERPOLATION_FACTOR * delta),
                        angleRad);
            }
        } else if (primaryBulletCount >= 14) {
            primaryBulletCount = -1;
        }
        primaryBulletCount++;
    }

    public void fireSecondaryBullet() {
        if (secondaryBulletCount == 0) {
            float angleRad = body.getAngle(), delta = Gdx.graphics.getDeltaTime() * 1.25f;
            for (Vector2 weaponPosition : definition.weaponPositionMap.get(secondaryBulletType)) {
                level.addBulletAlive(secondaryBulletType, this,
                        bulletPosition.set(weaponPosition).rotateRad(angleRad)
                                .add(bodyX + (toBodyX - bodyX) *
                                                ProjectLocus.INTERPOLATION_FACTOR * delta,
                                        bodyY + (toBodyY - bodyY) *
                                                ProjectLocus.INTERPOLATION_FACTOR * delta),
                        angleRad);
            }
        } else if (secondaryBulletCount >= 14) {
            secondaryBulletCount = -1;
        }
        secondaryBulletCount++;
    }

    public void interpolate(float delta) {
        bodyX += (toBodyX - bodyX) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        bodyY += (toBodyY - bodyY) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        angleDeg += (toAngleDeg - angleDeg) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);
        body.setTransform(bodyX, bodyY, angleDeg * MathUtils.degreesToRadians);
    }

    public void fire(boolean isPrimaryBulletEnabled, boolean isSecondaryBulletEnabled) {
        if (isPrimaryBulletEnabled) {
            firePrimaryBullet();
        }
        if (isSecondaryBulletEnabled) {
            fireSecondaryBullet();
        }
    }
}
