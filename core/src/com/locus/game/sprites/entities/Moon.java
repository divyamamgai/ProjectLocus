package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.levels.Level;

/**
 * Created by Divya Mamgai on 9/27/2016.
 * Moon
 */

public class Moon extends Entity {

    public enum Type {

        Rust,
        Iron,
        WhiteIce,
        DarkIce,
        Organic;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    public static class Property {

        public Moon.Type type;
        float orbitRadius, orbitAngle;

        public Property() {
            
        }

        public Property(Moon.Type type, float orbitRadius, float orbitAngle) {
            this.type = type;
            this.orbitRadius = orbitRadius;
            this.orbitAngle = orbitAngle;
        }

    }

    private Circle gravityCircle;
    private Vector2 planetPosition, orbitalVelocity;

    public Moon(Level level, float planetX, float planetY, Moon.Property moonProperty) {

        this.level = level;

        definition = level.projectLocus.entityLoader.get(Entity.Type.Moon, moonProperty.type.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.world.createBody(definition.bodyDef);

        Vector2 moonPosition = (new Vector2(moonProperty.orbitRadius, 0))
                .rotate(moonProperty.orbitAngle)
                .add(planetX, planetY);

        body.setTransform(moonPosition.x, moonPosition.y, 0);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        gravityCircle = new Circle(moonPosition.x, moonPosition.y, definition.radius * 6f);
        planetPosition = new Vector2(planetX, planetY);
        orbitalVelocity = new Vector2(1, 1);

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

        float angleRad = body.getPosition().sub(planetPosition).angleRad();
        Vector2 bodyPosition = body.getPosition();

        orbitalVelocity
                .set(0, 1f)
                .rotateRad(angleRad)
                .scl(definition.orbitalVelocity);
        body.setLinearVelocity(orbitalVelocity);

        gravityCircle.setPosition(bodyPosition);
        Vector2 planetPosition = bodyPosition.sub(definition.bodyOrigin);
        setPosition(planetPosition.x, planetPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);

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
