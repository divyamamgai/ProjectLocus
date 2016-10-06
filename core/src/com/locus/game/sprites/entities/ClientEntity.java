package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.locus.game.levels.ClientLevel;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * Extended Sprite
 */
public abstract class ClientEntity extends Sprite {

    protected ClientLevel level;
    protected EntityLoader.Definition definition;
    protected float health, bodyX, bodyY;
    protected boolean isAlive = true;

    // Enable the constructors as per the needs.

    ClientEntity() {
        super();
    }

    public abstract void draw(SpriteBatch spriteBatch, Frustum frustum);

    public ClientLevel getLevel() {
        return level;
    }

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    public EntityLoader.Definition getDefinition() {
        return definition;
    }

    public void setDefinition(EntityLoader.Definition definition) {
        this.definition = definition;
    }

    public float getBodyY() {
        return bodyY;
    }

    public float getBodyX() {
        return bodyX;
    }

    public void setBodyPosition(float bodyX, float bodyY) {
        this.bodyX = bodyX;
        this.bodyY = bodyY;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

}
