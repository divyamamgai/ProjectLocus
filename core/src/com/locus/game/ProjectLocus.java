package com.locus.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
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

public class ProjectLocus extends Game implements Disposable {

    // GRAVITY in metres/seconds^2
    public static final Vector2 GRAVITY = new Vector2(0, 0);

    // Gravitational Constant - (N(m^2))/(kg^2)
    public static final float GRAVITATIONAL_CONSTANT = 0.00004f;

    // Virtual Width of the Game World in meters.
    private static final float WORLD_WIDTH = 2048f;
    public static final float WORLD_HALF_WIDTH = WORLD_WIDTH / 2f;
    // Virtual Height of the Game World in meters.
    private static final float WORLD_HEIGHT = 2048f;
    public static final float WORLD_HALF_HEIGHT = WORLD_HEIGHT / 2f;
    public static final float WORLD_DIAGONAL = (float) Math.sqrt(WORLD_WIDTH * WORLD_WIDTH + WORLD_HEIGHT * WORLD_HEIGHT);

    // Desired FPS of the projectLocus and Box2D configuration variables.
    public static final float FPS = 1 / 60f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;

    public static final float TILED_MAP_SCALE = 0.125f;

    public static final float PI_BY_TWO = MathUtils.PI / 2f;

    public SpriteBatch spriteBatch;

    private static float aspectRatio = 1f;
    public static int screenCameraWidth = 854;
    public static int screenCameraHalfWidth = screenCameraWidth / 2;
    public static int screenCameraHeight = 480;
    public static int screenCameraHalfHeight = screenCameraHeight / 2;
    public static float worldCameraHeight = 100f;
    public static float worldCameraHalfHeight = worldCameraHeight / 2f;
    public static float worldCameraWidth = worldCameraHeight * aspectRatio;
    public static float worldCameraHalfWidth = worldCameraWidth / 2f;

    // Common Assets, for now we will cache everything since it is all very small.
    public AssetManager assetManager;
    public BitmapFont font24, font32;
    public BitmapFont font24Selected, font32Selected;
    public static Color fontSelectedColor = new Color(217f / 255f, 100f / 255f, 89f / 255f, 1f);
    public GlyphLayout glyphLayout;
    public ArrayList<TiledMap> tiledMapList;
    public TextureAtlas uiTextureAtlas;
    public TextureAtlas bulletTextureAtlas;
    public TextureAtlas shipTextureAtlas;
    public TextureAtlas planetTextureAtlas;
    public TextureAtlas moonTextureAtlas;
    public BulletLoader bulletLoader;
    public EntityLoader entityLoader;
    public Ship.Property playerShipProperty;

    @Override
    public void create() {

        // To load BulletLoader and EntityLoader we first need to load Box2D.
        // This is because without Box2D initialization we cannot get access to the Shapes
        // and its other sub-classes.
        Box2D.init();

        spriteBatch = new SpriteBatch();
        assetManager = new AssetManager();
        tiledMapList = new ArrayList<TiledMap>();
        glyphLayout = new GlyphLayout();
        playerShipProperty = new Ship.Property();

        setScreen(new LoadingScreen(this));

    }

    @Override
    public void render() {
        super.render();
    }

    public static void resizeCamera(int width, int height) {
        aspectRatio = (float) width / (float) height;
        screenCameraWidth = (int) (screenCameraHeight * aspectRatio);
        screenCameraHalfWidth = screenCameraWidth / 2;
        worldCameraWidth = worldCameraHeight * aspectRatio;
        worldCameraHalfWidth = worldCameraWidth / 2f;
    }

    @Override
    public void dispose() {
        assetManager.clear();
        assetManager.dispose();
        entityLoader.dispose();
        spriteBatch.dispose();
    }

}
