package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.tools.Hud;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Player Play Screen
 */
class MultiPlayerPlayScreen implements Screen {

    public ProjectLocus projectLocus;
    private Hud hud;
    public Level level;
    private OrthographicCamera foregroundCamera;

    MultiPlayerPlayScreen(ProjectLocus projectLocus, LobbyScreen lobbyScreen) {

        this.projectLocus = projectLocus;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        hud = new Hud(projectLocus, Hud.Type.Survival, foregroundCamera);

        level = new Level(projectLocus, hud, foregroundCamera, lobbyScreen.getLevelProperty(), true);

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
    public synchronized void render(float delta) {

        level.update(delta);
        level.render(projectLocus.spriteBatch);

        hud.update(level.getShipStateList());
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
