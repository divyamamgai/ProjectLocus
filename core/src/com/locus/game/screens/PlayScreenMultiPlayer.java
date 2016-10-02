package com.locus.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.tools.Hud;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
public class PlayScreenMultiPlayer implements Screen {

    public ProjectLocus projectLocus;
    private SelectScreenMode selectScreenMode;
    private Level level;
    private Hud hud;

    public PlayScreenMultiPlayer(ProjectLocus projectLocus, SelectScreenMode selectScreenMode) {

        this.projectLocus = projectLocus;
        this.selectScreenMode = selectScreenMode;

        Server server = new Server();
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) {

            }
        });
        server.start();
        try {
            server.bind(54555, 54777);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Moon.Property> moonPropertyList = new ArrayList<Moon.Property>();
        moonPropertyList.add(new Moon.Property(Moon.Type.Organic, 200f, 0f));
        moonPropertyList.add(new Moon.Property(Moon.Type.DarkIce, 300f, MathUtils.PI));
        moonPropertyList.add(new Moon.Property(Moon.Type.Iron, 400f, ProjectLocus.PI_BY_TWO));

        level = new Level(projectLocus, Planet.Type.Gas, moonPropertyList, 1);

        hud = new Hud(projectLocus.spriteBatch, Hud.Type.Survival);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);

        hud.update(delta);
        hud.draw();
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
