package com.locus.game.tools;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Rohit Yadav on 26-Sep-16.
 * Input Controller
 */
public class InputController implements InputProcessor {

    public interface InputCallBack {

        void applyThrust(boolean direction);

        void applyRotation(boolean direction);

        void fire();

    }

    private float initialXCoordinate, initialYCoordinate;
    private OrthographicCamera camera;
    private InputCallBack inputCallBack;
    private static boolean rotationState, thrustState, rotationDirection, thrustDirection;
    private static int pointerThrust, pointerRotation;

    public InputController(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
        camera = new OrthographicCamera(854, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        rotationState = thrustState = rotationDirection = thrustDirection = false;
        pointerRotation = pointerThrust = -1;
    }

    @Override
    public boolean keyDown(int keycode) {
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
        camera.unproject(touchPosition);
        initialXCoordinate = touchPosition.x;
        initialYCoordinate = touchPosition.y;
        if (touchPosition.x > 0) {
            pointerThrust = pointer;
        } else {
            pointerRotation = pointer;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointerRotation == pointer) {
            rotationState = false;
        }
        if (pointerThrust == pointer) {
            thrustState = false;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
        if (pointer == pointerThrust) {
            if (touchPosition.x > 0) {
                if (Math.abs(touchPosition.y - initialYCoordinate) > 24) {
                    thrustDirection = (initialYCoordinate - touchPosition.y <= 0);
                    thrustState = true;
                } else {
                    thrustState = false;
                }
            } else {
                thrustState = false;
            }
        }
        if (pointer == pointerRotation) {
            if (touchPosition.x < 0) {
                if (Math.abs(touchPosition.x - initialXCoordinate) > 24) {
                    rotationDirection = (initialXCoordinate - touchPosition.x <= 0);
                    rotationState = true;
                } else {
                    rotationState = false;
                }
            } else {
                rotationState = false;
            }
        }
        inputCallBack.fire();
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

    public void update() {
        if (rotationState) {
            inputCallBack.applyRotation(rotationDirection);
        }
        if (thrustState) {
            inputCallBack.applyThrust(thrustDirection);
        }
    }
}
