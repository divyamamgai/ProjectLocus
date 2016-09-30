package com.locus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.locus.game.screens.PlayScreen;

public class Main extends Game implements Disposable {

    // GRAVITY in metres/seconds^2
    public static final Vector2 GRAVITY = new Vector2(0, 0);
    // Gravitational Constant - (N(m^2))/(kg^2)
    public static final float GRAVITATIONAL_CONSTANT = 0.00004f;
    // Virtual Width of the Game World in meters.
    public static final float WORLD_WIDTH = 1280f;
    public static final float HALF_WORLD_WIDTH = WORLD_WIDTH / 2f;
    // Virtual Height of the Game World in meters.
    public static final float WORLD_HEIGHT = 1280f;
    public static final float HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2f;
    public static final float WORLD_DIAGONAL = (float) Math.sqrt(WORLD_WIDTH * WORLD_WIDTH + WORLD_HEIGHT * WORLD_HEIGHT);
    // Desired FPS of the game.
    public static final float FPS = 1 / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;
    public static final float PI_BY_TWO = MathUtils.PI / 2f;

    public SpriteBatch spriteBatch;

    @Override
    public void create() {
        spriteBatch = new SpriteBatch();
        setScreen(new PlayScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
    }

}
