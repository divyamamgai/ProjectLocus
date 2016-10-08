package com.locus.game.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.locus.game.ProjectLocus;
import com.locus.game.network.ShipState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Rohit Yadav on 02-Oct-16.
 * Hud
 */

public class Hud {

    public enum Type {
        Survival,
        DeathMatch
    }

    private Type type;

    private static final int ROW_PADDING = 48, COLUMN_PADDING = 32, HUD_HEIGHT = 80;

    private class PlayerHudData {
        float health;
        boolean isPlayer;
        short playerScore;
        Text playerScoreText, playerIDText;

        PlayerHudData(int ID, short playerScore, float health, boolean isPlayer) {
            this.playerScore = playerScore;
            this.isPlayer = isPlayer;
            this.health = health;

            this.playerScoreText = new Text(projectLocus.font24,
                    String.format(Locale.ENGLISH, "%04d", this.playerScore));
            this.playerIDText = new Text(projectLocus.font24,
                    String.format(Locale.ENGLISH, "%04d", ID));
        }
    }

    private HashMap<Short, PlayerHudData> playerHudDataMap;
    private int playerCount, timeMinutes, timeSeconds;
    private float timeChangeCount;
    private long score;
    private static int count;

    private Text scoreCountText, timeMinuteText, timeSecondText;
    private ProjectLocus projectLocus;
    private OrthographicCamera foregroundCamera;

    public Hud(ProjectLocus projectLocus, Type type, OrthographicCamera foregroundCamera) {
        playerCount = 0;
        count = 0;
        this.projectLocus = projectLocus;
        this.type = type;
        this.foregroundCamera = foregroundCamera;

        score = timeMinutes = timeSeconds = 0;
        timeChangeCount = 0f;
        playerHudDataMap = new HashMap<Short, PlayerHudData>();
        if (type == Type.DeathMatch) {
            timeMinutes = 4;
            timeSeconds = 0;
        }

        timeChangeCount = 0f;
        scoreCountText = new Text(projectLocus.font32,
                String.format(Locale.ENGLISH, "%04d", score));
        scoreCountText.setPosition(COLUMN_PADDING, ROW_PADDING);
        if (type == Type.DeathMatch) {
            timeSecondText = new Text(projectLocus.font32,
                    String.format(Locale.ENGLISH, " : %02d", timeSeconds));
            timeSecondText.setPosition(ProjectLocus.screenCameraWidth - COLUMN_PADDING
                    - timeSecondText.getWidth(), ROW_PADDING);
            timeMinuteText = new Text(projectLocus.font32,
                    String.format(Locale.ENGLISH, "%02d", timeMinutes));
            timeMinuteText.setPosition(ProjectLocus.screenCameraWidth - timeSecondText.getWidth()
                    - timeMinuteText.getWidth() - COLUMN_PADDING, ROW_PADDING);
        }

    }

    public void positionUI() {
        int rowHeight = ((HUD_HEIGHT) - (3 * ROW_PADDING)) / 2,
                columnWidth = (playerCount <= 0) ? 0 : ((ProjectLocus.screenCameraWidth) -
                        ((playerCount + 1) * COLUMN_PADDING)) / playerCount;
        int i = 0;
        for (PlayerHudData playerHudData : playerHudDataMap.values()) {
            playerHudData.playerIDText.setPosition(COLUMN_PADDING +
                            (i * (columnWidth + COLUMN_PADDING)) +
                            ((columnWidth / 2) - playerHudData.playerIDText.getHalfWidth()),
                    ProjectLocus.screenCameraHeight - ROW_PADDING - (ROW_PADDING / 4) - rowHeight);
            playerHudData.playerScoreText.setPosition(COLUMN_PADDING +
                            (i * (columnWidth + COLUMN_PADDING)) +
                            ((columnWidth / 2) - playerHudData.playerScoreText.getHalfWidth()),
                    ProjectLocus.screenCameraHeight - (2 * (ROW_PADDING + rowHeight))
                            - (ROW_PADDING / 2));
            i++;
        }
    }

    private void updateScore(short score) {
        this.score = score;
        scoreCountText.setText(String.format(Locale.ENGLISH, "%04d", this.score));
    }

    public void addPlayerData(ShipState shipState, boolean isPlayer) {
        ++count;
        playerHudDataMap.put(shipState.ID,
                new PlayerHudData(count, shipState.score, shipState.health, isPlayer));
    }

    public synchronized void update(ArrayList<ShipState> shipStateList) {
        playerCount = shipStateList.size();
        for (ShipState shipState : shipStateList) {
            if (playerHudDataMap.containsKey(shipState.ID)) {
                playerHudDataMap.get(shipState.ID).playerScore = shipState.score;
                playerHudDataMap.get(shipState.ID).health = shipState.health;
                playerHudDataMap.get(shipState.ID).playerScoreText.setTextFast(
                        String.format(Locale.ENGLISH, "%04d", shipState.score));
                if (playerHudDataMap.get(shipState.ID).isPlayer) {
                    updateScore(shipState.score);
                }
            }
        }
        positionUI();
    }

    private void updateTimer(float delta) {
        if (type == Type.DeathMatch) {
            timeChangeCount += delta;
            if (timeChangeCount >= 1) {
                timeSeconds--;
                if (timeSeconds < 0) {
                    timeMinutes--;
                    timeSeconds = 59;
                }
                timeMinuteText.setText(String.format(Locale.ENGLISH, "%02d", timeMinutes));
                timeSecondText.setText(String.format(Locale.ENGLISH, " : %02d", timeSeconds));
                timeChangeCount = 0f;
            }
        }
    }

    public synchronized void draw(SpriteBatch spriteBatch) {
        spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        spriteBatch.begin();
        for (PlayerHudData playerHudData : playerHudDataMap.values()) {
            if (playerHudData.health <= 0) {
                playerHudData.playerIDText.setFontFast(projectLocus.font24Selected);
                playerHudData.playerScoreText.setFontFast(projectLocus.font24Selected);
            }
            playerHudData.playerIDText.draw(spriteBatch);
            playerHudData.playerScoreText.draw(spriteBatch);
        }
        scoreCountText.draw(spriteBatch);
        if (type == Type.DeathMatch) {
            timeMinuteText.draw(spriteBatch);
            timeSecondText.draw(spriteBatch);
        }
        spriteBatch.end();
    }
}
