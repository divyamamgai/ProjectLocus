package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Frustum;
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

    Level level;
    public Body body;
    public EntityLoader.Definition definition;
    public float health;
    public boolean isAlive = true;

    // Enable the constructors as per the needs.

    Entity() {
        super();
    }

    //    Entity(Texture textureRegion) {
//        super(textureRegion);
//    }
//    public Entity(Texture textureRegion, int srcWidth, int srcHeight) {
//        super(textureRegion, srcWidth, srcHeight);
//    }
//
//    public Entity(Texture textureRegion, int srcX, int srcY, int srcWidth, int srcHeight) {
//        super(textureRegion, srcX, srcY, srcWidth, srcHeight);
//    }
//

    public Entity(TextureRegion region) {
        super(region);
    }

//
//    public Entity(TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
//        super(region, srcX, srcY, srcWidth, srcHeight);
//    }
//
//    public Entity(Sprite sprite) {
//        super(sprite);
//    }

    public abstract void update();

    public abstract void draw(SpriteBatch spriteBatch, Frustum frustum);

    public abstract void kill();

    public abstract void destroy();

}
