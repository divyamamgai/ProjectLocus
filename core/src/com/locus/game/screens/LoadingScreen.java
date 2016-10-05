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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.EntityLoader;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Splash Screen
 */

public class LoadingScreen implements Screen {

    private float backgroundMovementAngleRad = 0;
    private ProjectLocus projectLocus;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private Sprite logo;
    private TiledMapRenderer tiledMapRenderer;
    private AssetManager assetManager;
    private TextureRegion barBackgroundTexture, barForegroundTexture;

    public LoadingScreen(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        assetManager = projectLocus.assetManager;

        FileHandleResolver fileHandleResolver = new InternalFileHandleResolver();
        assetManager.setLoader(FreeTypeFontGenerator.class,
                new FreeTypeFontGeneratorLoader(fileHandleResolver));
//        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(fileHandleResolver));
        assetManager.setLoader(TiledMap.class, new TmxMapLoader(fileHandleResolver));

        assetManager.load("backgrounds/1.tmx", TiledMap.class);

        assetManager.load("ui/pack.atlas", TextureAtlas.class);

        // First complete loading the currently required assets.
        assetManager.finishLoading();

        // Use the initially loaded elements.
        projectLocus.uiTextureAtlas = assetManager.get("ui/pack.atlas", TextureAtlas.class);

        logo = projectLocus.uiTextureAtlas.createSprite("logo");
        logo.setSize(571, 200);

        projectLocus.tiledMapList.add(assetManager.get("backgrounds/1.tmx", TiledMap.class));
        TiledMap tiledMap = projectLocus.tiledMapList.get(0);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        // Now load heavy objects here.
        assetManager.load("fonts/font24.fnt", BitmapFont.class);
        assetManager.load("fonts/font24Selected.fnt", BitmapFont.class);
        assetManager.load("fonts/font32.fnt", BitmapFont.class);
        assetManager.load("fonts/font32Selected.fnt", BitmapFont.class);
        assetManager.load("fonts/font32Red.fnt", BitmapFont.class);
        assetManager.load("fonts/font32Green.fnt", BitmapFont.class);
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

        logo.setPosition(ProjectLocus.screenCameraHalfWidth - logo.getWidth() / 2,
                ProjectLocus.screenCameraHalfHeight - logo.getHeight() / 2);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundMovementAngleRad += delta * ProjectLocus.SCREEN_CAMERA_MOVEMENT_SPEED;
        backgroundCamera.position.set(
                ProjectLocus.WORLD_HALF_WIDTH + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.cos(backgroundMovementAngleRad),
                ProjectLocus.WORLD_HALF_HEIGHT + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();

        logo.draw(projectLocus.spriteBatch);

        projectLocus.spriteBatch.draw(barBackgroundTexture,
                ProjectLocus.screenCameraHalfWidth - 104f,
                ProjectLocus.screenCameraHalfHeight - 152f,
                208f, 24f);

        if (assetManager.update()) {

            projectLocus.spriteBatch.draw(barForegroundTexture,
                    ProjectLocus.screenCameraHalfWidth - 100f,
                    ProjectLocus.screenCameraHalfHeight - 148f,
                    200f, 16f);

            // Store the loaded assets.

            projectLocus.font24 = assetManager.get("fonts/font24.fnt", BitmapFont.class);
            projectLocus.font24Selected = assetManager.get("fonts/font24Selected.fnt",
                    BitmapFont.class);
            projectLocus.font24Selected.setColor(ProjectLocus.FONT_SELECTED_COLOR);

            projectLocus.font32 = assetManager.get("fonts/font32.fnt", BitmapFont.class);
            projectLocus.font32Selected = assetManager.get("fonts/font32Selected.fnt",
                    BitmapFont.class);
            projectLocus.font32Selected.setColor(ProjectLocus.FONT_SELECTED_COLOR);
            projectLocus.font32Red = assetManager.get("fonts/font32Red.fnt",
                    BitmapFont.class);
            projectLocus.font32Red.setColor(ProjectLocus.FONT_RED_COLOR);
            projectLocus.font32Green = assetManager.get("fonts/font32Green.fnt",
                    BitmapFont.class);
            projectLocus.font32Green.setColor(ProjectLocus.FONT_GREEN_COLOR);

            for (Integer backgroundType = 2; backgroundType <= 8; backgroundType++) {
                projectLocus.tiledMapList.add(assetManager.get("backgrounds/" +
                        String.valueOf(backgroundType) + ".tmx", TiledMap.class));
            }

            projectLocus.bulletTextureAtlas = assetManager.get("sprites/bullets/pack.atlas",
                    TextureAtlas.class);

            projectLocus.bulletLoader = new BulletLoader(projectLocus.bulletTextureAtlas);

            projectLocus.shipTextureAtlas = assetManager.get("sprites/entities/0/pack.atlas",
                    TextureAtlas.class);

            projectLocus.planetTextureAtlas = assetManager.get("sprites/entities/1/pack.atlas",
                    TextureAtlas.class);

            projectLocus.moonTextureAtlas = assetManager.get("sprites/entities/2/pack.atlas",
                    TextureAtlas.class);

            projectLocus.entityLoader = new EntityLoader(projectLocus);

            projectLocus.setScreen(new SelectPlayerScreen(projectLocus, backgroundMovementAngleRad));

        } else {

            projectLocus.spriteBatch.draw(barForegroundTexture,
                    ProjectLocus.screenCameraHalfWidth - 100f,
                    ProjectLocus.screenCameraHalfHeight - 148f,
                    200f * assetManager.getProgress(), 16f);

        }

        projectLocus.spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth, ProjectLocus.screenCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
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
