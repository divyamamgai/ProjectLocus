package com.locus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.utils.Disposable;
import com.locus.game.screens.LoadingScreen;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;

import java.util.ArrayList;

public class Main extends Game implements Disposable {

    // GRAVITY in metres/seconds^2
    public static final Vector2 GRAVITY = new Vector2(0, 0);

    // Gravitational Constant - (N(m^2))/(kg^2)
    public static final float GRAVITATIONAL_CONSTANT = 0.00004f;

    // Virtual Width of the Game World in meters.
    private static final float WORLD_WIDTH = 2048f;
    public static final float HALF_WORLD_WIDTH = WORLD_WIDTH / 2f;
    // Virtual Height of the Game World in meters.
    private static final float WORLD_HEIGHT = 2048f;
    public static final float HALF_WORLD_HEIGHT = WORLD_HEIGHT / 2f;
    public static final float WORLD_DIAGONAL = (float) Math.sqrt(WORLD_WIDTH * WORLD_WIDTH + WORLD_HEIGHT * WORLD_HEIGHT);

    // Desired FPS of the main and Box2D configuration variables.
    public static final float FPS = 1 / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;

    public static final float TILED_MAP_SCALE = 0.125f;

    public static final float PI_BY_TWO = MathUtils.PI / 2f;

    public SpriteBatch spriteBatch;

    private static float aspectRatio = 1f;
    public static float cameraHeight = 100f;
    public static float cameraHalfHeight = cameraHeight / 2f;
    public static float cameraWidth = cameraHeight * aspectRatio;
    public static float cameraHalfWidth = cameraWidth / 2f;

    public static void resizeCamera(int width, int height) {
        aspectRatio = (float) width / (float) height;
        cameraWidth = cameraHeight * aspectRatio;
        cameraHalfWidth = cameraWidth / 2f;
    }

    // Common Assets, for now we will cache everything since it is all very small.
    public BitmapFont font1, font2, font3;
    public BitmapFont font1Selected, font2Selected, font3Selected;
    public GlyphLayout glyphLayout;
    public ArrayList<TiledMap> tiledMapList;
    public TextureAtlas uiTextureAtlas;
    public TextureAtlas bulletTextureAtlas;
    public TextureAtlas shipTextureAtlas;
    public TextureAtlas planetTextureAtlas;
    public TextureAtlas moonTextureAtlas;
    public BulletLoader bulletLoader;
    public EntityLoader entityLoader;
    public Color playerColor;
    public Ship.Type playerShipType;

    @Override
    public void create() {

        // To load BulletLoader and EntityLoader we first need to load Box2D.
        // This is because without Box2D initialization we cannot get access to the Shapes
        // and its other sub-classes.
        Box2D.init();

        spriteBatch = new SpriteBatch();

        tiledMapList = new ArrayList<TiledMap>();

        glyphLayout = new GlyphLayout();

        setScreen(new LoadingScreen(this));

    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        font1.dispose();
        font2.dispose();
        font3.dispose();
        uiTextureAtlas.dispose();
        bulletTextureAtlas.dispose();
        shipTextureAtlas.dispose();
        planetTextureAtlas.dispose();
        moonTextureAtlas.dispose();
        bulletLoader.dispose();
        entityLoader.dispose();
        spriteBatch.dispose();
    }

}
