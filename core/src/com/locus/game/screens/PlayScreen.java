package com.locus.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.Main;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;

import java.io.IOException;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Ship Play Screen
 */
public class PlayScreen implements Screen {

    public static Timer timer;

    public Main main;
    private Level level;

    public PlayScreen(Main main) {

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

        this.main = main;

        level = new Level(Planet.Type.Gas, Moon.Type.Organic, 1);

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
