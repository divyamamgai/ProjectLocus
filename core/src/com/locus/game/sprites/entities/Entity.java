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

    public enum Type {

        Ship,
        Planet,
        Moon;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    protected Level level;
    protected EntityLoader.Definition definition;
    Body body;
    float health;
    boolean isAlive = true;

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
    }

    public void reduceHealth(float by) {
        health -= by;
        if (health <= 0) {
            kill();
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    Entity() {
        super();
    }

    public abstract void update();

    public abstract void draw(SpriteBatch spriteBatch, Frustum frustum);

    public abstract void kill();

    public abstract void killBody();

}
