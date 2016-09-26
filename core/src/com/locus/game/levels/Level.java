package com.locus.game.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.Main;
import com.locus.game.screens.PlayScreen;
import com.locus.game.sprites.entities.Planet;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class Level {

    private static final float BACKGROUND_SCALE = 7f;
    private static final Circle LEVEL_CIRCLE = new Circle(Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT, 400f);

    private PlayScreen playScreen;
    public Planet planet;
    private Texture backgroundTexture;

    private float backgroundPortionWidth, backgroundPortionHeight;

    public Level(PlayScreen playScreen, Planet.Type planetType, int backgroundType) {

        this.playScreen = playScreen;

        planet = new Planet(playScreen, planetType, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);

        backgroundTexture = new Texture("backgrounds/" + backgroundType + ".jpg");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat,
                Texture.TextureWrap.Repeat);

        backgroundPortionWidth = Main.WORLD_WIDTH * BACKGROUND_SCALE / backgroundTexture.getWidth();
        backgroundPortionHeight = Main.WORLD_HEIGHT * BACKGROUND_SCALE / backgroundTexture.getHeight();

    }

//    private void definePlanet() {
//
//        BodyDef planetBodyDef = new BodyDef();
//        planetBodyDef.position.set(Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
//        planetBodyDef.type = BodyDef.BodyType.KinematicBody;
//
//        planetBody = gameWorld.createBody(planetBodyDef);
//
//        planetBody.setUserData(planetSprite);
//
//        FixtureDef planetFixtureDef = new FixtureDef();
//        planetFixtureDef.density = 1f;
//        planetFixtureDef.friction = 1f;
//        planetFixtureDef.restitution = 0.1f;
//        planetFixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_PLANET;
//        planetFixtureDef.filter.maskBits = CollisionDetector.MASK_PLANET;
//
//        CircleShape planetCircle = new CircleShape();
//        planetCircle.setRadius(planetRadius);
//
//        planetFixtureDef.shape = planetCircle;
//
//        planetBody.createFixture(planetFixtureDef);
//
//        planetBody.setAngularVelocity(0.05f);
//
//        // Free up used memory, its not needed any more.
//        planetCircle.dispose();
//
//    }

    public void update() {

        planet.update();

    }

    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

        spriteBatch.draw(backgroundTexture,
                0, 0, Main.WORLD_WIDTH, Main.WORLD_HEIGHT,
                0, 0, backgroundPortionWidth, backgroundPortionHeight);

        if (planet.inFrustum(frustum)) {
            planet.draw(spriteBatch);
        }

    }

    public boolean isInLevel(Vector2 playerPosition) {
        return LEVEL_CIRCLE.contains(playerPosition);
    }

}
