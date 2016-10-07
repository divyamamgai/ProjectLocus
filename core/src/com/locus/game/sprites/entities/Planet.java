package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.Level;
import com.locus.game.network.PlanetState;

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

    private Circle gravityCircle;
    private PlanetState planetState;

    public Planet(Level level, Planet.Type type, float x, float y) {

        this.level = level;

        definition = level.getEntityLoader().get(Entity.Type.Planet, type.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.getWorld().createBody(definition.bodyDef);

        body.setTransform(x, y, 0);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        // We only to update these once since the planet is stationary.
        Vector2 planetPosition = body.getPosition().sub(definition.bodyOrigin);
        setPosition(planetPosition.x, planetPosition.y);

        gravityCircle = new Circle(x, y, definition.radius * 6f);

        planetState = new PlanetState();

    }

    public PlanetState getPlanetState() {
        return planetState;
    }

    public float getRadius() {
        return definition.radius;
    }

    public void applyGravitationalForce(Entity entity) {
        Vector2 entityWorldCenter = entity.body.getWorldCenter();
        if (gravityCircle.contains(entityWorldCenter)) {
            Vector2 gravitationalForce = body.getPosition().sub(entity.body.getPosition());
            float distance2 = gravitationalForce.len2();
            gravitationalForce
                    .nor()
                    .scl(definition.gravitationalMass * entity.definition.mass / distance2);
            entity.body.applyForce(gravitationalForce, entityWorldCenter, true);
        }
    }

    @Override
    public void update() {

        setRotation(body.getAngle() * MathUtils.radiansToDegrees);

        planetState.angleDeg = getRotation();

    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {
        Vector2 bodyPosition = body.getPosition();
        if (frustum.boundsInFrustum(bodyPosition.x, bodyPosition.y, 0,
                definition.halfWidth, definition.halfHeight, 0)) {
            super.draw(spriteBatch);
        }
    }

    @Override
    public void kill() {
    }

    @Override
    public void killBody() {

    }

}
