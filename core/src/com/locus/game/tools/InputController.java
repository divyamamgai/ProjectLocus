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

    private float initialXCoordinateRotation, initialYCoordinateThrust;
    private OrthographicCamera camera;
    private InputCallBack inputCallBack;
    private static boolean rotationState, thrustState, rotationDirection,
            thrustDirection, isSetRotationPointer, isSetThrustPointer;
    private static int thrustPointerID, rotationPointerID;

    public InputController(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
        camera = new OrthographicCamera(854, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        rotationState = thrustState = rotationDirection = thrustDirection = false;
        rotationPointerID = thrustPointerID = -1;
        isSetRotationPointer = isSetThrustPointer = false;
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
        if ((touchPosition.x > 0) && (!isSetThrustPointer)){
            thrustPointerID = pointer;
            isSetThrustPointer = true;
            initialYCoordinateThrust = touchPosition.y;
        }
        if((touchPosition.x < 0) && (!isSetRotationPointer)){
            rotationPointerID = pointer;
            isSetRotationPointer = true;
            initialXCoordinateRotation = touchPosition.x;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if ((rotationPointerID == pointer) && (isSetRotationPointer)){
            rotationState = false;
            isSetRotationPointer = false;
            rotationPointerID = -1;
        }
        if ((thrustPointerID == pointer) && (isSetThrustPointer)){
            thrustState = false;
            isSetThrustPointer = false;
            thrustPointerID = -1;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector3 touchPosition = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPosition);
        if ((pointer == thrustPointerID) && (isSetThrustPointer)){
            if (touchPosition.x > 0) {
                if (Math.abs(touchPosition.y - initialYCoordinateThrust) > 16) {
                    thrustDirection = (initialYCoordinateThrust - touchPosition.y <= 0);
                    thrustState = true;
                } else {
                    thrustState = false;
                }
            } else {
                thrustState = false;
            }
        }
        if ((pointer == rotationPointerID) && (isSetRotationPointer)) {
            if (touchPosition.x < 0) {
                if (Math.abs(touchPosition.x - initialXCoordinateRotation) > 16) {
                    rotationDirection = (initialXCoordinateRotation - touchPosition.x <= 0);
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
