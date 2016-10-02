package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Multi Player Select Screen
 */

class SelectScreenMode implements Screen, InputProcessor, GestureDetector.GestureListener {

    private static final float CAMERA_MOVEMENT_SPEED = 0.05f;
    private static final float CAMERA_MOVEMENT_RADIUS = 512f;

    private float backgroundMovementAngleRad;
    private float menuStartPositionY;

    private class MenuOption {

        private String text;
        private float halfWidth, x, y, dy;
        private BitmapFont font, fontSelected;
        private GlyphLayout glyphLayout;

        boolean isSelected;

        MenuOption(BitmapFont font, BitmapFont fontSelected, GlyphLayout glyphLayout,
                   String text, float dy) {

            this.font = font;
            this.fontSelected = fontSelected;
            this.glyphLayout = glyphLayout;
            this.dy = dy;

            isSelected = false;
            setText(text);
            position();

        }

        void setText(String text) {
            this.text = text;
            glyphLayout.setText(font, text);
            halfWidth = glyphLayout.width / 2f;
        }

        public void draw(SpriteBatch spriteBatch) {
            if (isSelected) {
                fontSelected.draw(spriteBatch, text, x, y);
            } else {
                font.draw(spriteBatch, text, x, y);
            }
        }

        public void position() {
            x = ProjectLocus.screenCameraHalfWidth - halfWidth;
            y = menuStartPositionY + dy;
        }

    }

    private ProjectLocus projectLocus;
    private SelectScreenPlayer selectScreenPlayer;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private Sprite logo;
    private ArrayList<MenuOption> menuOptionList;
    private int selectedMenuOption;
    private InputMultiplexer inputMultiplexer;

    SelectScreenMode(ProjectLocus projectLocus, SelectScreenPlayer selectScreenPlayer) {

        this.projectLocus = projectLocus;
        this.selectScreenPlayer = selectScreenPlayer;
        backgroundMovementAngleRad = selectScreenPlayer.backgroundMovementAngleRad;

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

        logo = projectLocus.uiTextureAtlas.createSprite("logo");
        logo.setSize(366, 128);

        menuOptionList = new ArrayList<MenuOption>();
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                projectLocus.glyphLayout, "Host", 0));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                projectLocus.glyphLayout, "Join", -48f));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                projectLocus.glyphLayout, "Practice", -96f));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                projectLocus.glyphLayout, "Back", -144f));

        selectedMenuOption = 0;
        menuOptionList.get(selectedMenuOption).isSelected = true;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        logo.setPosition(ProjectLocus.screenCameraHalfWidth - logo.getWidth() / 2,
                ProjectLocus.screenCameraHeight - logo.getHeight() - 24f);

        menuStartPositionY = ProjectLocus.screenCameraHeight - logo.getHeight() - 96f;

        for (MenuOption menuOption : menuOptionList) {
            menuOption.position();
        }

    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        backgroundMovementAngleRad += delta * CAMERA_MOVEMENT_SPEED;
        backgroundCamera.position.set(
                ProjectLocus.WORLD_HALF_WIDTH +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.cos(backgroundMovementAngleRad),
                ProjectLocus.WORLD_HALF_HEIGHT +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        projectLocus.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        projectLocus.spriteBatch.begin();

        logo.draw(projectLocus.spriteBatch);

        for (MenuOption menuOption : menuOptionList) {
            menuOption.draw(projectLocus.spriteBatch);
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

    private void nextMenuOption() {
        menuOptionList.get(selectedMenuOption).isSelected = false;
        selectedMenuOption++;
        if (selectedMenuOption == menuOptionList.size()) {
            selectedMenuOption = 0;
        }
        menuOptionList.get(selectedMenuOption).isSelected = true;
    }

    private void previousMenuOption() {
        menuOptionList.get(selectedMenuOption).isSelected = false;
        selectedMenuOption--;
        if (selectedMenuOption < 0) {
            selectedMenuOption = menuOptionList.size() - 1;
        }
        menuOptionList.get(selectedMenuOption).isSelected = true;
    }

    private void submit() {
        switch (selectedMenuOption) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                projectLocus.setScreen(new PlayScreenPractice(projectLocus, this));
                break;
            case 3:
                selectScreenPlayer.backgroundMovementAngleRad = backgroundMovementAngleRad;
                projectLocus.setScreen(selectScreenPlayer);
                break;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                previousMenuOption();
                break;
            case Input.Keys.S:
                nextMenuOption();
                break;
            case Input.Keys.ENTER:
                submit();
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
        if (count == 2) {
            submit();
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        float flingAngle = (float) Math.atan2(velocityY, velocityX) * MathUtils.radiansToDegrees;
        if (flingAngle <= -70 && flingAngle >= -110) {
            previousMenuOption();
        } else if (flingAngle >= 70 && flingAngle <= 110) {
            nextMenuOption();
        }
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
