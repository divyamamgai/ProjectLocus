package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;

/**
 * Created by Divya Mamgai on 10/2/2016.
 * Lobby Host Screen
 */

public class LobbyScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    public enum Type {
        Host,
        Client
    }

    public enum State {
        Starting,
        Started,
        Searching,
        Connecting,
        Connected,
        Failed
    }

    private float backgroundMovementAngleRad;
    private float startingFontHalfWidth, startedFontHalfWidth, failedFontHalfWidth,
            searchingFontHalfWidth, connectingFontHalfWidth, connectedFontHalfWidth;

    private ProjectLocus projectLocus;
    private SelectModeScreen selectModeScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;

    private Type type;
    public State state = State.Starting;

    LobbyScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen,
                LobbyScreen.Type type) {

        this.projectLocus = projectLocus;
        this.selectModeScreen = selectModeScreen;
        this.type = type;

        backgroundMovementAngleRad = selectModeScreen.backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraHalfWidth,
                ProjectLocus.screenCameraHalfHeight);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);
        backgroundCamera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(projectLocus.tiledMapList.get(0),
                ProjectLocus.TILED_MAP_SCALE);

        projectLocus.glyphLayout.setText(projectLocus.font32, "Starting...");
        startingFontHalfWidth = projectLocus.glyphLayout.width / 2f;
        projectLocus.glyphLayout.setText(projectLocus.font32, "Started");
        startedFontHalfWidth = projectLocus.glyphLayout.width / 2f;
        projectLocus.glyphLayout.setText(projectLocus.font32, "Searching...");
        searchingFontHalfWidth = projectLocus.glyphLayout.width / 2f;
        projectLocus.glyphLayout.setText(projectLocus.font32, "Connecting...");
        connectingFontHalfWidth = projectLocus.glyphLayout.width / 2f;
        projectLocus.glyphLayout.setText(projectLocus.font32, "Connected");
        connectedFontHalfWidth = projectLocus.glyphLayout.width / 2f;
        projectLocus.glyphLayout.setText(projectLocus.font32, "Failed");
        failedFontHalfWidth = projectLocus.glyphLayout.width / 2f;

        switch (type) {
            case Host:
                state = State.Starting;
                projectLocus.gameServer.start(this);
                break;
            case Client:
                state = State.Searching;
                projectLocus.gameClient.start(this);
                break;
        }

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

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

        switch (type) {
            case Host:
                switch (state) {
                    case Starting:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Starting...",
                                ProjectLocus.screenCameraHalfWidth - startingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Started:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Started",
                                ProjectLocus.screenCameraHalfWidth - startedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Failed:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Failed",
                                ProjectLocus.screenCameraHalfWidth - failedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                }
                break;
            case Client:
                switch (state) {
                    case Searching:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Searching...",
                                ProjectLocus.screenCameraHalfWidth - searchingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Connecting:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Connecting...",
                                ProjectLocus.screenCameraHalfWidth - connectingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Connected:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Connected",
                                ProjectLocus.screenCameraHalfWidth - connectedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Failed:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Failed",
                                ProjectLocus.screenCameraHalfWidth - failedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                }
                break;
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

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.BACK:
            case Input.Keys.ESCAPE:
            case Input.Keys.BACKSPACE:
                switch (type) {
                    case Host:
                        projectLocus.gameServer.stop();
                        break;
                    case Client:
                        projectLocus.gameClient.stop();
                        break;
                }
                selectModeScreen.backgroundMovementAngleRad = backgroundMovementAngleRad;
                projectLocus.setScreen(selectModeScreen);
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
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
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
