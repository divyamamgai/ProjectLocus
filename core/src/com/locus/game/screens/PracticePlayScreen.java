package com.locus.game.screens;

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
class PracticePlayScreen implements Screen {

    public ProjectLocus projectLocus;
    private Level level;

    PracticePlayScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen) {

        this.projectLocus = projectLocus;

        ArrayList<Moon.Property> moonPropertyList = new ArrayList<Moon.Property>();
        moonPropertyList.add(new Moon.Property(Moon.Type.Organic, 200f, 0f));
        moonPropertyList.add(new Moon.Property(Moon.Type.DarkIce, 300f, MathUtils.PI));
        moonPropertyList.add(new Moon.Property(Moon.Type.Iron, 400f, ProjectLocus.PI_BY_TWO));

        level = new Level(projectLocus, new Level.Property(Planet.Type.Gas, moonPropertyList, 1));

    }

    @Override
    public void show() {

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

}
