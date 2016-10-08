package com.locus.game.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.tools.Hud;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
class MultiPlayerPlayScreenClient implements Screen {

    public ProjectLocus projectLocus;
    private Hud hud;
    public ClientLevel level;
    private OrthographicCamera foregroundCamera;

    MultiPlayerPlayScreenClient(ProjectLocus projectLocus, LobbyScreen lobbyScreen) {

        this.projectLocus = projectLocus;

        level = new ClientLevel(projectLocus, lobbyScreen.getLevelProperty());

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        hud = new Hud(projectLocus, Hud.Type.Survival, foregroundCamera);

    }

    @Override
    public void show() {
        level.onShow();
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

    }

}
