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

    private Bullet.Type type;

    public Bullet.Type getType() {
        return type;
    }

    public ClientBullet(ClientLevel level, BulletState bulletState) {

        type = bulletState.type;

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

        bodyX = toBodyX = bulletState.bodyX;
        bodyY = toBodyY = bulletState.bodyY;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
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

    public boolean interpolate(float delta, boolean isDead) {
        float dx = (toBodyX - bodyX), dy = (toBodyY - bodyY);
        bodyX += dx * ProjectLocus.INTERPOLATION_FACTOR * 3f * delta;
        bodyY += dy * ProjectLocus.INTERPOLATION_FACTOR * 3f * delta;
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        return isDead && (Math.abs(dx) <= 0.3f || Math.abs(dy) <= 0.3f);
    }

}
