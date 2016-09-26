package com.locus.game.sprites.entities;

import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.screens.PlayScreen;

/**
 * Created by Divya Mamgai on 9/23/2016.
 * Planet
 */

public class Planet extends Entity {

    public enum Type {

        Desert,
        Gas,
        Rock,
        Ice,
        Volcano;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    public Planet(PlayScreen playScreen, Planet.Type type, float x, float y) {

        this.playScreen = playScreen;

        definition = playScreen.entityLoader.get(Entity.Type.Planet, type.ordinal());

        setTexture(definition.texture);
        setRegion(0, 0, definition.texture.getWidth(), definition.texture.getHeight());
        setSize(definition.width, definition.height);

        body = playScreen.gameWorld.createBody(definition.bodyDef);

        body.setTransform(x, y, 0);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

    }

    public void applyGravitationalForce(Entity entity) {
        Vector2 gravitationalForce = body.getPosition().sub(entity.body.getPosition());
        float distance2 = gravitationalForce.len2();
        gravitationalForce
                .nor()
                .scl(definition.gravitationalMass * entity.definition.mass / distance2);
        entity.body.applyForce(gravitationalForce, entity.body.getWorldCenter(), true);
    }

    @Override
    public void update() {
        Vector2 planetPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(planetPosition.x, planetPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);
    }

    @Override
    public boolean inFrustum(Frustum frustum) {
        Vector2 planetPosition = body.getPosition();
        return frustum.boundsInFrustum(planetPosition.x, planetPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0);
    }

    @Override
    public void kill() {
        playScreen.destroyEntityStack.push(this);
    }

    @Override
    public void destroy() {
        playScreen.gameWorld.destroyBody(body);
        playScreen.entityList.remove(this);
    }

}
