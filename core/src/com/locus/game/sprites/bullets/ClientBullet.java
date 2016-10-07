package com.locus.game.sprites.bullets;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.BulletState;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Bullet
 */

public class ClientBullet extends Sprite {

    private float bodyX, bodyY, toBodyX, toBodyY;

    public void setDefinition(BulletLoader.Definition definition) {
        this.definition = definition;
    }

    private BulletLoader.Definition definition;

    public ClientBullet(ClientLevel level, Bullet.Type type, BulletState bulletState) {

        setDefinition(level.getBulletLoader().get(type));

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        bodyX = toBodyX = bulletState.bodyX;
        bodyY = toBodyY = bulletState.bodyY;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(bulletState.angleDeg);

    }

    public void resurrect(BulletState bulletState) {
        update(bulletState);
        setRotation(bulletState.angleDeg);
    }

    public void update(BulletState bulletState) {
        toBodyX = bulletState.bodyX;
        toBodyY = bulletState.bodyY;
    }

    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

    public void interpolate(float delta) {
        bodyX += (toBodyX - bodyX) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        bodyY += (toBodyY - bodyY) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
    }

}
