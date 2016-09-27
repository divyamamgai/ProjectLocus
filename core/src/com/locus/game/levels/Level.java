package com.locus.game.levels;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.Main;
import com.locus.game.screens.PlayScreen;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class Level {

    private static final float BACKGROUND_SCALE = 7f;
    private static final Circle LEVEL_CIRCLE = new Circle(Main.HALF_WORLD_WIDTH,
            Main.HALF_WORLD_HEIGHT, 400f);

    private PlayScreen playScreen;
    public Planet planet;
    public Moon moon;
    private Texture backgroundTexture;

    private float backgroundPortionWidth, backgroundPortionHeight;

    public Level(PlayScreen playScreen, Planet.Type planetType, Moon.Type moonType, int backgroundType) {

        this.playScreen = playScreen;

        planet = new Planet(playScreen, planetType, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);

        moon = new Moon(playScreen, moonType, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT,
                planet.getRadius() * 4f, 0);

        backgroundTexture = new Texture("backgrounds/" + backgroundType + ".jpg");
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat,
                Texture.TextureWrap.Repeat);

        backgroundPortionWidth = Main.WORLD_WIDTH * BACKGROUND_SCALE / backgroundTexture.getWidth();
        backgroundPortionHeight = Main.WORLD_HEIGHT * BACKGROUND_SCALE / backgroundTexture.getHeight();

    }

    public void update() {
        planet.update();
        moon.update();
    }

    public void draw(SpriteBatch spriteBatch, Frustum frustum) {

        spriteBatch.draw(backgroundTexture,
                0, 0, Main.WORLD_WIDTH, Main.WORLD_HEIGHT,
                0, 0, backgroundPortionWidth, backgroundPortionHeight);

        if (planet.inFrustum(frustum)) {
            planet.draw(spriteBatch);
        }

        if (moon.inFrustum(frustum)) {
            moon.draw(spriteBatch);
        }

    }

    public boolean isInLevel(Vector2 playerPosition) {
        return LEVEL_CIRCLE.contains(playerPosition);
    }

}
