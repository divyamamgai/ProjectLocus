package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.TimeUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.network.Player;
import com.locus.game.network.ShipState;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.tools.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Divya Mamgai on 10/2/2016.
 * Lobby Host Screen
 */

public class LobbyScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    public enum Type {
        Host,
        Client
    }

    public enum State {
        Starting,
        Started,
        Searching,
        Connecting,
        Connected,
        Failed
    }

    private class PlayerData {

        private Sprite shipSprite;
        private Text playerReadyText, playerNumberText;

        PlayerData(Sprite shipSprite, Text playerReadyText, Text playerNumberText) {
            this.shipSprite = shipSprite;
            this.playerNumberText = playerNumberText;
            this.playerReadyText = playerReadyText;
        }

    }

    private float backgroundMovementAngleRad;
    private float startingFontHalfWidth, failedFontHalfWidth, searchingFontHalfWidth,
            connectingFontHalfWidth;

    private ProjectLocus projectLocus;
    private SelectModeScreen selectModeScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;

    public MultiPlayerPlayScreen multiPlayerPlayScreen;
    public Level.Property levelProperty;
    public HashMap<Integer, Player> playerMap;
    public HashMap<Integer, ShipState> shipStateMap;

    private Type type;
    public State state = State.Starting;
    private ArrayList<PlayerData> playerDataList;

    private Text clientLobbyText, hostLobbyText, readyText, timerText;
    private static final int ROW_PADDING = 50, COLUMN_PADDING = 50, SHIP_PADDING = 34,
            MARGIN_TOP = 80;

    private Vector3 readyBBMinimum, readyBBMaximum;
    private BoundingBox readyBB;

    public boolean initializePlayScreen;
    private boolean isInitializedPlayScreen;
    public boolean isLobbyToBeUpdated;
    public boolean isGameToBeStarted;
    public float gameStartTime;
    private long previousTime;
    public boolean isGameStarted;
    public boolean isShipStateToBeUpdated;

    LobbyScreen(ProjectLocus projectLocus, SelectModeScreen selectModeScreen,
                LobbyScreen.Type type) {

        this.projectLocus = projectLocus;
        this.selectModeScreen = selectModeScreen;
        this.type = type;

        backgroundMovementAngleRad = selectModeScreen.backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
        foregroundCamera.setToOrtho(false, ProjectLocus.screenCameraHalfWidth,
                ProjectLocus.screenCameraHalfHeight);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);
        backgroundCamera.setToOrtho(false, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);
        backgroundCamera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(projectLocus.tiledMapList.get(0),
                ProjectLocus.TILED_MAP_SCALE);

        GlyphLayout glyphLayout = new GlyphLayout();
        glyphLayout.setText(projectLocus.font32, "Starting...");
        startingFontHalfWidth = glyphLayout.width / 2f;
        glyphLayout.setText(projectLocus.font32, "Searching...");
        searchingFontHalfWidth = glyphLayout.width / 2f;
        glyphLayout.setText(projectLocus.font32, "Connecting...");
        connectingFontHalfWidth = glyphLayout.width / 2f;
        glyphLayout.setText(projectLocus.font32, "Failed");
        failedFontHalfWidth = glyphLayout.width / 2f;

        readyBBMinimum = new Vector3(0, 0, 0);
        readyBBMaximum = new Vector3(0, 0, 0);
        readyBB = new BoundingBox();

        initializePlayScreen = false;
        isLobbyToBeUpdated = false;
        isGameToBeStarted = false;
        isGameStarted = false;
        isShipStateToBeUpdated = false;
        previousTime = 0;

        clientLobbyText = new Text(projectLocus.font32, "Client Lobby");
        hostLobbyText = new Text(projectLocus.font32, "Host Lobby");
        readyText = new Text(projectLocus.font32, "READY");
        timerText = new Text(projectLocus.font32, "10");
        playerDataList = new ArrayList<PlayerData>();

        switch (type) {
            case Host:

                ArrayList<Moon.Property> moonPropertyList = new ArrayList<Moon.Property>();
                moonPropertyList.add(new Moon.Property(Moon.Type.Organic, 200f, 0f));
                moonPropertyList.add(new Moon.Property(Moon.Type.DarkIce, 300f, MathUtils.PI));
                moonPropertyList.add(new Moon.Property(Moon.Type.Iron, 400f, ProjectLocus.PI_BY_TWO));

                levelProperty = new Level.Property(Planet.Type.Gas, moonPropertyList, 1);
                multiPlayerPlayScreen = new MultiPlayerPlayScreen(projectLocus, this);
                isInitializedPlayScreen = true;

                gameStartTime = 10f;

                state = State.Starting;
                projectLocus.gameServer.initializeMap();
                projectLocus.gameServer.start(this);

                break;
            case Client:

                isInitializedPlayScreen = false;

                state = State.Searching;
                projectLocus.gameClient.start(this);

                break;
        }

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        int row, col;
        float colWidth = (ProjectLocus.screenCameraWidth - (5 * COLUMN_PADDING)) / 4,
                rowHeight = ((ProjectLocus.screenCameraHeight - 80) - (3 * ROW_PADDING)) / 2;

        PlayerData playerData;
        for (int i = 0; i < playerDataList.size(); i++) {

            row = i / 4;
            col = i % 4;

            playerData = playerDataList.get(i);

            playerData.shipSprite.setPosition(COLUMN_PADDING + (col * (colWidth + COLUMN_PADDING))
                            + ((colWidth - playerData.shipSprite.getWidth()) / 2),
                    ROW_PADDING + (((row + 1) % 2) * (rowHeight + ROW_PADDING)) +
                            ((rowHeight - playerData.shipSprite.getHeight()) / 2));

            playerData.playerNumberText.setPosition(playerData.shipSprite.getX() +
                            ((playerData.shipSprite.getWidth() / 2)
                                    - playerData.playerNumberText.getHalfWidth()),
                    playerData.shipSprite.getY() + playerData.shipSprite.getHeight() + SHIP_PADDING);

            playerData.playerReadyText.setPosition(playerData.shipSprite.getX() +
                            ((playerData.shipSprite.getWidth() / 2)
                                    - playerData.playerReadyText.getHalfWidth()),
                    playerData.shipSprite.getY() - SHIP_PADDING / 2);

        }

        readyBBMinimum.set(ProjectLocus.screenCameraWidth - COLUMN_PADDING
                - readyText.getWidth() - 20, ProjectLocus.screenCameraHeight - MARGIN_TOP +
                ROW_PADDING - readyText.getHalfHeight() - 20, 0);
        readyBBMaximum.set(readyBBMinimum.x + readyText.getWidth() + 20,
                readyBBMinimum.y + readyText.getHeight() + 20, 0);
        readyBB.set(readyBBMinimum, readyBBMaximum);

        readyText.setPosition(
                ProjectLocus.screenCameraWidth - COLUMN_PADDING - readyText.getWidth(),
                ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING -
                        readyText.getHalfHeight());

        timerText.setPosition(ProjectLocus.screenCameraHalfWidth - timerText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight - timerText.getHalfWidth());

    }

    private void updateLobby() {

        if (isLobbyToBeUpdated) {

            Player player;
            playerDataList.clear();
            int i = 1;
            for (Integer connectionID : playerMap.keySet()) {
                player = playerMap.get(connectionID);
                Gdx.app.log("Player Connection ID", String.valueOf(connectionID));
                Gdx.app.log("Player Type", player.property.type.toString());
                Text playerNumberText = new Text(projectLocus.font24,
                        String.format(Locale.ENGLISH, "%02d", i++)),
                        playerReadyText = new Text(projectLocus.font24,
                                (player.isReady ? "Ready" : "Not Ready"));
                Sprite shipSprite =
                        projectLocus.shipTextureAtlas.createSprite(player.property.type.toString());
                shipSprite.setColor(player.property.color);
                playerDataList.add(new PlayerData(shipSprite, playerReadyText, playerNumberText));
            }
            positionUI();

            isLobbyToBeUpdated = false;
        }

    }

    private void drawLobby(SpriteBatch spriteBatch) {

        updateLobby();

        if (type == Type.Host) {
            hostLobbyText.draw(spriteBatch, COLUMN_PADDING,
                    ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING
                            - hostLobbyText.getHalfHeight());
        } else {
            clientLobbyText.draw(spriteBatch, COLUMN_PADDING,
                    ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING
                            - clientLobbyText.getHalfHeight());
        }
        for (PlayerData playerData : playerDataList) {
            playerData.playerNumberText.draw(spriteBatch);
            playerData.shipSprite.draw(spriteBatch);
            playerData.playerReadyText.draw(spriteBatch);
        }
        readyText.draw(projectLocus.spriteBatch);

        if (isGameToBeStarted && !isGameStarted) {

            if (TimeUtils.timeSinceMillis(previousTime) >= 1000) {

                if (gameStartTime <= 1) {
                    Gdx.app.log("Over Time", String.valueOf(gameStartTime));
                    projectLocus.setScreen(multiPlayerPlayScreen);
                    isGameStarted = true;
                    isGameToBeStarted = false;
                }

                previousTime = TimeUtils.millis();
                Gdx.app.log("Start Time", String.valueOf(gameStartTime));
                gameStartTime -= 1f;
                timerText.setText(String.format(Locale.ENGLISH, "%02d",
                        MathUtils.ceil(gameStartTime)));

            }

            timerText.draw(spriteBatch);

        }

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

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

        switch (type) {
            case Host:
                switch (state) {
                    case Started:
                        drawLobby(projectLocus.spriteBatch);
                        break;
                    case Starting:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Starting...",
                                ProjectLocus.screenCameraHalfWidth - startingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Failed:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Failed",
                                ProjectLocus.screenCameraHalfWidth - failedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                }
                break;
            case Client:
                switch (state) {
                    case Connected:
                        drawLobby(projectLocus.spriteBatch);
                        // Cannot be called from the GameClient cause that is not a Screen and is
                        // on a separate thread.
                        if (initializePlayScreen &&
                                !isInitializedPlayScreen) {
                            multiPlayerPlayScreen = new MultiPlayerPlayScreen(projectLocus, this);
                            isInitializedPlayScreen = true;
                            initializePlayScreen = false;
                        }
                        break;
                    case Searching:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Searching...",
                                ProjectLocus.screenCameraHalfWidth - searchingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Connecting:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Connecting...",
                                ProjectLocus.screenCameraHalfWidth - connectingFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                    case Failed:
                        projectLocus.font32.draw(projectLocus.spriteBatch, "Failed",
                                ProjectLocus.screenCameraHalfWidth - failedFontHalfWidth,
                                ProjectLocus.screenCameraHalfHeight + 16f);
                        break;
                }
                break;
        }

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
            case Input.Keys.BACK:
            case Input.Keys.ESCAPE:
            case Input.Keys.BACKSPACE:
                switch (type) {
                    case Host:
                        projectLocus.gameServer.stop();
                        break;
                    case Client:
                        projectLocus.gameClient.stop();
                        break;
                }
                selectModeScreen.backgroundMovementAngleRad = backgroundMovementAngleRad;
                projectLocus.setScreen(selectModeScreen);
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
        if (readyBB.contains(touchPosition)) {
            projectLocus.gameClient.ready();
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
