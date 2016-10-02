package com.locus.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.Main;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
public class MultiPlayerPlayScreen implements Screen {

    public static Timer timer;

    public Main main;
    private Level level;

    public MultiPlayerPlayScreen(Main main) {
        
        this.main = main;

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
        moonPropertyList.add(new Moon.Property(Moon.Type.Iron, 400f, Main.PI_BY_TWO));

        level = new Level(main, Planet.Type.Gas, moonPropertyList, 1);

        timer = new Timer();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(main.spriteBatch);

    }

    @Override
    public void resize(int width, int height) {
        level.resize(width, height);
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
