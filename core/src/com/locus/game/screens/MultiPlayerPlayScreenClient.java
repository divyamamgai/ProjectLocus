package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
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

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        hud = new Hud(projectLocus, Hud.Type.Survival, foregroundCamera);

        level = new ClientLevel(projectLocus, hud, lobbyScreen.getLevelProperty());

    }

    @Override
    public void show() {
        level.onShow();
        try {
            if (projectLocus.lobbyScreenBackgroundMusic.isPlaying()) {
                projectLocus.lobbyScreenBackgroundMusic.stop();
            }
            projectLocus.playScreenBackgroundMusic.setVolume(0.8f);
            projectLocus.playScreenBackgroundMusic.setLooping(true);
            projectLocus.playScreenBackgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.log("Sound Error", "Error - " + e.toString());
        }
    }

    @Override
    public void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);

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
