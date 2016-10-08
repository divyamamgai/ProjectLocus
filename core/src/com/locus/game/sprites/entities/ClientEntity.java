package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.physics.box2d.Body;
import com.locus.game.levels.ClientLevel;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * Extended Sprite
 */
public abstract class ClientEntity extends Sprite {

    protected ClientLevel level;
    protected EntityLoader.Definition definition;
    protected Body body;
    short ID;
    float health, bodyX, bodyY, angleDeg, toBodyX, toBodyY, toAngleDeg;

    // Enable the constructors as per the needs.

    ClientEntity() {
        super();
    }

    public short getID() {
        return ID;
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

}
