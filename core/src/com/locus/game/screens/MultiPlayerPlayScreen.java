package com.locus.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.tools.Hud;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
public class MultiPlayerPlayScreen implements Screen {

    public ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private Hud hud;
    public Level level;
    private OrthographicCamera foregroundCamera;

    MultiPlayerPlayScreen(ProjectLocus projectLocus, LobbyScreen lobbyScreen) {

        this.projectLocus = projectLocus;
        this.lobbyScreen = lobbyScreen;

        level = new Level(projectLocus, lobbyScreen.levelProperty);

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        hud = new Hud(projectLocus, Hud.Type.Survival, foregroundCamera);

    }

    @Override
    public void show() {
        level.bindController();
    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);

        hud.updateTimer(delta);
        hud.draw(projectLocus.spriteBatch);
        
    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        level.resize();
        hud.positionUI();
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
