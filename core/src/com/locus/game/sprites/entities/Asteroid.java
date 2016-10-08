package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.MathUtils;
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

        Asteroid.Type type;
        float startAngle, speed;
        int life;

        Property() {
        }

        Property(Asteroid.Type type, float startAngle, float speed) {
            this.type = type;
            this.startAngle = startAngle;
            this.speed = speed;
        }

        public static Asteroid.Property generateRandom() {
            Asteroid.Property property = new Asteroid.Property();
            property.type = Asteroid.Type.values()[MathUtils.random(0, 4)];
            property.startAngle = MathUtils.random(0, 360);
            property.speed = MathUtils.random(30f, 60f);
            property.life = MathUtils.ceil(ProjectLocus.WORLD_DIAGONAL / property.speed);
            return property;
        }

    }

    Asteroid(Level level, Asteroid.Property property) {

        this.level = level;

        definition = level.getEntityLoader().get(Entity.Type.Moon, property.type.ordinal());

        setRegion(definition.textureRegion);
        setSize(definition.width, definition.height);

        body = level.getWorld().createBody(definition.bodyDef);

        body.setTransform(0, 0, 0);
        body.setUserData(this);
        definition.attachFixture(body);

        setOrigin(definition.bodyOrigin.x, definition.bodyOrigin.y);

    }

    @Override
    public void update() {

    }

    @Override
    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

    }

    @Override
    public void kill() {

    }

    @Override
    public void killBody() {

    }

}
