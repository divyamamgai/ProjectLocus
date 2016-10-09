package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.network.MoonState;

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
    private Vector2 orbitalVelocity;
    private MoonState moonState;

    public Moon(Level level, Moon.Property moonProperty) {

        this.level = level;

        definition = level.getEntityLoader().get(Entity.Type.Moon, moonProperty.type.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.getWorld().createBody(definition.bodyDef);

        Vector2 moonPosition = (new Vector2(moonProperty.orbitRadius, 0))
                .rotate(moonProperty.orbitAngle)
                .add(ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT);

        body.setTransform(moonPosition.x, moonPosition.y, 0);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

        gravityCircle = new Circle(moonPosition.x, moonPosition.y, definition.radius * 6f);
        orbitalVelocity = new Vector2(1, 1);

        moonState = new MoonState();

    }

    static Vector2 getStartPosition(Moon.Property property) {
        return (new Vector2(property.orbitRadius, 0))
                .rotate(property.orbitAngle)
                .add(ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT);
    }

    public MoonState getMoonState() {
        return moonState;
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

        float orbitalAngleRad = body.getPosition()
                .sub(ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT).angleRad();
        Vector2 bodyPosition = body.getPosition();

        moonState.bodyX = bodyPosition.x;
        moonState.bodyY = bodyPosition.y;

        orbitalVelocity
                .set(0, 1f)
                .rotateRad(orbitalAngleRad)
                .scl(definition.orbitalVelocity);
        body.setLinearVelocity(orbitalVelocity);

        gravityCircle.setPosition(bodyPosition);

        Vector2 moonPosition = bodyPosition.sub(definition.bodyOrigin);
        setPosition(moonPosition.x, moonPosition.y);
        setRotation(body.getAngle() * MathUtils.radiansToDegrees);

        moonState.angleDeg = getRotation();

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
