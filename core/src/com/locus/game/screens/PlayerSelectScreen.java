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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.locus.game.Main;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;

import java.util.HashMap;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Player Select Screen
 */

class PlayerSelectScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private static final float CAMERA_MOVEMENT_SPEED = 0.05f;
    private static final float CAMERA_MOVEMENT_RADIUS = 512f;
    private static final float SHIP_SPRITE_SCALE = 2f;

    float backgroundMovementAngleRad;
    private float statPositionX, statPositionY;

    private Main main;
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

    private int selectedShipTypeIndex, selectedShipColorIndex;

    PlayerSelectScreen(Main main, float backgroundMovementAngleRad) {

        this.main = main;
        this.backgroundMovementAngleRad = backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        foregroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        backgroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        backgroundCamera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(main.tiledMapList.get(0),
                Main.TILED_MAP_SCALE);

        logo = main.uiTextureAtlas.createSprite("logo");
        logo.setSize(71.31f, 25f);

        arrowLeft = main.uiTextureAtlas.createSprite("arrowLeft");
        arrowLeft.setSize(6f, 6f);
        arrowRight = main.uiTextureAtlas.createSprite("arrowRight");
        arrowRight.setSize(6f, 6f);
        arrowUp = main.uiTextureAtlas.createSprite("arrowUp");
        arrowUp.setSize(4f, 4f);
        arrowDown = main.uiTextureAtlas.createSprite("arrowDown");
        arrowDown.setSize(4f, 4f);

        shipMap = new HashMap<Ship.Type, Sprite>();
        shipDefinitionMap = new HashMap<Ship.Type, EntityLoader.Definition>();

        Sprite sprite;
        EntityLoader.Definition definition;
        for (Ship.Type shipType : shipTypeArray) {
            sprite = main.shipTextureAtlas.createSprite(shipType.toString());
            definition = main.entityLoader.get(Entity.Type.Ship, shipType.ordinal());
            sprite.setSize(definition.width * SHIP_SPRITE_SCALE,
                    definition.height * SHIP_SPRITE_SCALE);
            shipMap.put(shipType, sprite);
            shipDefinitionMap.put(shipType, definition);
        }

        selectedShipTypeIndex = selectedShipColorIndex = 0;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        logo.setPosition(Main.cameraHalfWidth - logo.getWidth() / 2,
                Main.cameraHeight - logo.getHeight() - 10f);

        arrowLeft.setPosition(Main.cameraHalfWidth - 20f, Main.cameraHalfHeight - 8f);
        arrowRight.setPosition(Main.cameraHalfWidth + 14f, Main.cameraHalfHeight - 8f);
        arrowUp.setPosition(Main.cameraHalfWidth - 2f, Main.cameraHalfHeight + 5f);
        arrowDown.setPosition(Main.cameraHalfWidth - 2f, Main.cameraHalfHeight - 19f);

        EntityLoader.Definition definition;
        for (Ship.Type shipType : shipTypeArray) {
            definition = main.entityLoader.get(Entity.Type.Ship, shipType.ordinal());
            shipMap.get(shipType).setPosition(
                    Main.cameraHalfWidth - definition.halfWidth * SHIP_SPRITE_SCALE,
                    Main.cameraHalfHeight - definition.halfHeight * SHIP_SPRITE_SCALE - 5f);
        }

        statPositionX = Main.cameraHalfWidth - 40f;
        statPositionY = Main.cameraHalfHeight - 24f;

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
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

        shipMap.get(shipTypeArray[selectedShipTypeIndex]).draw(main.spriteBatch);

        arrowLeft.draw(main.spriteBatch);
        arrowRight.draw(main.spriteBatch);
        arrowUp.draw(main.spriteBatch);
        arrowDown.draw(main.spriteBatch);

        EntityLoader.Definition definition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);
        main.font1.draw(main.spriteBatch, "Health", statPositionX, statPositionY);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barBackground"),
                statPositionX + 29.5f, statPositionY - 3f,
                33f, 3f);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barForeground"),
                statPositionX + 30f, statPositionY - 2.5f,
                32f * (definition.maxHealth / 3000f),
                2f);

        main.font1.draw(main.spriteBatch, "Speed", statPositionX, statPositionY - 6f);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barBackground"),
                statPositionX + 29.5f, statPositionY - 9f,
                33f, 3f);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barForeground"),
                statPositionX + 30f, statPositionY - 8.5f,
                32f * (definition.maxSpeed / 72f),
                2f);

        main.font1.draw(main.spriteBatch, "Power", statPositionX, statPositionY - 12f);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barBackground"),
                statPositionX + 29.5f, statPositionY - 15f,
                33f, 3f);
        main.spriteBatch.draw(main.uiTextureAtlas.findRegion("barForeground"),
                statPositionX + 30f, statPositionY - 14.5f,
                32f * (definition.maxDamage / 2f),
                2f);

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

    private void nextShip() {
        selectedShipTypeIndex++;
        if (selectedShipTypeIndex == shipTypeArray.length) {
            selectedShipTypeIndex = 0;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
    }

    private void previousShip() {
        selectedShipTypeIndex--;
        if (selectedShipTypeIndex < 0) {
            selectedShipTypeIndex = shipTypeArray.length - 1;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
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
        main.playerColor = shipColorArray[selectedShipColorIndex];
        main.playerShipType = shipTypeArray[selectedShipTypeIndex];
        main.setScreen(new MultiPlayerSelectScreen(main, this));
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
        Vector3 touchPoint = new Vector3(screenX, screenY, 0);
        foregroundCamera.unproject(touchPoint);
        if (arrowLeft.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
            previousShip();
        } else if (arrowRight.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
            nextShip();
        } else if (arrowUp.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
            previousShipColor();
        } else if (arrowDown.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
            nextShipColor();
        }
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
