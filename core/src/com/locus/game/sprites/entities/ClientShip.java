package com.locus.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
    private short primaryBulletCount, primaryBulletFireRate, secondaryBulletCount,
            secondaryBulletFireRate;
    private Bullet.Type primaryBulletType, secondaryBulletType;
    private Sound primaryBulletSound, secondaryBulletSound;

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
        primaryBulletFireRate = level.getBulletLoader().get(primaryBulletType).fireRate;
        primaryBulletSound = level.getProjectLocus().primaryBulletSound;
        switch (property.type) {
            case Fighter:
                secondaryBulletType = Bullet.Type.Fighter;
                secondaryBulletSound = level.getProjectLocus().secondaryBulletFighterSound;
                break;
            case SuperSonic:
                secondaryBulletType = Bullet.Type.SuperSonic;
                secondaryBulletSound = level.getProjectLocus().secondaryBulletSuperSonicSound;
                break;
            case Bomber:
                secondaryBulletType = Bullet.Type.Bomber;
                secondaryBulletSound = level.getProjectLocus().secondaryBulletBomberSound;
                break;
        }
        secondaryBulletFireRate = level.getBulletLoader().get(secondaryBulletType).fireRate;
        bulletPosition = new Vector2();
        primaryBulletCount = secondaryBulletCount = 0;

        isAlive = true;

    }

//    public void resurrect(Color color, ShipState shipState) {
//
//        setColor(color);
//
//        body.setActive(true);
//
//        bodyX = toBodyX = shipState.bodyX;
//        bodyY = toBodyY = shipState.bodyY;
//        angleDeg = toAngleDeg = shipState.angleDeg;
//
//        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
//        setRotation(angleDeg);
//        body.setTransform(bodyX, bodyY, angleDeg * MathUtils.degreesToRadians);
//
//        isAlive = true;
//
//    }

    public void update(ShipState shipState) {
        toBodyX = shipState.bodyX;
        toBodyY = shipState.bodyY;
        toAngleDeg = shipState.angleDeg;
        health = shipState.health;
        if (health <= 0) {
            killBody();
            isAlive = false;
        }
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

    private void firePrimaryBullet() {
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
            primaryBulletSound.play();
        } else if (primaryBulletCount >= primaryBulletFireRate) {
            primaryBulletCount = -1;
        }
        primaryBulletCount++;
    }

    private void fireSecondaryBullet() {
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
            secondaryBulletSound.play();
        } else if (secondaryBulletCount >= secondaryBulletFireRate) {
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

    public void fire(boolean isPrimaryBulletEnabled, boolean isSecondaryBulletEnabled,
                     boolean doPrimaryReset, boolean doSecondaryReset) {
        if (doPrimaryReset) {
            primaryBulletCount = 0;
        }
        if (doSecondaryReset) {
            secondaryBulletCount = 0;
        }
        if (isPrimaryBulletEnabled) {
            firePrimaryBullet();
        }
        if (isSecondaryBulletEnabled) {
            fireSecondaryBullet();
        }
    }

    public void kill() {
        isAlive = false;
        killBody();
    }

    public void killBody() {
        body.setActive(false);
    }

    public Vector2 getBodyPosition() {
        return body.getPosition();
    }
}
