package com.locus.game.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Rohit Yadav on 18-Sep-16.
 * InputController
 */
public class InputController implements InputProcessor {

    public interface InputCallback {

        void applyImpulse(float xImpulse, float yImpulse);

        void applyRotation(boolean isClockwise);

        void applyThrust(boolean isForward);

        void fireBullet();

    }

    private Sprite impulseControlSprite, rotationControlSprite, forwardThrustSprite, backwardThrustSprite;
    private OrthographicCamera camera;
    private InputCallback inputCallback;
    private Vector2 impulseControlOrigin, rotationControlOrigin;
    private boolean isThrusterController;
    private int forwardThrustStickSize;
    private int backwardThrustStickSize;
    private int translateControlSpriteSize;

    public InputController(InputCallback inputCallback, boolean isMirrored, boolean thrustController) {
        this.inputCallback = inputCallback;
        this.isThrusterController = thrustController;
        Texture stickTexture = new Texture(Gdx.files.internal("controller/stick.png"));

        camera = new OrthographicCamera(854, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        int padding = 15;

        if (isThrusterController) {
            forwardThrustSprite = new Sprite(stickTexture);
            forwardThrustStickSize = 100;
            forwardThrustSprite.setColor(Color.RED);
            forwardThrustSprite.setSize(forwardThrustStickSize, forwardThrustStickSize);
            forwardThrustSprite.setAlpha(0.3f);
            backwardThrustSprite = new Sprite(stickTexture);
            backwardThrustStickSize = 100;
            backwardThrustSprite.setColor(Color.GREEN);
            backwardThrustSprite.setSize(backwardThrustStickSize, backwardThrustStickSize);
            backwardThrustSprite.setAlpha(0.3f);
        } else {
            translateControlSpriteSize = 200;
            impulseControlSprite = new Sprite(stickTexture);
            impulseControlSprite.setSize(translateControlSpriteSize, translateControlSpriteSize);
            impulseControlSprite.setOrigin(impulseControlSprite.getWidth() / 2, impulseControlSprite.getHeight() / 2);
            impulseControlSprite.setAlpha(0.3f);
        }

        int rotationControlSpriteSize = 200;
        rotationControlSprite = new Sprite(stickTexture);
        rotationControlSprite.setSize(rotationControlSpriteSize, rotationControlSpriteSize);
        rotationControlSprite.setOrigin(rotationControlSprite.getWidth() / 2, rotationControlSprite.getHeight() / 2);
        rotationControlSprite.setAlpha(0.3f);

        if (isMirrored) {
            if (isThrusterController) {
                backwardThrustSprite.setPosition(camera.viewportWidth / 2 - backwardThrustStickSize - padding, -camera.viewportHeight / 2 + forwardThrustStickSize / 2 + padding);
                forwardThrustSprite.setPosition(camera.viewportWidth / 2 - forwardThrustStickSize - backwardThrustStickSize - padding, -camera.viewportHeight / 2 + padding);
            } else {
                impulseControlSprite.setPosition(camera.viewportWidth / 2 - translateControlSpriteSize - padding, -camera.viewportHeight / 2 + padding);
            }
            rotationControlSprite.setPosition(-camera.viewportWidth / 2 + padding, -camera.viewportHeight / 2 + padding);
        } else {
            if (isThrusterController) {
                backwardThrustSprite.setPosition(-camera.viewportWidth / 2 + padding, -camera.viewportHeight / 2 + forwardThrustStickSize / 2 + padding);
                forwardThrustSprite.setPosition(-camera.viewportWidth / 2 + backwardThrustStickSize + padding, -camera.viewportHeight / 2 + padding);
            } else {
                impulseControlSprite.setPosition(-camera.viewportWidth / 2 + padding, -camera.viewportHeight / 2 + padding);
            }
            rotationControlSprite.setPosition(camera.viewportWidth / 2 - rotationControlSpriteSize - padding, -camera.viewportHeight / 2 + padding);
        }

        if (!isThrusterController) {
            impulseControlOrigin = (new Vector2(impulseControlSprite.getX(), impulseControlSprite.getY())).add(impulseControlSprite.getOriginX(), impulseControlSprite.getOriginY());
        }
        rotationControlOrigin = (new Vector2(rotationControlSprite.getX(), rotationControlSprite.getY())).add(rotationControlSprite.getOriginX(), rotationControlSprite.getOriginY());
    }

    public void draw(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if (isThrusterController) {
            forwardThrustSprite.draw(batch);
            backwardThrustSprite.draw(batch);
        } else {
            impulseControlSprite.draw(batch);
        }
        rotationControlSprite.draw(batch);
        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                inputCallback.applyThrust(true);
                break;
            case Input.Keys.S:
                inputCallback.applyThrust(false);
                break;
            case Input.Keys.A:
                inputCallback.applyRotation(false);
                break;
            case Input.Keys.D:
                inputCallback.applyRotation(true);
                break;
            case Input.Keys.SPACE:
                inputCallback.fireBullet();
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
//        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
//        camera.unproject(touchPosition);
//        if (isThrusterController) {
//            if (forwardThrustSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
//                inputCallback.applyRotation(true);
//            }
//            if (backwardThrustSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
//                inputCallback.applyRotation(false);
//            }
//        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
//        if (!isThrusterController) {
//            if (impulseControlSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
//                inputCallback.applyImpulse(touchPosition.x - impulseControlOrigin.x,
//                        touchPosition.y - impulseControlOrigin.y);
//            }
//        }
        inputCallback.fireBullet();
        Vector2 angleCoordinates = (new Vector2(touchPosition.x, touchPosition.y)).sub(rotationControlOrigin);
        if (rotationControlSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
            inputCallback.applyImpulse(angleCoordinates.x, angleCoordinates.y);
        }
        if (forwardThrustSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
            inputCallback.applyRotation(true);
        }
        if (backwardThrustSprite.getBoundingRectangle().contains(touchPosition.x, touchPosition.y)) {
            inputCallback.applyRotation(false);
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
