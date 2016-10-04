package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;

import java.util.HashMap;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Player Select Screen
 */

class SelectPlayerScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private static final float SHIP_SPRITE_SCALE = 10f;

    float backgroundMovementAngleRad;
    private float statPositionX, statPositionY;

    private ProjectLocus projectLocus;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private Sprite logo, arrowLeft, arrowRight, arrowUp, arrowDown;
    private HashMap<Ship.Type, Sprite> shipMap;
    private HashMap<Ship.Type, EntityLoader.Definition> shipDefinitionMap;
    private Ship.Type[] shipTypeArray = Ship.Type.values();
    private Color[] shipColorArray = {
            Color.WHITE,
            Color.PINK,
            Color.CYAN,
            Color.YELLOW,
            Color.CHARTREUSE,
            Color.LIME,
            Color.CORAL,
            Color.VIOLET
    };
    private InputMultiplexer inputMultiplexer;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private EntityLoader.Definition selectedDefinition;

    private int selectedShipTypeIndex, selectedShipColorIndex;

    SelectPlayerScreen(ProjectLocus projectLocus, float backgroundMovementAngleRad) {

        this.projectLocus = projectLocus;
        this.backgroundMovementAngleRad = backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        foregroundCamera.setToOrtho(false, ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT);
        backgroundCamera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(projectLocus.tiledMapList.get(0),
                ProjectLocus.TILED_MAP_SCALE);

        logo = projectLocus.uiTextureAtlas.createSprite("logo");
        logo.setSize(366, 128);

        arrowLeft = projectLocus.uiTextureAtlas.createSprite("arrowLeft");
        arrowLeft.setSize(16f, 32f);
        arrowRight = projectLocus.uiTextureAtlas.createSprite("arrowRight");
        arrowRight.setSize(16f, 32f);
        arrowUp = projectLocus.uiTextureAtlas.createSprite("arrowUp");
        arrowUp.setSize(16f, 8f);
        arrowDown = projectLocus.uiTextureAtlas.createSprite("arrowDown");
        arrowDown.setSize(16f, 8f);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        shipMap = new HashMap<Ship.Type, Sprite>();
        shipDefinitionMap = new HashMap<Ship.Type, EntityLoader.Definition>();

        Sprite sprite;
        EntityLoader.Definition definition;
        for (Ship.Type shipType : shipTypeArray) {
            sprite = projectLocus.shipTextureAtlas.createSprite(shipType.toString());
            definition = projectLocus.entityLoader.get(Entity.Type.Ship, shipType.ordinal());
            sprite.setSize(definition.width * SHIP_SPRITE_SCALE,
                    definition.height * SHIP_SPRITE_SCALE);
            shipMap.put(shipType, sprite);
            shipDefinitionMap.put(shipType, definition);
        }

        selectedShipTypeIndex = selectedShipColorIndex = 0;
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        logo.setPosition(ProjectLocus.screenCameraHalfWidth - logo.getWidth() / 2,
                ProjectLocus.screenCameraHeight - logo.getHeight() - 24f);

        arrowLeft.setPosition(ProjectLocus.screenCameraHalfWidth - 96f - arrowLeft.getWidth(),
                ProjectLocus.screenCameraHalfHeight - arrowLeft.getHeight() / 2 - 16f);
        arrowRight.setPosition(ProjectLocus.screenCameraHalfWidth + 96f,
                ProjectLocus.screenCameraHalfHeight - arrowRight.getHeight() / 2 - 16f);
        arrowUp.setPosition(ProjectLocus.screenCameraHalfWidth - arrowDown.getWidth() / 2,
                ProjectLocus.screenCameraHalfHeight + 32f);
        arrowDown.setPosition(ProjectLocus.screenCameraHalfWidth - arrowDown.getWidth() / 2,
                ProjectLocus.screenCameraHalfHeight - 72f - arrowDown.getHeight());

        EntityLoader.Definition definition;
        for (Ship.Type shipType : shipTypeArray) {
            definition = projectLocus.entityLoader.get(Entity.Type.Ship, shipType.ordinal());
            shipMap.get(shipType).setPosition(
                    ProjectLocus.screenCameraHalfWidth - definition.halfWidth * SHIP_SPRITE_SCALE,
                    ProjectLocus.screenCameraHalfHeight - definition.halfHeight * SHIP_SPRITE_SCALE - 18f);
        }

        statPositionX = ProjectLocus.screenCameraHalfWidth - 168f;
        statPositionY = ProjectLocus.screenCameraHalfHeight - 128f;

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
        Gdx.input.setCatchBackKey(true);
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

        shipMap.get(shipTypeArray[selectedShipTypeIndex]).draw(projectLocus.spriteBatch);

        arrowLeft.draw(projectLocus.spriteBatch);
        arrowRight.draw(projectLocus.spriteBatch);
        arrowUp.draw(projectLocus.spriteBatch);
        arrowDown.draw(projectLocus.spriteBatch);

        projectLocus.font24.draw(projectLocus.spriteBatch, "Health", statPositionX, statPositionY + 8f);
        projectLocus.spriteBatch.draw(barBackgroundTexture,
                statPositionX + 124f, statPositionY - 12f,
                208f, 24f);
        projectLocus.spriteBatch.draw(barForegroundTexture,
                statPositionX + 128f, statPositionY - 8f,
                200f * (selectedDefinition.maxHealth / 3000f), 16f);

        projectLocus.font24.draw(projectLocus.spriteBatch, "Speed", statPositionX, statPositionY - 24f);
        projectLocus.spriteBatch.draw(barBackgroundTexture,
                statPositionX + 124f, statPositionY - 44f,
                208f, 24f);
        projectLocus.spriteBatch.draw(barForegroundTexture,
                statPositionX + 128f, statPositionY - 40f,
                200f * (selectedDefinition.maxSpeed / 72f), 16f);

        projectLocus.font24.draw(projectLocus.spriteBatch, "Power", statPositionX, statPositionY - 56f);
        projectLocus.spriteBatch.draw(barBackgroundTexture,
                statPositionX + 124f, statPositionY - 76f,
                208f, 24f);
        projectLocus.spriteBatch.draw(barForegroundTexture,
                statPositionX + 128f, statPositionY - 72f,
                200f * (selectedDefinition.maxDamage / 2f), 16f);

        projectLocus.spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);
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

    private void nextShip() {
        selectedShipTypeIndex++;
        if (selectedShipTypeIndex == shipTypeArray.length) {
            selectedShipTypeIndex = 0;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);
    }

    private void previousShip() {
        selectedShipTypeIndex--;
        if (selectedShipTypeIndex < 0) {
            selectedShipTypeIndex = shipTypeArray.length - 1;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);
    }

    private void nextShipColor() {
        selectedShipColorIndex++;
        if (selectedShipColorIndex == shipColorArray.length) {
            selectedShipColorIndex = 0;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
    }

    private void previousShipColor() {
        selectedShipColorIndex--;
        if (selectedShipColorIndex < 0) {
            selectedShipColorIndex = shipColorArray.length - 1;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
    }

    private void submit() {
        projectLocus.playerShipProperty.color = shipColorArray[selectedShipColorIndex];
        projectLocus.playerShipProperty.type = shipTypeArray[selectedShipTypeIndex];
        projectLocus.setScreen(new SelectModeScreen(projectLocus, this));
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
                previousShip();
                break;
            case Input.Keys.D:
                nextShip();
                break;
            case Input.Keys.W:
                previousShipColor();
                break;
            case Input.Keys.S:
                nextShipColor();
                break;
            case Input.Keys.ENTER:
                submit();
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//        Vector3 touchPoint = new Vector3(screenX, screenY, 0);
//        foregroundCamera.unproject(touchPoint);
//        if (arrowLeft.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
//            previousShip();
//        } else if (arrowRight.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
//            nextShip();
//        } else if (arrowUp.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
//            previousShipColor();
//        } else if (arrowDown.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
//            nextShipColor();
//        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (count == 2) {
            submit();
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        float flingAngle = (float) Math.atan2(velocityY, velocityX) * MathUtils.radiansToDegrees;
        if (flingAngle >= -20 && flingAngle <= 20) {
            nextShip();
        } else if (flingAngle <= -160 || flingAngle >= 160) {
            previousShip();
        } else if (flingAngle <= -70 && flingAngle >= -110) {
            previousShipColor();
        } else if (flingAngle >= 70 && flingAngle <= 110) {
            nextShipColor();
        }
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
