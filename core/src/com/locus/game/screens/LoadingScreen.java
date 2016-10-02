package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.locus.game.Main;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.EntityLoader;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Splash Screen
 */

public class LoadingScreen implements Screen {

    private static final float CAMERA_MOVEMENT_SPEED = 0.05f;
    private static final float CAMERA_MOVEMENT_RADIUS = 512f;

    private float backgroundMovementAngleRad = 0;
    private Main main;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private Sprite logo;
    private TiledMapRenderer tiledMapRenderer;
    private AssetManager assetManager;

    public LoadingScreen(Main main) {

        this.main = main;

        foregroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        foregroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        backgroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        backgroundCamera.update();

        assetManager = new AssetManager();

        FileHandleResolver fileHandleResolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class,
                new FreeTypeFontGeneratorLoader(fileHandleResolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandleResolver));
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(fileHandleResolver));

        assetManager.load("backgrounds/1.tmx", TiledMap.class);

        assetManager.load("ui/pack.atlas", TextureAtlas.class);

        // First complete loading the currently required assets.
        assetManager.finishLoading();

        // Use the initially loaded elements.
        main.uiTextureAtlas = assetManager.get("ui/pack.atlas", TextureAtlas.class);

        logo = main.uiTextureAtlas.createSprite("logo");
        logo.setSize(71.31f, 25f);

        main.tiledMapList.add(assetManager.get("backgrounds/1.tmx", TiledMap.class));
        TiledMap tiledMap = main.tiledMapList.get(0);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, Main.TILED_MAP_SCALE);

        // Now load heavy objects here.

        FreeTypeFontLoaderParameter font1Parameter = new FreeTypeFontLoaderParameter();
        font1Parameter.fontFileName = "ui/font.ttf";
        font1Parameter.fontParameters.size = 24;
        font1Parameter.fontParameters.spaceX = 3;
//        font1Parameter.fontParameters.magFilter = Texture.TextureFilter.Linear;
//        font1Parameter.fontParameters.minFilter = Texture.TextureFilter.Linear;
        assetManager.load("font1.ttf", BitmapFont.class, font1Parameter);
        assetManager.load("font1Selected.ttf", BitmapFont.class, font1Parameter);

        FreeTypeFontLoaderParameter font2Parameter = new FreeTypeFontLoaderParameter();
        font2Parameter.fontFileName = "ui/font.ttf";
        font2Parameter.fontParameters.size = 32;
        font2Parameter.fontParameters.spaceX = 4;
//        font2Parameter.fontParameters.magFilter = Texture.TextureFilter.Linear;
//        font2Parameter.fontParameters.minFilter = Texture.TextureFilter.Linear;
        assetManager.load("font2.ttf", BitmapFont.class, font2Parameter);
        assetManager.load("font2Selected.ttf", BitmapFont.class, font2Parameter);

        FreeTypeFontLoaderParameter font3Parameter = new FreeTypeFontLoaderParameter();
        font3Parameter.fontFileName = "ui/font.ttf";
        font3Parameter.fontParameters.size = 48;
        font3Parameter.fontParameters.spaceX = 6;
//        font3Parameter.fontParameters.magFilter = Texture.TextureFilter.Linear;
//        font3Parameter.fontParameters.minFilter = Texture.TextureFilter.Linear;
        assetManager.load("font3.ttf", BitmapFont.class, font3Parameter);
        assetManager.load("font3Selected.ttf", BitmapFont.class, font3Parameter);

        assetManager.load("sprites/bullets/pack.atlas", TextureAtlas.class);

        assetManager.load("sprites/entities/0/pack.atlas", TextureAtlas.class);

        assetManager.load("sprites/entities/1/pack.atlas", TextureAtlas.class);

        assetManager.load("sprites/entities/2/pack.atlas", TextureAtlas.class);

        for (Integer backgroundType = 2; backgroundType <= 8; backgroundType++) {
            assetManager.load("backgrounds/" + String.valueOf(backgroundType) + ".tmx",
                    TiledMap.class);
        }

    }

    private void positionUI() {

        logo.setPosition(Main.cameraHalfWidth - logo.getWidth() / 2,
                Main.cameraHalfHeight - logo.getHeight() / 2);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundMovementAngleRad += delta * CAMERA_MOVEMENT_SPEED;
        backgroundCamera.position.set(
                Main.HALF_WORLD_WIDTH +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.cos(backgroundMovementAngleRad),
                Main.HALF_WORLD_HEIGHT +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        main.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        main.spriteBatch.begin();

        logo.draw(main.spriteBatch);

        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barBackground"),
                Main.cameraHalfWidth - 16.5f, Main.cameraHalfHeight - 24.5f,
                33f, 3f);

        if (assetManager.update()) {

            main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barForeground"),
                    Main.cameraHalfWidth - 16f, Main.cameraHalfHeight - 24f,
                    32f, 2f);

            // Store the loaded assets.

            main.font1 = assetManager.get("font1.ttf", BitmapFont.class);
            main.font1.getData().setScale(0.2f);
            main.font1Selected = assetManager.get("font1Selected.ttf", BitmapFont.class);
            main.font1Selected.getData().setScale(0.2f);
            main.font1Selected.setColor(217f / 255f, 100f / 255f, 89f / 255f, 1f);
            main.font2 = assetManager.get("font2.ttf", BitmapFont.class);
            main.font2.getData().setScale(0.2f);
            main.font2Selected = assetManager.get("font2Selected.ttf", BitmapFont.class);
            main.font2Selected.getData().setScale(0.2f);
            main.font2Selected.setColor(217f / 255f, 100f / 255f, 89f / 255f, 1f);
            main.font3 = assetManager.get("font3.ttf", BitmapFont.class);
            main.font3.getData().setScale(0.2f);
            main.font3Selected = assetManager.get("font3Selected.ttf", BitmapFont.class);
            main.font3Selected.getData().setScale(0.2f);
            main.font3Selected.setColor(217f / 255f, 100f / 255f, 89f / 255f, 1f);

            for (Integer backgroundType = 2; backgroundType <= 8; backgroundType++) {
                main.tiledMapList.add(assetManager.get("backgrounds/" +
                        String.valueOf(backgroundType) + ".tmx", TiledMap.class));
            }

            main.bulletTextureAtlas = assetManager.get("sprites/bullets/pack.atlas",
                    TextureAtlas.class);

            main.bulletLoader = new BulletLoader(main.bulletTextureAtlas);

            main.shipTextureAtlas = assetManager.get("sprites/entities/0/pack.atlas",
                    TextureAtlas.class);

            main.planetTextureAtlas = assetManager.get("sprites/entities/1/pack.atlas",
                    TextureAtlas.class);

            main.moonTextureAtlas = assetManager.get("sprites/entities/2/pack.atlas",
                    TextureAtlas.class);

            main.entityLoader = new EntityLoader(main);

            main.setScreen(new PlayerSelectScreen(main, backgroundMovementAngleRad));

        } else {

            main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barForeground"),
                    Main.cameraHalfWidth - 16f, Main.cameraHalfHeight - 24f,
                    32f * assetManager.getProgress(), 2f);

        }

        main.spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        Main.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, Main.cameraWidth, Main.cameraHeight);
        backgroundCamera.setToOrtho(false, Main.cameraWidth, Main.cameraHeight);
        positionUI();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
