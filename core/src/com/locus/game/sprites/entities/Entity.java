package com.locus.game.sprites.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.physics.box2d.Body;
import com.locus.game.screens.PlayScreen;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * Extended Sprite
 */
public abstract class Entity extends Sprite {

    public enum Type {

        Ship,
        Planet,
        Moon,
        Asteroid;

        public String toString() {
            return String.valueOf(ordinal());
        }

    }

    PlayScreen playScreen;
    public Body body;
    public EntityLoader.Definition definition;
    public float health;
    boolean isAlive = true;

    // Enable the constructors as per the needs.

    Entity() {
        super();
    }

//    Entity(Texture texture) {
//        super(texture);
//    }
//
//    public Entity(Texture texture, int srcWidth, int srcHeight) {
//        super(texture, srcWidth, srcHeight);
//    }
//
//    public Entity(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
//        super(texture, srcX, srcY, srcWidth, srcHeight);
//    }
//
//    public Entity(TextureRegion region) {
//        super(region);
//    }
//
//    public Entity(TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
//        super(region, srcX, srcY, srcWidth, srcHeight);
//    }
//
//    public Entity(Sprite sprite) {
//        super(sprite);
//    }

    public abstract void drawHealth();

    public abstract void update();

    public abstract boolean inFrustum(Frustum frustum);

    public abstract void kill();

    public abstract void destroy();

}
