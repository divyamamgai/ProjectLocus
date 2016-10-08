package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.tools.Text;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
class PracticePlayScreen implements Screen, InputProcessor {

    public ProjectLocus projectLocus;
    private SelectModeScreen selectModeScreen;
    private Level level;
    private OrthographicCamera foregroundCamera;
    private Text fpsText;

    PracticePlayScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen) {

        this.projectLocus = projectLocus;
        this.selectModeScreen = selectModeScreen;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        fpsText = new Text(projectLocus.font24, "60");

        level = new Level(projectLocus, null, foregroundCamera, selectModeScreen,
                Level.Property.generateRandom(), false);

        level.addShipAlive(projectLocus.playerShipProperty,
                ProjectLocus.WORLD_HALF_WIDTH + 100f, ProjectLocus.WORLD_HALF_HEIGHT + 100f,
                0, true);

        level.addShipAlive(projectLocus.playerShipProperty,
                ProjectLocus.WORLD_HALF_WIDTH + 100f, ProjectLocus.WORLD_HALF_HEIGHT,
                0, false);

    }

    @Override
    public void show() {
        try {
            if (!projectLocus.playScreenBackgroundMusic.isPlaying()) {
                projectLocus.playScreenBackgroundMusic.setVolume(0.8f);
                projectLocus.playScreenBackgroundMusic.setLooping(true);
                projectLocus.playScreenBackgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.log("Sound Error", "Error - " + e.toString());
        }

        level.getInputMultiplexer().addProcessor(this);
        level.onShow();
    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);

        fpsText.setTextFast(String.valueOf(Gdx.graphics.getFramesPerSecond()));

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();
        fpsText.draw(projectLocus.spriteBatch);
        projectLocus.spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        fpsText.setPosition(ProjectLocus.screenCameraWidth - fpsText.getWidth() - 8,
                ProjectLocus.screenCameraHeight - fpsText.getHeight());
        level.resize();
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
        level.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
            case Input.Keys.P:
            case Input.Keys.BACKSPACE:
            case Input.Keys.BACK:
                projectLocus.setScreen(new PauseScreen(projectLocus, selectModeScreen, this));
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
}
