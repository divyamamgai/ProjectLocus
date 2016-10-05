package com.locus.game.tools;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.locus.game.ProjectLocus;

import java.util.ArrayList;
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
        int playerNumber, connectionID;
        long playerScore;
        boolean isAlive;
        Text playerScoreText, playerNumberText;

        PlayerHudData(int playerNumber, int playerScore, boolean isAlive, int connectionID) {
            this.playerNumber = playerNumber;
            this.playerScore = playerScore;
            this.isAlive = isAlive;
            this.connectionID = connectionID;
            this.playerScoreText = new Text(projectLocus.font24,
                    String.format(Locale.ENGLISH, "%04d", this.playerScore));
            this.playerNumberText = new Text(projectLocus.font24,
                    String.format(Locale.ENGLISH, "%d", this.playerNumber));
        }
    }

    private ArrayList<PlayerHudData> playerHudDataList;
    private int playerCount, timeMinutes, timeSeconds;
    private float timeChangeCount;
    private long score;

    private Text scoreCountText, timeMinuteText, timeSecondText;
    private ProjectLocus projectLocus;
    private OrthographicCamera foregroundCamera;

    public Hud(ProjectLocus projectLocus, Type type, OrthographicCamera foregroundCamera) {
        playerCount = 8;
        this.projectLocus = projectLocus;
        this.type = type;
        this.foregroundCamera = foregroundCamera;

        score = timeMinutes = timeSeconds = 0;
        timeChangeCount = 0f;
        playerHudDataList = new ArrayList<PlayerHudData>();
        for (int i = 0; i < playerCount; i++) {
            playerHudDataList.add(new PlayerHudData(i + 1, 0, false, -1));
        }
        if (type == Type.DeathMatch) {
            timeMinutes = 4;
            timeSeconds = 0;
        }

        timeChangeCount = 0f;
        scoreCountText = new Text(projectLocus.font32,
                String.format(Locale.ENGLISH, "%04d", score), COLUMN_PADDING, ROW_PADDING);
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
                columnWidth = ((ProjectLocus.screenCameraWidth) -
                        ((playerCount + 1) * COLUMN_PADDING)) / playerCount;
        int i = 0;
        for (PlayerHudData playerHudData : playerHudDataList) {
            playerHudData.playerNumberText.setPosition(COLUMN_PADDING +
                            (i * (columnWidth + COLUMN_PADDING)) +
                            ((columnWidth / 2) - playerHudData.playerNumberText.getHalfWidth()),
                    ProjectLocus.screenCameraHeight - ROW_PADDING - (ROW_PADDING / 4) - rowHeight);
            playerHudData.playerScoreText.setPosition(COLUMN_PADDING +
                            (i * (columnWidth + COLUMN_PADDING)) +
                            ((columnWidth / 2) - playerHudData.playerScoreText.getHalfWidth()),
                    ProjectLocus.screenCameraHeight - (2 * (ROW_PADDING + rowHeight))
                            - (ROW_PADDING / 2));
            i++;
        }
    }

    public void updateScore(long score) {
        this.score += score;
        scoreCountText.setText(String.format(Locale.ENGLISH, "%04d", this.score));
    }

    public void updateTimer(float delta) {
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

    public void draw(SpriteBatch spriteBatch) {
        spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        spriteBatch.begin();
        for (PlayerHudData playerHudData : playerHudDataList) {
            playerHudData.playerNumberText.draw(spriteBatch);
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
