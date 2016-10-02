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
import com.locus.game.Main;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 10/1/2016.
 * Multi Player Select Screen
 */

class MultiPlayerSelectScreen implements Screen, InputProcessor, GestureDetector.GestureListener {

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
            x = Main.cameraHalfWidth - halfWidth;
            y = menuStartPositionY + dy;
        }

    }

    private Main main;
    private PlayerSelectScreen playerSelectScreen;
    private OrthographicCamera foregroundCamera, backgroundCamera;
    private TiledMapRenderer tiledMapRenderer;
    private Sprite logo;
    private ArrayList<MenuOption> menuOptionList;
    private int selectedMenuOption;
    private InputMultiplexer inputMultiplexer;

    MultiPlayerSelectScreen(Main main, PlayerSelectScreen playerSelectScreen) {

        this.main = main;
        this.playerSelectScreen = playerSelectScreen;
        backgroundMovementAngleRad = playerSelectScreen.backgroundMovementAngleRad;

        foregroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        foregroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        foregroundCamera.update();

        backgroundCamera = new OrthographicCamera(Main.cameraWidth, Main.cameraHeight);
        backgroundCamera.setToOrtho(false, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);
        backgroundCamera.update();

        tiledMapRenderer = new OrthogonalTiledMapRenderer(main.tiledMapList.get(0),
                Main.TILED_MAP_SCALE);

        logo = main.uiTextureAtlas.createSprite("logo");
        logo.setSize(71.31f, 25f);

        menuOptionList = new ArrayList<MenuOption>();
        menuOptionList.add(new MenuOption(main.font1, main.font1Selected, main.glyphLayout,
                "Host", 0));
        menuOptionList.add(new MenuOption(main.font1, main.font1Selected, main.glyphLayout,
                "Join", -10f));
        menuOptionList.add(new MenuOption(main.font1, main.font1Selected, main.glyphLayout,
                "Back", -20f));

        selectedMenuOption = 0;
        menuOptionList.get(selectedMenuOption).isSelected = true;

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new GestureDetector(this));
        inputMultiplexer.addProcessor(this);

    }

    private void positionUI() {

        logo.setPosition(Main.cameraHalfWidth - logo.getWidth() / 2,
                Main.cameraHeight - logo.getHeight() - 10f);

        menuStartPositionY = Main.cameraHeight - logo.getHeight() - 28f;

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
                Main.HALF_WORLD_WIDTH +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.cos(backgroundMovementAngleRad),
                Main.HALF_WORLD_HEIGHT +
                        CAMERA_MOVEMENT_RADIUS * MathUtils.sin(backgroundMovementAngleRad), 0);
        backgroundCamera.update();

        tiledMapRenderer.setView(backgroundCamera);
        tiledMapRenderer.render();

        main.spriteBatch.setProjectionMatrix(foregroundCamera.combined);
        main.spriteBatch.begin();

        logo.draw(main.spriteBatch);

        for (MenuOption menuOption : menuOptionList) {
            menuOption.draw(main.spriteBatch);
        }

        main.spriteBatch.end();

    }

    @Override
    public void resize(int width, int height) {
        Main.resizeCamera(width, height);
        foregroundCamera.setToOrtho(false, Main.cameraWidth, Main.cameraHeight);
        backgroundCamera.setToOrtho(false, Main.cameraWidth, Main.cameraHeight);
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
                playerSelectScreen.backgroundMovementAngleRad = backgroundMovementAngleRad;
                main.setScreen(playerSelectScreen);
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
