package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.ShipState;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class ClientShip extends ClientEntity {

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

    }

    public void resurrect(Color color, ShipState shipState) {

        setColor(color);

        bodyX = toBodyX = shipState.bodyX;
        bodyY = toBodyY = shipState.bodyY;
        angleDeg = toAngleDeg = shipState.angleDeg;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);

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

    public void interpolate(float delta) {
        bodyX += (toBodyX - bodyX) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        bodyY += (toBodyY - bodyY) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        angleDeg += (toAngleDeg - angleDeg) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);
    }
}
