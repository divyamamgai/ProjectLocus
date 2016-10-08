package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.network.MoonState;

/**
 * Created by Divya Mamgai on 9/27/2016.
 * Moon
 */

public class ClientMoon extends ClientEntity {

    public ClientMoon(ClientLevel level, Moon.Property moonProperty) {

        setLevel(level);
        setDefinition(level.getEntityLoader().get(Entity.Type.Moon, moonProperty.type.ordinal()));
        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);
        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);

        Vector2 startPosition = Moon.getStartPosition(moonProperty);
        toBodyX = bodyX = startPosition.x;
        toBodyY = bodyY = startPosition.y;
        toAngleDeg = angleDeg = 0;

        body = level.getWorld().createBody(definition.bodyDef);
        body.setTransform(bodyX, bodyY, 0);
        definition.attachFixture(body);

    }

    public void update(MoonState moonState) {
        toBodyX = moonState.bodyX;
        toBodyY = moonState.bodyY;
        toAngleDeg = moonState.angleDeg;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        if (frustum.boundsInFrustum(bodyX, bodyY, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

    public void interpolate(float delta) {
        bodyX += (toBodyX - bodyX) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        bodyY += (toBodyY - bodyY) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        angleDeg += (toAngleDeg - angleDeg) * ProjectLocus.INTERPOLATION_FACTOR * delta;
        setPosition(bodyX - definition.bodyOrigin.x, bodyY - definition.bodyOrigin.y);
        setRotation(angleDeg);
        body.setTransform(bodyX, bodyY, angleDeg * MathUtils.degreesToRadians);
    }
}
