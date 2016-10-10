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
import com.badlogic.gdx.utils.TimeUtils;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.network.GameClient;
import com.locus.game.network.GameServer;
import com.locus.game.network.Player;
import com.locus.game.tools.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by Divya Mamgai on 10/2/2016.
 * Lobby Host Screen
 */

public class LobbyScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private static final int ROW_PADDING = 50, COLUMN_PADDING = 50, SHIP_PADDING = 34,
            MARGIN_TOP = 80;

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

    public class PlayerDisplay {

        private Sprite shipSprite;
        private Text readyText, numberText;

        PlayerDisplay(Sprite shipSprite, Text readyText, Text numberText) {
            this.shipSprite = shipSprite;
            this.numberText = numberText;
            this.readyText = readyText;
        }

        Sprite getShipSprite() {
            return shipSprite;
        }

    }

    private float backgroundMovementAngleRad;
    private ProjectLocus projectLocus;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private Type type;
    private State state = State.Starting;
    private LinkedHashMap<Integer, Player> playerMap;
    ArrayList<PlayerDisplay> playerDisplayList;
    private MultiPlayerPlayScreen multiPlayerPlayScreen;
    private MultiPlayerPlayScreenClient multiPlayerPlayScreenClient;
    private Level.Property levelProperty;
    private Text startingText, searchingText, connectingText, failedText, clientLobbyText,
            hostLobbyText, readyText, timerText;
    private TextureRegion transparentBackgroundTexture;

    private boolean createPlayScreen;
    private boolean isPlayScreenCreated;
    private boolean isPlayerMapToBeUpdated;
    private boolean isReady;
    private boolean isGameToBeStarted;
    private boolean isGameStarted;
    private boolean isReadyToBeChanged;
    private float startGameIn;
    private long previousTime;
    private float searchingSecond;
    private int searchingTickCount;
    private String searchingString;

    public Level.Property getLevelProperty() {
        return levelProperty;
    }

    public void setLevelProperty(Level.Property levelProperty) {
        if (!createPlayScreen) {
            this.levelProperty = levelProperty;
            createPlayScreen = true;
        }
    }

    public Level getLevel() {
        return multiPlayerPlayScreen.level;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean checkState(State state) {
        return this.state == state;
    }

    public void setPlayerMap(LinkedHashMap<Integer, Player> playerMap) {
        if (!isPlayerMapToBeUpdated) {
            this.playerMap = playerMap;
            isPlayerMapToBeUpdated = true;
        }
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public void startGame(float in) {
        if (!isGameToBeStarted) {
            this.startGameIn = in;
            isGameToBeStarted = true;
            projectLocus.countdownMusic.play();
        }
    }

    public void allReady() {

    }

    LobbyScreen(ProjectLocus projectLocus, LobbyScreen.Type type) {

        this.projectLocus = projectLocus;
        this.type = type;

        backgroundMovementAngleRad = projectLocus.mainMenuScreen.backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);

        backgroundCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        tiledMapRenderer = new OrthogonalTiledMapRenderer(
                projectLocus.tiledMapList.get(MathUtils.random(0, 7)),
                ProjectLocus.TILED_MAP_SCALE);

        transparentBackgroundTexture = projectLocus.uiTextureAtlas
                .findRegion("transparentBackground");

        createPlayScreen = isPlayScreenCreated = isPlayerMapToBeUpdated = isGameToBeStarted =
                isGameStarted = isReady = false;

        // Set at extreme condition so that it can be initialized correctly on its own.
        searchingSecond = 1f;
        searchingTickCount = 4;
        searchingString = "Searching";

        previousTime = 0;

        playerDisplayList = new ArrayList<PlayerDisplay>();

        startingText = new Text(projectLocus.font32, "Starting...");
        searchingText = new Text(projectLocus.font32, "Searching...");
        connectingText = new Text(projectLocus.font32, "Connecting...");
        failedText = new Text(projectLocus.font32, "Failed");
        clientLobbyText = new Text(projectLocus.font32, "Client Lobby");
        hostLobbyText = new Text(projectLocus.font32, "Host Lobby");
        readyText = new Text(projectLocus.font32Red, "READY");
        timerText = new Text(projectLocus.font72, "10");

        switch (type) {
            case Host:

                projectLocus.gameServer = new GameServer(projectLocus);

                levelProperty = Level.Property.generateRandom();

                multiPlayerPlayScreen = new MultiPlayerPlayScreen(projectLocus, this);

                isPlayScreenCreated = true;

                // For Host it is always the same as the Count Down.
                startGameIn = ProjectLocus.GAME_COUNT_DOWN;

                state = State.Starting;
                projectLocus.gameServer.start(this);

                break;
            case Client:

                state = State.Searching;
                projectLocus.gameClient = new GameClient(projectLocus);
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

        PlayerDisplay playerDisplay;
        for (int i = 0; i < playerDisplayList.size(); i++) {

            row = i / 4;
            col = i % 4;

            playerDisplay = playerDisplayList.get(i);

            playerDisplay.shipSprite.setPosition(COLUMN_PADDING + (col * (colWidth + COLUMN_PADDING))
                            + ((colWidth - playerDisplay.shipSprite.getWidth()) / 2),
                    ROW_PADDING + (((row + 1) % 2) * (rowHeight + ROW_PADDING)) +
                            ((rowHeight - playerDisplay.shipSprite.getHeight()) / 2));

            playerDisplay.numberText.setPosition(playerDisplay.shipSprite.getX() +
                            ((playerDisplay.shipSprite.getWidth() / 2)
                                    - playerDisplay.numberText.getHalfWidth()),
                    playerDisplay.shipSprite.getY() + playerDisplay.shipSprite.getHeight() + SHIP_PADDING);

            playerDisplay.readyText.setPosition(playerDisplay.shipSprite.getX() +
                            ((playerDisplay.shipSprite.getWidth() / 2)
                                    - playerDisplay.readyText.getHalfWidth()),
                    playerDisplay.shipSprite.getY() - SHIP_PADDING / 2);

        }

        readyText.setPosition(
                ProjectLocus.screenCameraWidth - COLUMN_PADDING - readyText.getWidth(),
                ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING -
                        readyText.getHalfHeight());

        startingText.setPosition(ProjectLocus.screenCameraHalfWidth - startingText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + startingText.getHalfHeight());

        searchingText.setPosition(ProjectLocus.screenCameraHalfWidth - searchingText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + searchingText.getHalfHeight());

        connectingText.setPosition(ProjectLocus.screenCameraHalfWidth - connectingText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + connectingText.getHalfHeight());

        failedText.setPosition(ProjectLocus.screenCameraHalfWidth - failedText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + failedText.getHalfHeight());

        timerText.setPosition(ProjectLocus.screenCameraHalfWidth - timerText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + timerText.getHalfHeight());

    }

    private void updateLobby() {

        // Cannot be called from the GameClient cause that is not a Screen and is
        // on a separate thread.
        if (createPlayScreen && !isPlayScreenCreated) {
            multiPlayerPlayScreenClient = new MultiPlayerPlayScreenClient(projectLocus, this);
            projectLocus.gameClient.setLevel(multiPlayerPlayScreenClient.level);
            isPlayScreenCreated = true;
            createPlayScreen = false;
        }

        if (isPlayerMapToBeUpdated) {

            playerDisplayList.clear();
            int i = 1;

            for (Player player : playerMap.values()) {
                Sprite shipSprite = projectLocus.shipTextureAtlas.createSprite(
                        player.property.type.toString());
                shipSprite.setColor(player.property.color);
                playerDisplayList.add(new PlayerDisplay(
                        shipSprite,
                        new Text(projectLocus.font24, (player.isReady ? "Ready" : "Not Ready")),
                        new Text(projectLocus.font24, String.format(Locale.ENGLISH, "%01d", i++))
                ));
            }

            positionUI();

            isPlayerMapToBeUpdated = false;

        }

        if (isGameToBeStarted && !isGameStarted) {

            if (previousTime == 0) {

                previousTime = TimeUtils.millis();
                timerText.setText(String.format(Locale.ENGLISH, "%02d",
                        MathUtils.ceil(startGameIn)));

            } else if (TimeUtils.timeSinceMillis(previousTime) >= 1000) {

                if (startGameIn <= 1) {

                    try {
                        projectLocus.lobbyScreenBackgroundMusic.setVolume(0f);
                        projectLocus.lobbyScreenBackgroundMusic.stop();
                    } catch (Exception e) {
                        Gdx.app.log("Sound Error", "Error - " + e.toString());
                    }

                    switch (type) {
                        case Host:
                            projectLocus.screenTransitionSound.play();
                            projectLocus.setScreen(multiPlayerPlayScreen);
                            break;
                        case Client:
                            projectLocus.screenTransitionSound.play();
                            projectLocus.setScreen(multiPlayerPlayScreenClient);
                            break;
                    }

                    isGameStarted = true;
                    isGameToBeStarted = false;

                }

                previousTime = TimeUtils.millis();

                startGameIn -= 1f;

                projectLocus.flingVerticalSound.play();

                timerText.setText(String.format(Locale.ENGLISH, "%02d",
                        MathUtils.ceil(startGameIn)));

            }

        }

    }

    private void drawLobby(SpriteBatch spriteBatch) {

        switch (type) {
            case Host:
                hostLobbyText.draw(spriteBatch, COLUMN_PADDING,
                        ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING
                                - hostLobbyText.getHalfHeight());
                break;
            case Client:
                clientLobbyText.draw(spriteBatch, COLUMN_PADDING,
                        ProjectLocus.screenCameraHeight - MARGIN_TOP + ROW_PADDING
                                - clientLobbyText.getHalfHeight());
                break;
        }

        for (PlayerDisplay playerDisplay : playerDisplayList) {
            playerDisplay.numberText.draw(spriteBatch);
            playerDisplay.shipSprite.draw(spriteBatch);
            playerDisplay.readyText.draw(spriteBatch);
        }

        readyText.draw(spriteBatch);

        if (isGameToBeStarted && !isGameStarted) {
            spriteBatch.draw(transparentBackgroundTexture, 0, 0,
                    ProjectLocus.screenCameraWidth, ProjectLocus.screenCameraHeight);
            timerText.draw(spriteBatch);
        }

    }

    private void toggleReady() {
        if (!isGameToBeStarted) {
            if (isReady) {
                projectLocus.screenTransitionSound.play();
                switch (type) {
                    case Host:
                        projectLocus.gameServer.sendReadyState(isReady = false);
                        break;
                    case Client:
                        projectLocus.gameClient.sendReadyState(isReady = false);
                        break;
                }
                readyText.setFont(projectLocus.font32Red);
            } else {
                projectLocus.flingHorizontalSound.play();
                switch (type) {
                    case Host:
                        projectLocus.gameServer.sendReadyState(isReady = true);
                        break;
                    case Client:
                        projectLocus.gameClient.sendReadyState(isReady = true);
                        break;
                }
                readyText.setFont(projectLocus.font32Green);
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
        try {
            if (!projectLocus.lobbyScreenBackgroundMusic.isPlaying()) {
                if (projectLocus.screenBackgroundMusic.isPlaying())
                    projectLocus.lobbyScreenBackgroundMusic.setVolume(0f);
                else {
                    projectLocus.lobbyScreenBackgroundMusic.setVolume(1f);
                }
                projectLocus.lobbyScreenBackgroundMusic.setLooping(true);
                projectLocus.lobbyScreenBackgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.log("Sound Error", "Error - " + e.toString());
        }
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        try {
            if (projectLocus.screenBackgroundMusic.isPlaying()) {
                if (projectLocus.screenBackgroundMusic.getVolume() > 0.05) {
                    projectLocus.screenBackgroundMusic.setVolume(
                            projectLocus.screenBackgroundMusic.getVolume() - delta
                    );
                } else {
                    projectLocus.screenBackgroundMusic.setVolume(0f);
                    projectLocus.lobbyScreenBackgroundMusic.setVolume(
                            projectLocus.lobbyScreenBackgroundMusic.getVolume() + delta);
                    if (projectLocus.lobbyScreenBackgroundMusic.getVolume() > 0.95f) {
                        projectLocus.lobbyScreenBackgroundMusic.setVolume(1f);
                        projectLocus.screenBackgroundMusic.stop();
                        projectLocus.lobbyScreenBackgroundMusic.play();
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.log("Sound Error", "Error - " + e.toString());
        }

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
                        updateLobby();
                        drawLobby(projectLocus.spriteBatch);
                        break;
                    case Starting:
                        startingText.draw(projectLocus.spriteBatch);
                        break;
                    case Failed:
                        failedText.draw(projectLocus.spriteBatch);
                        break;
                }
                break;
            case Client:
                switch (state) {
                    case Connected:
                        updateLobby();
                        drawLobby(projectLocus.spriteBatch);
                        break;
                    case Searching:
                        if (searchingSecond >= 1f) {
                            searchingSecond = delta;
                            searchingTickCount++;
                            if (searchingTickCount > 3) {
                                searchingString = "Searching";
                                searchingTickCount = 0;
                            } else {
                                searchingString += ".";
                            }
                            searchingText.setText(searchingString);
                        } else {
                            searchingSecond += delta;
                        }
                        searchingText.draw(projectLocus.spriteBatch);
                        break;
                    case Connecting:
                        connectingText.draw(projectLocus.spriteBatch);
                        break;
                    case Failed:
                        failedText.draw(projectLocus.spriteBatch);
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
        if (projectLocus.gameServer != null) {
            projectLocus.gameServer.stop();
        }
        if (projectLocus.gameClient != null) {
            projectLocus.gameClient.stop();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.ENTER:
                toggleReady();
                break;
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
                projectLocus.mainMenuScreen.backgroundMovementAngleRad = backgroundMovementAngleRad;
                projectLocus.screenTransitionSound.play();
                projectLocus.setScreen(projectLocus.mainMenuScreen);
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
        if (readyText.getTextBoundingBox().contains(touchPosition)) {
            isReadyToBeChanged = true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (isReadyToBeChanged) {
            toggleReady();
            isReadyToBeChanged = false;
        }
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
