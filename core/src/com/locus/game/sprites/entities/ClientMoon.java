package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.MoonState;

/**
 * Created by Divya Mamgai on 9/27/2016.
 * Moon
 */

public class ClientMoon extends ClientEntity {

    ClientMoon() {

    }

    public ClientMoon(ClientLevel level, Moon.Property moonProperty) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Moon, moonProperty.type.ordinal()));

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        Vector2 startPosition = Moon.getStartPosition(moonProperty);
        setPosition(startPosition.x, startPosition.y);
        bodyX = startPosition.x + definition.bodyOrigin.x;
        bodyY = startPosition.y + definition.bodyOrigin.y;

    }

    public void update(MoonState moonState) {
        setPosition(moonState.x, moonState.y);
        bodyX = moonState.bodyX;
        bodyY = moonState.bodyY;
        setRotation(moonState.angleDeg);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

}
