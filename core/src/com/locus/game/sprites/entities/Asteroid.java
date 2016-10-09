package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;

/**
 * Created by Divya Mamgai on 10/8/2016.
 * Asteroid
 */

public class Asteroid extends Entity {

    public enum Type {

        Rock,
        Dust,
        Loose,
        Organic,
        Mineral;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    public static class Property {

        float startAngle, speed;

        Property() {
        }

        static Asteroid.Property generateRandom() {
            Asteroid.Property property = new Asteroid.Property();
            property.startAngle = MathUtils.random(0, 360);
            property.speed = MathUtils.random(30f, 60f);
            return property;
        }

    }

    private Asteroid.Type asteroidType;

    public Type getAsteroidType() {
        return asteroidType;
    }

    public Asteroid(Level level, Asteroid.Type asteroidType, Asteroid.Property property) {

        this.level = level;
        this.asteroidType = asteroidType;

        definition = level.getEntityLoader().get(Entity.Type.Asteroid, asteroidType.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.getWorld().createBody(definition.bodyDef);

        body.setTransform(Level.LEVEL_CIRCLE.radius * MathUtils.cosDeg(property.startAngle) +
                        ProjectLocus.WORLD_HALF_WIDTH,
                Level.LEVEL_CIRCLE.radius * MathUtils.sinDeg(property.startAngle) +
                        ProjectLocus.WORLD_HALF_HEIGHT, 0);
        body.setLinearVelocity(
                (new Vector2(1, 0)).rotate(property.startAngle).scl(-property.speed));
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);
        update();

        setHealth(definition.maxHealth);

    }

    public void resurrect(Asteroid.Property property) {

        isAlive = true;
        setHealth(definition.maxHealth);

        body.setActive(true);
        body.setTransform(Level.LEVEL_CIRCLE.radius * MathUtils.cosDeg(property.startAngle) +
                        ProjectLocus.WORLD_HALF_WIDTH,
                Level.LEVEL_CIRCLE.radius * MathUtils.sinDeg(property.startAngle) +
                        ProjectLocus.WORLD_HALF_HEIGHT, 0);
        body.setLinearVelocity(
                (new Vector2(1, 0)).rotate(property.startAngle).scl(-property.speed));

    }

    @Override
    public void update() {

        Vector2 asteroidPosition = body.getPosition().sub(definition.bodyOrigin);

        setPosition(asteroidPosition.x, asteroidPosition.y);
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
        if (isAlive) {
            isAlive = false;
        }
    }

    @Override
    public void killBody() {
        body.setActive(false);
    }

}
