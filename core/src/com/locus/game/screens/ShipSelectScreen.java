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
import com.badlogic.gdx.math.Vector3;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.Text;

import java.util.HashMap;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Player Select Screen
 */

class ShipSelectScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private static final float SHIP_SPRITE_SCALE = 10f, SIDE_PADDING = 25f, BOTTOM_PADDING = 44f;

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
            new Color(200f / 255f, 150f / 255f, 80f / 255f, 1),
            Color.SKY,
            Color.CYAN,
            Color.YELLOW,
            Color.CHARTREUSE,
            Color.ROYAL,
            Color.VIOLET
    };
    private InputMultiplexer inputMultiplexer;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private EntityLoader.Definition selectedDefinition;
    private Text exitGameText, doneText, exitGameSelectedText, doneSelectedText;
    private boolean isExitGame, isDoneSelection;
    private float maxSpeed, maxHealth, maxDamage;

    private int selectedShipTypeIndex, selectedShipColorIndex;

    ShipSelectScreen(ProjectLocus projectLocus, float backgroundMovementAngleRad) {

        this.projectLocus = projectLocus;
        this.backgroundMovementAngleRad = backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(
                projectLocus.tiledMapList.get(MathUtils.random(0, 7)),
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

        maxHealth = maxSpeed = maxDamage = 0;

        Sprite sprite;
        EntityLoader.Definition definition;
        for (Ship.Type shipType : shipTypeArray) {
            sprite = projectLocus.shipTextureAtlas.createSprite(shipType.toString());
            definition = projectLocus.entityLoader.get(Entity.Type.Ship, shipType.ordinal());
            sprite.setSize(definition.width * SHIP_SPRITE_SCALE,
                    definition.height * SHIP_SPRITE_SCALE);
            shipMap.put(shipType, sprite);
            shipDefinitionMap.put(shipType, definition);
            maxHealth = Math.max(definition.maxHealth, maxHealth);
            maxSpeed = Math.max(definition.maxSpeed, maxSpeed);
            maxDamage = Math.max(definition.maxDamage, maxDamage);
        }

        selectedShipTypeIndex = selectedShipColorIndex = 0;
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);

        doneText = new Text(projectLocus.font32, "DONE");
        doneText.setPosition(ProjectLocus.screenCameraWidth - SIDE_PADDING -
                doneText.getWidth(), BOTTOM_PADDING);
        exitGameText = new Text(projectLocus.font32, "EXIT");
        exitGameText.setPosition(SIDE_PADDING, BOTTOM_PADDING);

        doneSelectedText = new Text(projectLocus.font32Selected, "DONE");
        doneSelectedText.setPosition(ProjectLocus.screenCameraWidth - SIDE_PADDING -
                doneSelectedText.getWidth(), BOTTOM_PADDING);
        exitGameSelectedText = new Text(projectLocus.font32Selected, "EXIT");
        exitGameSelectedText.setPosition(SIDE_PADDING, BOTTOM_PADDING);

        isDoneSelection = false;
        isExitGame = false;

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
        try {
            if (!projectLocus.screenBackgroundMusic.isPlaying()) {
                projectLocus.screenBackgroundMusic.setVolume(0f);
            } else {
                projectLocus.screenBackgroundMusic.setVolume(0.8f);
            }
            projectLocus.screenBackgroundMusic.setLooping(true);
            projectLocus.screenBackgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.log("Sound Error", "Error - " + e.toString());
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (projectLocus.screenBackgroundMusic.getVolume() < 1f) {
            projectLocus.screenBackgroundMusic.setVolume(
                    projectLocus.screenBackgroundMusic.getVolume() + delta
            );
        }

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
                200f * (selectedDefinition.maxHealth / maxHealth), 16f);

        projectLocus.font24.draw(projectLocus.spriteBatch, "Speed", statPositionX, statPositionY - 24f);
        projectLocus.spriteBatch.draw(barBackgroundTexture,
                statPositionX + 124f, statPositionY - 44f,
                208f, 24f);
        projectLocus.spriteBatch.draw(barForegroundTexture,
                statPositionX + 128f, statPositionY - 40f,
                200f * (selectedDefinition.maxSpeed / maxSpeed), 16f);

        projectLocus.font24.draw(projectLocus.spriteBatch, "Power", statPositionX, statPositionY - 56f);
        projectLocus.spriteBatch.draw(barBackgroundTexture,
                statPositionX + 124f, statPositionY - 76f,
                208f, 24f);
        projectLocus.spriteBatch.draw(barForegroundTexture,
                statPositionX + 128f, statPositionY - 72f,
                200f * (selectedDefinition.maxDamage / maxDamage), 16f);

        if (isExitGame) {
            exitGameSelectedText.draw(projectLocus.spriteBatch);
        } else {
            exitGameText.draw(projectLocus.spriteBatch);
        }
        if (isDoneSelection) {
            doneSelectedText.draw(projectLocus.spriteBatch);
        } else {
            doneText.draw(projectLocus.spriteBatch);
        }

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
        projectLocus.flingHorizontalSound.play();
        selectedShipTypeIndex++;
        if (selectedShipTypeIndex == shipTypeArray.length) {
            selectedShipTypeIndex = 0;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);
    }

    private void previousShip() {
        projectLocus.flingHorizontalSound.play();
        selectedShipTypeIndex--;
        if (selectedShipTypeIndex < 0) {
            selectedShipTypeIndex = shipTypeArray.length - 1;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
        selectedDefinition = shipDefinitionMap.get(shipTypeArray[selectedShipTypeIndex]);
    }

    private void nextShipColor() {
        projectLocus.flingVerticalSound.play();
        selectedShipColorIndex++;
        if (selectedShipColorIndex == shipColorArray.length) {
            selectedShipColorIndex = 0;
        }
        shipMap.get(shipTypeArray[selectedShipTypeIndex])
                .setColor(shipColorArray[selectedShipColorIndex]);
    }

    private void previousShipColor() {
        projectLocus.flingVerticalSound.play();
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
        projectLocus.screenTransitionSound.play();
        projectLocus.mainMenuScreen = new MainMenuScreen(projectLocus, this);
        projectLocus.setScreen(projectLocus.mainMenuScreen);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                previousShip();
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                nextShip();
                break;
            case Input.Keys.W:
            case Input.Keys.UP:
                previousShipColor();
                break;
            case Input.Keys.S:
            case Input.Keys.DOWN:
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
        if (exitGameText.getTextBoundingBox().contains(touchPoint)) {
            isExitGame = true;
        } else if (doneText.getTextBoundingBox().contains(touchPoint)) {
            isDoneSelection = true;
        } else if (arrowLeft.getBoundingRectangle().contains(touchPoint.x, touchPoint.y)) {
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
        if (isExitGame) {
            isExitGame = false;
            Gdx.app.exit();
        }
        if (isDoneSelection) {
            isDoneSelection = false;
            submit();
        }
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
