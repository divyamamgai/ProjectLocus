package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.PlanetState;

/**
 * Created by Divya Mamgai on 9/27/2016.
 * Moon
 */

public class ClientPlanet extends ClientEntity {

    ClientPlanet() {

    }

    public ClientPlanet(ClientLevel level, Planet.Type type) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Planet, type.ordinal()));

        bodyX = ProjectLocus.WORLD_HALF_WIDTH;
        bodyY = ProjectLocus.WORLD_HALF_HEIGHT;

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);

    }

    public void update(PlanetState planetState) {
        // No need to change positions.
        setRotation(planetState.angleDeg);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

}
