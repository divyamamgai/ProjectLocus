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

/**
 * Created by Rohit Yadav on 07-Oct-16.
 * Pause Screen
 */

public class PauseScreen implements Screen, InputProcessor {

    private ProjectLocus projectLocus;
    private SelectModeScreen selectModeScreen;
    private Screen screen;
    private OrthographicCamera foregroundCamera;
    private InputMultiplexer inputMultiplexer;
    private static final int TOP_PADDING = 60;
    private Text pauseText, resumeText, resumeSelectedText, quitText, quitSelectedText;
    private boolean isResumePressed, isQuitPressed;

    PauseScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen, Screen screen) {
        this.projectLocus = projectLocus;
        this.selectModeScreen = selectModeScreen;
        this.screen = screen;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        pauseText = new Text(projectLocus.font72, "PAUSE");
        resumeText = new Text(projectLocus.font32, "Resume");
        resumeSelectedText = new Text(projectLocus.font32Selected, "Resume");
        quitText = new Text(projectLocus.font32, "Quit");
        quitSelectedText = new Text(projectLocus.font32Selected, "Quit");
        isResumePressed = isQuitPressed = false;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(this);
    }

    private void positionUI() {
        pauseText.setPosition(ProjectLocus.screenCameraHalfWidth - pauseText.getHalfWidth(),
                ProjectLocus.screenCameraHeight - pauseText.getHeight() - TOP_PADDING);
        resumeText.setPosition(ProjectLocus.screenCameraHalfWidth - resumeText.getHalfWidth(),
                pauseText.getY() - resumeText.getHeight() - (2 * TOP_PADDING));
        resumeSelectedText.setPosition(ProjectLocus.screenCameraHalfWidth -
                resumeSelectedText.getHalfWidth(), pauseText.getY() -
                resumeSelectedText.getHeight() - (2 * TOP_PADDING));
        quitText.setPosition(ProjectLocus.screenCameraHalfWidth - quitText.getHalfWidth(),
                resumeText.getY() - quitText.getHeight() - TOP_PADDING);
        quitSelectedText.setPosition(ProjectLocus.screenCameraHalfWidth - quitSelectedText.getHalfWidth(),
                resumeText.getY() - quitSelectedText.getHeight() - TOP_PADDING);
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
        pauseText.draw(projectLocus.spriteBatch);
        if (isResumePressed) {
            resumeSelectedText.draw(projectLocus.spriteBatch);
        } else {
            resumeText.draw(projectLocus.spriteBatch);
        }
        if (isQuitPressed) {
            quitSelectedText.draw(projectLocus.spriteBatch);
        } else {
            quitText.draw(projectLocus.spriteBatch);
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
            case Input.Keys.DOWN:
                projectLocus.flingVerticalSound.play();
                if (!isResumePressed && !isQuitPressed) {
                    isResumePressed = true;
                } else if (isResumePressed) {
                    isQuitPressed = true;
                    isResumePressed = false;
                } else {
                    isResumePressed = true;
                    isQuitPressed = false;
                }
                break;
            case Input.Keys.UP:
                projectLocus.flingVerticalSound.play();
                if (!isResumePressed && !isQuitPressed) {
                    isQuitPressed = true;
                } else if (isResumePressed) {
                    isQuitPressed = true;
                    isResumePressed = false;
                } else {
                    isResumePressed = true;
                    isQuitPressed = false;
                }
                break;
            case Input.Keys.ENTER:
                if (isResumePressed) {
                    projectLocus.flingHorizontalSound.play();
                    isResumePressed = false;
                    projectLocus.setScreen(screen);
                }
                if (isQuitPressed) {
                    projectLocus.screenTransitionSound.play();
                    isQuitPressed = false;
                    try {
                        projectLocus.playScreenBackgroundMusic.stop();
                    } catch (Exception e) {
                        Gdx.app.log("Sound Error", "Error - " + e.toString());
                    }
                    projectLocus.setScreen(selectModeScreen);
                }
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
        if (resumeText.getTextBoundingBox().contains(touchPosition)) {
            isResumePressed = true;
        }
        if (quitText.getTextBoundingBox().contains(touchPosition)) {
            isQuitPressed = true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isResumePressed) {
            projectLocus.flingHorizontalSound.play();
            isResumePressed = false;
            projectLocus.setScreen(screen);
        }
        if (isQuitPressed) {
            projectLocus.screenTransitionSound.play();
            isQuitPressed = false;
            try {
                projectLocus.playScreenBackgroundMusic.stop();
            } catch (Exception e) {
                Gdx.app.log("Sound Error", "Error - " + e.toString());
            }
            projectLocus.setScreen(selectModeScreen);
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
