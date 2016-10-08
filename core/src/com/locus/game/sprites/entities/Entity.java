package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.locus.game.levels.Level;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * Extended Sprite
 */
public abstract class Entity extends Sprite {

    public static short EntityCount = 0;

    public enum Type {

        Ship,
        Planet,
        Moon,
        Asteroid;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    short ID;
    protected Level level;
    protected EntityLoader.Definition definition;
    Body body;
    float health, percentageHealth;
    boolean isAlive = true;

    public short getID() {
        return ID;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Body getBody() {
        return body;
    }

    void setBody(Body body) {
        this.body = body;
    }

    public Vector2 getBodyPosition() {
        return body.getPosition();
    }

    public Type getType() {
        return definition.type;
    }

    public EntityLoader.Definition getDefinition() {
        return definition;
    }

    public void setDefinition(EntityLoader.Definition definition) {
        this.definition = definition;
    }

    public float getHealth() {
        return health;
    }

    void setHealth(float health) {
        this.health = health;
        percentageHealth = health / definition.maxHealth;
    }

    public void reduceHealth(float by) {
        health -= by;
        if (health <= 0) {
            kill();
            percentageHealth = 0;
        } else {
            percentageHealth = health / definition.maxHealth;
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    Entity() {
        super();
        ID = EntityCount++;
    }

    public abstract void update();

    public abstract void draw(SpriteBatch spriteBatch, Frustum frustum);

    public abstract void kill();

    public abstract void killBody();

}
