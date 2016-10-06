package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.ShipState;

/**
 * Created by Divya Mamgai on 9/11/2016.
 * Ship
 */
public class ClientShip extends ClientEntity {

    ClientShip() {

    }

    ClientShip(ClientLevel level, Ship.Property property, ShipState shipState) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Ship, property.type.ordinal()));

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setColor(property.color);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        update(shipState);

    }

    public void update(ShipState shipState) {
        setPosition(shipState.x, shipState.y);
        bodyX = shipState.bodyX;
        bodyY = shipState.bodyY;
        setRotation(shipState.angleDeg);
        health = shipState.health;
        isAlive = shipState.isAlive;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
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

}
