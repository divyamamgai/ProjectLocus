package com.locus.game.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.locus.game.ProjectLocus;

/**
 * Created by Rohit Yadav on 02-Oct-16.
 * Hud
 */

public class Hud {
    private Stage stage;

    private int[] playersNumber, playersScore;
    private int score, playerCount, timeMinutes, timeSeconds;
    private float timeChangeCount;

    private Label scoreCountLabel, timeCountLabel, timeLabel;
    private Label[] playersScoreLabel;

    public enum Type {
        Survival,
        DeathMatch
    }

    private Type type;

    public Hud(SpriteBatch spriteBatch, Type type) {
        playerCount = 8;
        this.type = type;

        score = timeMinutes = timeSeconds = 0;
        timeChangeCount = 0f;
        playersNumber = new int[playerCount];
        playersScore = new int[playerCount];
        Label[] playersLabel = new Label[playerCount];
        playersScoreLabel = new Label[playerCount];
        for (int i = 0; i < playerCount; i++) {
            playersNumber[i] = i + 1;
            playersScore[i] = 0;
        }
        if (type == Type.DeathMatch) {
            timeMinutes = 4;
        }

        Viewport viewport = new FitViewport(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight, new OrthographicCamera());
        stage = new Stage(viewport, spriteBatch);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        Label scoreLabel = new Label("SCORE", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        scoreCountLabel = new Label(String.format("%04d", score),
                new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        for (int i = 0; i < playerCount; i++) {
            playersLabel[i] = new Label(String.format("%01d", playersNumber[i]),
                    new Label.LabelStyle(new BitmapFont(), Color.BLUE));
            playersScoreLabel[i] = new Label(String.format("%04d", playersScore[i]),
                    new Label.LabelStyle(new BitmapFont(), Color.GREEN));
        }
        if (type == Type.DeathMatch) {
            timeLabel = new Label("TIME", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
            timeCountLabel = new Label(String.format("%02d:%02d", timeMinutes, timeSeconds),
                    new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        table.add(scoreLabel).expandX().padTop(10);
        for (int i = 0; i < playerCount; i++) {
            table.add(playersLabel[i]).expandX().padTop(10);
        }
        if (type == Type.DeathMatch) {
            table.add(timeLabel).expandX().padTop(10);
        }
        table.row();
        table.add(scoreCountLabel).padTop(10);
        for (int i = 0; i < playerCount; i++) {
            table.add(playersScoreLabel[i]).padTop(10);
        }
        if (type == Type.DeathMatch) {
            table.add(timeCountLabel).padTop(10);
        }

        stage.addActor(table);
    }

    public void update(float deltaTime) {
        if (type == Type.DeathMatch) {
            timeChangeCount += deltaTime;
            if (timeChangeCount >= 1) {
                timeSeconds--;
                if (timeSeconds < 0) {
                    timeMinutes--;
                    timeSeconds = 59;
                }
                timeCountLabel.setText(String.format("%02d:%02d", timeMinutes, timeSeconds));
                timeChangeCount = 0f;
            }
        }
    }

    public void draw() {
        stage.draw();
    }
}
