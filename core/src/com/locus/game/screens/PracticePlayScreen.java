package com.locus.game.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
class PracticePlayScreen implements Screen, InputProcessor {

    public ProjectLocus projectLocus;
    private SelectModeScreen selectModeScreen;
    private Level level;

    PracticePlayScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen) {

        this.projectLocus = projectLocus;
        this.selectModeScreen = selectModeScreen;

        ArrayList<Moon.Property> moonPropertyList = new ArrayList<Moon.Property>();
        moonPropertyList.add(new Moon.Property(Moon.Type.Organic, 200f, 0f));
        moonPropertyList.add(new Moon.Property(Moon.Type.DarkIce, 300f, MathUtils.PI));
        moonPropertyList.add(new Moon.Property(Moon.Type.Iron, 400f, ProjectLocus.PI_BY_TWO));

        level = new Level(projectLocus,
                new Level.Property(Planet.Type.Gas, moonPropertyList, 1), false);

        level.addShipAlive(projectLocus.playerShipProperty,
                ProjectLocus.WORLD_HALF_WIDTH + 100f, ProjectLocus.WORLD_HALF_HEIGHT + 100f,
                0, true);

        level.addShipAlive(projectLocus.playerShipProperty,
                ProjectLocus.WORLD_HALF_WIDTH + 100f, ProjectLocus.WORLD_HALF_HEIGHT,
                0, false);

    }

    @Override
    public void show() {
        level.getInputMultiplexer().addProcessor(this);
        level.onShow();
    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);
//        level.render(projectLocus.spriteBatch, delta);

    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
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
