package com.locus.game.tools;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Rohit Yadav on 26-Sep-16.
 * Input Controller
 */
public class InputController implements InputProcessor {

    public interface InputCallBack {

        void applyThrust(boolean isForward);

        void applyRotation(boolean isClockwise);

        void fire();

    }

    private float initialXCoordinateRotation, initialYCoordinateThrust;
    private OrthographicCamera camera;
    private InputCallBack inputCallBack;
    private static boolean isRotationEnabled, isThrustEnabled, isFireEnabled,
            isRotationClockwise, isThrustForward, isSetRotationPointer, isSetThrustPointer;
    private static int thrustPointerID, rotationPointerID;

    public InputController(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
        camera = new OrthographicCamera(854, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        isRotationEnabled = isThrustEnabled = isFireEnabled = false;
        isRotationClockwise = isThrustForward = false;
        rotationPointerID = thrustPointerID = -1;
        isSetRotationPointer = isSetThrustPointer = false;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
                isThrustEnabled = true;
                isThrustForward = true;
                break;
            case Input.Keys.S:
                isThrustEnabled = true;
                isThrustForward = false;
                break;
            case Input.Keys.A:
                isRotationEnabled = true;
                isRotationClockwise = false;
                break;
            case Input.Keys.D:
                isRotationEnabled = true;
                isRotationClockwise = true;
                break;
            case Input.Keys.SPACE:
                isFireEnabled = true;
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.W:
            case Input.Keys.S:
                isThrustEnabled = false;
                break;
            case Input.Keys.A:
            case Input.Keys.D:
                isRotationEnabled = false;
                break;
            case Input.Keys.SPACE:
                isFireEnabled = false;
                break;
        }
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
        if ((touchPosition.x > 0) && (!isSetThrustPointer)) {
            thrustPointerID = pointer;
            isSetThrustPointer = true;
            initialYCoordinateThrust = touchPosition.y;
        }
        if ((touchPosition.x < 0) && (!isSetRotationPointer)) {
            rotationPointerID = pointer;
            isSetRotationPointer = true;
            initialXCoordinateRotation = touchPosition.x;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if ((rotationPointerID == pointer) && (isSetRotationPointer)) {
            isRotationEnabled = false;
            isSetRotationPointer = false;
            rotationPointerID = -1;
        }
        if ((thrustPointerID == pointer) && (isSetThrustPointer)) {
            isThrustEnabled = false;
            isSetThrustPointer = false;
            thrustPointerID = -1;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
        if ((pointer == thrustPointerID) && (isSetThrustPointer)) {
            if (touchPosition.x > 0) {
                if (Math.abs(touchPosition.y - initialYCoordinateThrust) > 16) {
                    isThrustForward = (initialYCoordinateThrust - touchPosition.y <= 0);
                    isThrustEnabled = true;
                } else {
                    isThrustEnabled = false;
                }
            } else {
                isThrustEnabled = false;
            }
        }
        if ((pointer == rotationPointerID) && (isSetRotationPointer)) {
            if (touchPosition.x < 0) {
                if (Math.abs(touchPosition.x - initialXCoordinateRotation) > 16) {
                    isRotationClockwise = (initialXCoordinateRotation - touchPosition.x <= 0);
                    isRotationEnabled = true;
                } else {
                    isRotationEnabled = false;
                }
            } else {
                isRotationEnabled = false;
            }
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

    public void update() {
        if (isRotationEnabled) {
            inputCallBack.applyRotation(isRotationClockwise);
        }
        if (isThrustEnabled) {
            inputCallBack.applyThrust(isThrustForward);
        }
        if (isFireEnabled) {
            inputCallBack.fire();
        }
    }
}
