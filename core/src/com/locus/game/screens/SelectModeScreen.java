package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.locus.game.ProjectLocus;
import com.locus.game.tools.Text;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Multi Player Select Screen
 */

class SelectModeScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

    private class MenuOption {

        private Text text, textSelected;
        private Vector2 textPosition, textSelectedPosition;
        private float dy;

        boolean isSelected;

        MenuOption(BitmapFont font, BitmapFont fontSelected, String text, float dy) {

            this.dy = dy;
            this.text = new Text(font, text);
            this.textSelected = new Text(fontSelected, text);

            textPosition = new Vector2(0, 0);
            textSelectedPosition = new Vector2(0, 0);

            isSelected = false;
            position();

        }

        public void draw(SpriteBatch spriteBatch) {
            if (isSelected) {
                textSelected.draw(spriteBatch, textSelectedPosition.x, textSelectedPosition.y);
            } else {
                text.draw(spriteBatch, textPosition.x, textPosition.y);
            }
        }

        void position() {
            textPosition.set(
                    ProjectLocus.screenCameraHalfWidth - text.getHalfWidth(),
                    menuStartPositionY + dy);
            textSelectedPosition.set(
                    ProjectLocus.screenCameraHalfWidth - textSelected.getHalfWidth(),
                    menuStartPositionY + dy);
        }

    }

    float backgroundMovementAngleRad;
    private float menuStartPositionY;

    private ProjectLocus projectLocus;
    private SelectPlayerScreen selectPlayerScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private Sprite logo;
    private ArrayList<MenuOption> menuOptionList;
    private int selectedMenuOption;
    private InputMultiplexer inputMultiplexer;

    SelectModeScreen(ProjectLocus projectLocus, SelectPlayerScreen selectPlayerScreen) {

        this.projectLocus = projectLocus;
        this.selectPlayerScreen = selectPlayerScreen;
        backgroundMovementAngleRad = selectPlayerScreen.backgroundMovementAngleRad;

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
                "Host", 0));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                "Join", -48f));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                "Practice", -96f));
        menuOptionList.add(new MenuOption(projectLocus.font32, projectLocus.font32Selected,
                "Back", -144f));

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
                projectLocus.setScreen(new LobbyScreen(projectLocus, this, LobbyScreen.Type.Host));
                break;
            case 1:
                projectLocus.setScreen(new LobbyScreen(projectLocus, this,
                        LobbyScreen.Type.Client));
                break;
            case 2:
                PracticePlayScreen practicePlayScreen = new PracticePlayScreen(projectLocus, this);
                projectLocus.setScreen(practicePlayScreen);
                break;
            case 3:
                selectPlayerScreen.backgroundMovementAngleRad = backgroundMovementAngleRad;
                projectLocus.setScreen(selectPlayerScreen);
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
            case Input.Keys.BACK:
                // Go back to previous Screen.
                selectedMenuOption = 3;
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
