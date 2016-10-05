package com.locus.game.tools;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.locus.game.ProjectLocus;
import com.locus.game.network.ShipState;

/**
 * Created by Rohit Yadav on 26-Sep-16.
 * Input Controller
 */
public class InputController implements InputProcessor, GestureDetector.GestureListener {

    public interface InputCallBack {

        void applyThrust(boolean isForward);

        void applyRotation(boolean isClockwise);

        void fire();

    }

    private float initialXCoordinateRotation, initialYCoordinateThrust, tapXCoordinate, tapYCoordinate;
    private OrthographicCamera camera;
    private InputCallBack inputCallBack;
    private static boolean isRotationEnabled, isThrustEnabled, isFireEnabled,
            isRotationClockwise, isThrustForward, isSetRotationPointer, isSetThrustPointer,
            isPrimaryBulletEnabled, isSecondaryBulletEnabled;
    private static int thrustPointerID, rotationPointerID, firingPointerID;

    private ProjectLocus projectLocus;
    private boolean isHost;
    private ShipState shipState;

    public InputController(ProjectLocus projectLocus, InputCallBack inputCallBack, boolean isHost,
                           ShipState shipState) {
        this.projectLocus = projectLocus;
        this.inputCallBack = inputCallBack;
        this.isHost = isHost;
        this.shipState = shipState;
        camera = new OrthographicCamera(854, 480);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        isRotationEnabled = isThrustEnabled = isFireEnabled = false;
        isRotationClockwise = isThrustForward = false;
        rotationPointerID = thrustPointerID = firingPointerID = -1;
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
        if (firingPointerID == pointer) {
            if (isPrimaryBulletEnabled) {
                isFireEnabled = (Math.abs(screenX - tapXCoordinate) < 20) && (Math.abs(screenY - tapYCoordinate) < 20);
                isPrimaryBulletEnabled = false;
            }
            if (isSecondaryBulletEnabled) {
                isFireEnabled = (Math.abs(screenX - tapXCoordinate) < 20) && (Math.abs(screenY - tapYCoordinate) < 20);
                isSecondaryBulletEnabled = false;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if ((rotationPointerID == pointer) && (isSetRotationPointer)) {
            isRotationEnabled = false;
            isSetRotationPointer = false;
        }
        if ((thrustPointerID == pointer) && (isSetThrustPointer)) {
            isThrustEnabled = false;
            isSetThrustPointer = false;
        }
        if (firingPointerID == pointer) {
            isFireEnabled = false;
        }
        return false;
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
        Vector3 touchPosition = new Vector3(x, y, 0);
        camera.unproject(touchPosition);
        if (count == 1) {
            tapXCoordinate = x;
            tapYCoordinate = y;
            isPrimaryBulletEnabled = true;
            isSecondaryBulletEnabled = false;
            if (touchPosition.x > 0) {
                firingPointerID = thrustPointerID;
            } else {
                firingPointerID = rotationPointerID;
            }
        } else if (count == 2) {
            tapXCoordinate = x;
            tapYCoordinate = y;
            isSecondaryBulletEnabled = true;
            isPrimaryBulletEnabled = false;
            if (touchPosition.x > 0) {
                firingPointerID = thrustPointerID;
            } else {
                firingPointerID = rotationPointerID;
            }
        } else {
            isPrimaryBulletEnabled = isSecondaryBulletEnabled = false;
            isFireEnabled = false;
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
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

    public void update() {
        shipState.isRotationEnabled = isRotationEnabled;
        shipState.isThrustEnabled = isThrustEnabled;
        shipState.isFireEnabled = isFireEnabled;
        if (isRotationEnabled) {
            inputCallBack.applyRotation(isRotationClockwise);
            shipState.rotationDirection = isRotationClockwise;
            if (isHost) {
                projectLocus.gameServer.sendShipState(shipState);
            } else {
                projectLocus.gameClient.sendShipState(shipState);
            }
        }
        if (isThrustEnabled) {
            inputCallBack.applyThrust(isThrustForward);
            shipState.thrustDirection = isThrustForward;
            if (isHost) {
                projectLocus.gameServer.sendShipState(shipState);
            } else {
                projectLocus.gameClient.sendShipState(shipState);
            }
        }
        if (isFireEnabled) {
            inputCallBack.fire();
            if (isHost) {
                projectLocus.gameServer.sendShipState(shipState);
            } else {
                projectLocus.gameClient.sendShipState(shipState);
            }
        }
    }
}
