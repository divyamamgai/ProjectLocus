package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.PlanetState;

/**
 * Created by Divya Mamgai on 9/27/2016.
 * Moon
 */

public class ClientPlanet extends ClientEntity {

    public ClientPlanet(ClientLevel level, Planet.Type type) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Planet, type.ordinal()));
        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);


        bodyX = ProjectLocus.WORLD_HALF_WIDTH;
        bodyY = ProjectLocus.WORLD_HALF_HEIGHT;
        angleDeg = 0;
        toAngleDeg = 0;

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);

        body = level.getWorld().createBody(definition.bodyDef);
        body.setTransform(bodyX, bodyY, 0);
        definition.attachFixture(body);

    }

    public void update(PlanetState planetState) {
        toAngleDeg = planetState.angleDeg;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

    public void interpolate(float delta) {
        angleDeg += (toAngleDeg - angleDeg) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        setRotation(angleDeg);
        body.setTransform(bodyX, bodyY, angleDeg * MathUtils.degreesToRadians);
    }
}
