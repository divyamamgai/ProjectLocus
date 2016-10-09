package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.locus.game.ProjectLocus;
import com.locus.game.tools.Text;

import java.lang.String;

/**
 * Created by Rohit Yadav on 07-Oct-16.
 * Error Screen
 */

public class ErrorScreen implements Screen, InputProcessor {

    private Text errorText, errorStringText, backText, backSelectedText;
    private ProjectLocus projectLocus;
    private static final int TOP_PADDING = 60;
    private OrthographicCamera foregroundCamera;
    private boolean isBackPressed;
    private InputMultiplexer inputMultiplexer;
    private MainMenuScreen mainMenuScreen;

    public ErrorScreen(ProjectLocus projectLocus, MainMenuScreen mainMenuScreen,
                       String errorString) {
        this.projectLocus = projectLocus;
        this.mainMenuScreen = mainMenuScreen;

        errorText = new Text(projectLocus.font72, "ERROR");
        backText = new Text(projectLocus.font32, "BACK");
        backSelectedText = new Text(projectLocus.font32Selected, "BACK");
        errorStringText = new Text(projectLocus.font24, errorString);
        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        isBackPressed = false;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
    }

    private void positionUI() {
        errorText.setPosition(ProjectLocus.screenCameraHalfWidth - errorText.getHalfWidth(),
                ProjectLocus.screenCameraHeight - errorText.getHeight() - TOP_PADDING);
        errorStringText.setPosition(ProjectLocus.screenCameraHalfWidth -
                errorStringText.getHalfWidth(), ProjectLocus.screenCameraHalfHeight -
                errorStringText.getHalfHeight());
        backText.setPosition(ProjectLocus.screenCameraHalfWidth - backText.getHalfWidth(),
                (2 * ProjectLocus.screenCameraHalfHeight) - errorText.getY() + 5);
        backSelectedText.setPosition(ProjectLocus.screenCameraHalfWidth -
                backSelectedText.getHalfWidth(), (2 * ProjectLocus.screenCameraHalfHeight) -
                errorText.getY() + 5);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();
        errorText.draw(projectLocus.spriteBatch);
        errorStringText.draw(projectLocus.spriteBatch);
        if (isBackPressed) {
            backSelectedText.draw(projectLocus.spriteBatch);
        } else {
            backText.draw(projectLocus.spriteBatch);
        }
        projectLocus.spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
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
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        switch (keycode) {
            case Input.Keys.ENTER:
            case Input.Keys.BACK:
                projectLocus.screenTransitionSound.play();
                projectLocus.setScreen(mainMenuScreen);
                break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        foregroundCamera.unproject(touchPosition);
        if (backText.getTextBoundingBox().contains(touchPosition)) {
            isBackPressed = true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isBackPressed) {
            projectLocus.screenTransitionSound.play();
            isBackPressed = false;
            projectLocus.setScreen(mainMenuScreen);
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
}
