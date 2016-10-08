package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.locus.game.ProjectLocus;
import com.locus.game.network.ShipState;
import com.locus.game.tools.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Rohit Yadav on 05-Oct-16.
 * Scoreboard Screen
 */

public class ScoreBoardScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private class PlayerResultData {

        private Sprite shipSprite;
        private Text playerScoreText, playerNumberText;
        private short score;

        PlayerResultData(Sprite shipSprite, Text playerNumberText, Text playerScoreText,
                         short score) {
            this.shipSprite = shipSprite;
            this.playerNumberText = playerNumberText;
            this.playerScoreText = playerScoreText;
            this.score = score;
        }
    }

    private float backgroundMovementAngleRad;

    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private ArrayList<PlayerResultData> playerResultList;
    private TextureRegion goldMedalTexture, silverMedalTexture, bronzeMedalTexture;

    private static final int ROW_PADDING = 50, COLUMN_PADDING = 50, SHIP_PADDING = 34,
            MARGIN_TOP = 80;

    private Text doneText, scoreBoardText;
    private boolean isTiledMapCreated;

    public ScoreBoardScreen(ProjectLocus projectLocus, LobbyScreen lobbyScreen,
                            ArrayList<ShipState> shipStateList) {
        this.projectLocus = projectLocus;
        this.lobbyScreen = lobbyScreen;

        backgroundMovementAngleRad = 0;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        doneText = new Text(projectLocus.font32, "DONE");
        scoreBoardText = new Text(projectLocus.font32, "STANDINGS");
        playerResultList = new ArrayList<PlayerResultData>();

        int i = 0;
        for (ShipState shipState : shipStateList) {
            playerResultList.add(
                    new PlayerResultData(lobbyScreen.playerDisplayList.get(i).getShipSprite(),
                            new Text(projectLocus.font24,
                                    String.format(Locale.ENGLISH, "%01d", (i + 1))),
                            new Text(projectLocus.font24,
                                    String.format(Locale.ENGLISH, "%04d", shipState.score)),
                            shipState.score));
            i++;
        }

        Collections.sort(playerResultList, new Comparator<PlayerResultData>() {
            @Override
            public int compare(PlayerResultData a, PlayerResultData b) {
                return (b.score >= a.score) ? 1 : -1;
            }
        });

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

        isTiledMapCreated = false;

        goldMedalTexture = projectLocus.uiTextureAtlas.findRegion("goldMedal");
        silverMedalTexture = projectLocus.uiTextureAtlas.findRegion("silverMedal");
        bronzeMedalTexture = projectLocus.uiTextureAtlas.findRegion("bronzeMedal");

    }

    private void positionUI() {

        int row, col;
        float colWidth = (ProjectLocus.screenCameraWidth - (5 * COLUMN_PADDING)) / 4,
                rowHeight = ((ProjectLocus.screenCameraHeight - 80) - (3 * ROW_PADDING)) / 2;

        PlayerResultData playerResultData;
        for (int i = 0; i < playerResultList.size(); i++) {

            row = i / 4;
            col = i % 4;

            playerResultData = playerResultList.get(i);

            playerResultData.shipSprite.setPosition(COLUMN_PADDING + (col * (colWidth + COLUMN_PADDING))
                            + ((colWidth - playerResultData.shipSprite.getWidth()) / 2),
                    ROW_PADDING + (((row + 1) % 2) * (rowHeight + ROW_PADDING)) +
                            ((rowHeight - playerResultData.shipSprite.getHeight()) / 2));

            playerResultData.playerNumberText.setPosition(playerResultData.shipSprite.getX() +
                            ((playerResultData.shipSprite.getWidth() / 2)
                                    - playerResultData.playerNumberText.getHalfWidth()),
                    playerResultData.shipSprite.getY() +
                            playerResultData.shipSprite.getHeight() + SHIP_PADDING);

            playerResultData.playerScoreText.setPosition(playerResultData.shipSprite.getX() +
                            ((playerResultData.shipSprite.getWidth() / 2)
                                    - playerResultData.playerScoreText.getHalfWidth()),
                    playerResultData.shipSprite.getY() - SHIP_PADDING / 2);

        }

        doneText.setPosition(
                ProjectLocus.screenCameraWidth - COLUMN_PADDING - doneText.getWidth(),
                ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING -
                        doneText.getHalfHeight());

        scoreBoardText.setPosition(COLUMN_PADDING, ProjectLocus.screenCameraHeight -
                MARGIN_TOP + ROW_PADDING - scoreBoardText.getHalfHeight());

    }

    private void drawScoreBoardScreen(SpriteBatch spriteBatch) {

        int i = 0;
        scoreBoardText.draw(spriteBatch);
        for (PlayerResultData playerResultData : playerResultList) {
            playerResultData.playerNumberText.draw(spriteBatch);
            playerResultData.shipSprite.draw(spriteBatch);
            if (i < 3) {
                switch (i) {
                    case 0:
                        spriteBatch.draw(goldMedalTexture,
                                playerResultData.shipSprite.getX() - 16,
                                playerResultData.shipSprite.getY() - 24, 32, 48);
                        break;
                    case 1:
                        spriteBatch.draw(silverMedalTexture,
                                playerResultData.shipSprite.getX() - 16,
                                playerResultData.shipSprite.getY() - 24, 32, 48);
                        break;
                    case 2:
                        spriteBatch.draw(bronzeMedalTexture,
                                playerResultData.shipSprite.getX() - 16,
                                playerResultData.shipSprite.getY() - 24, 32, 48);
                        break;
                }
                i++;
            }
            playerResultData.playerScoreText.draw(spriteBatch);
        }
        doneText.draw(spriteBatch);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

        if (!isTiledMapCreated) {
            tiledMapRenderer = new OrthogonalTiledMapRenderer(
                    projectLocus.tiledMapList.get(MathUtils.random(0, 7)),
                    ProjectLocus.TILED_MAP_SCALE);
            isTiledMapCreated = true;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundMovementAngleRad += delta * ProjectLocus.SCREEN_CAMERA_MOVEMENT_SPEED;
        backgroundCamera.position.set(
                ProjectLocus.WORLD_HALF_WIDTH + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.cos(backgroundMovementAngleRad),
                ProjectLocus.WORLD_HALF_HEIGHT + ProjectLocus.SCREEN_CAMERA_MOVEMENT_RADIUS *
                        MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();
        drawScoreBoardScreen(projectLocus.spriteBatch);
        projectLocus.spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        ProjectLocus.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);
        positionUI();
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

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ENTER:
            case Input.Keys.BACK:
                try {
                    projectLocus.playScreenBackgroundMusic.stop();
                } catch (Exception e) {
                    Gdx.app.log("Sound Error", "Error - " + e.toString());
                }
                projectLocus.screenTransitionSound.play();
                projectLocus.setScreen(lobbyScreen.selectModeScreen);
                lobbyScreen.dispose();
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
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        foregroundCamera.unproject(touchPosition);
        if (doneText.getTextBoundingBox().contains(touchPosition)) {
            try {
                projectLocus.playScreenBackgroundMusic.stop();
            } catch (Exception e) {
                Gdx.app.log("Sound Error", "Error - " + e.toString());
            }
            projectLocus.screenTransitionSound.play();
            projectLocus.setScreen(lobbyScreen.selectModeScreen);
            lobbyScreen.dispose();
        }
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

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
