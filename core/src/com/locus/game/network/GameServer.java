package com.locus.game.network;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.screens.ErrorScreen;
import com.locus.game.screens.LobbyScreen;
import com.locus.game.screens.ScoreBoardScreen;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Server
 */

public class GameServer implements InputController.InputCallBack {

    private Server server;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private LinkedHashMap<Integer, Player> playerMap;
    private HashMap<Integer, Short> connectionIDToShipIDMap;
    private Network.GameState gameState;
    private Level level;
    private Ship hostShip;
    private boolean isGameStarted, isGameEnded;

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server();
        Network.registerClasses(server);

        playerMap = new LinkedHashMap<Integer, Player>();
        connectionIDToShipIDMap = new HashMap<Integer, Short>();
        gameState = new Network.GameState();

        isGameStarted = isGameEnded = false;

    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        level = lobbyScreen.getLevel();

        playerMap.clear();
        connectionIDToShipIDMap.clear();

        server.addListener(new HostListener(this));

        try {

            server.bind(Network.SERVER_TCP_PORT, Network.SERVER_UDP_PORT);
            server.start();

            lobbyScreen.setState(LobbyScreen.State.Started);

            // Add the Host itself initially.
            addPlayer(0, projectLocus.playerShipProperty);
            // At the start the Ship at 0 index will be Host itself.
            hostShip = level.getShipAlive(connectionIDToShipIDMap.get(0));

            lobbyScreen.setPlayerMap(playerMap);

        } catch (IOException e) {

            lobbyScreen.setState(LobbyScreen.State.Failed);

            e.printStackTrace();

        }

    }

    void onConnected(Connection connection) {
        if (isGameStarted) {
            connection.close();
        }
    }

    void onReceived(Connection connection, Object object) {

        int connectionID = connection.getID();

        if (object instanceof Network.ControllerState) {

            Network.ControllerState controllerState = (Network.ControllerState) object;
            Ship ship = level.getShipAlive(connectionIDToShipIDMap.get(connectionID));

            if (ship != null) {

                if (controllerState.isThrustEnabled) {
                    ship.applyThrust(controllerState.isThrustForward);
                }
                if (controllerState.isRotationEnabled) {
                    ship.applyRotation(controllerState.isRotationClockwise);
                }
                ship.fire(controllerState.isPrimaryBulletEnabled,
                        controllerState.isSecondaryBulletEnabled,
                        controllerState.doPrimaryReset, controllerState.doSecondaryReset);
                server.sendToAllExceptTCP(connectionID, new Network.FireState(
                        ship.getID(),
                        controllerState.isPrimaryBulletEnabled,
                        controllerState.isSecondaryBulletEnabled,
                        controllerState.doPrimaryReset,
                        controllerState.doSecondaryReset
                ));
            }

        } else if (object instanceof Network.ShipKill) {

            level.getShipAlive(((Network.ShipKill) object).shipID).kill();

        } else if (object instanceof Network.PlayerJoinRequest) {

            if (lobbyScreen.isGameStarted()) {

                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Game Already Started"));

            } else if (playerMap.size() == ProjectLocus.MAX_PLAYER_COUNT) {

                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Lobby Is Full"));

            } else {

                Network.PlayerJoinRequest playerJoinRequest = (Network.PlayerJoinRequest) object;

                if (playerMap.containsKey(connectionID)) {

                    connection.sendTCP(new Network.PlayerJoinRequestRejected(
                            "Already Existing Client #" + connectionID));

                } else {

                    addPlayer(connectionID, playerJoinRequest.property);

                    sendUpdateLobby();

                    connection.sendTCP(
                            new Network.LevelProperty(lobbyScreen.getLevelProperty()));

                }

            }

        } else if (object instanceof Network.PlayerReadyRequest) {

            Network.PlayerReadyRequest playerReadyRequest = (Network.PlayerReadyRequest) object;

            if (playerMap.containsKey(connectionID)) {

                playerMap.get(connectionID).isReady = playerReadyRequest.isReady;

                sendUpdateLobby();

            } else {

                connection.sendTCP(new Network.Error("Player Not Found"));

            }

        }
    }

    void onDisconnected(Connection connection) {
        if (server != null) {
            removePlayer(connection.getID());
        }
    }

    private void addPlayer(int connectionID, Ship.Property shipProperty) {

        playerMap.put(connectionID, new Player(shipProperty, false));

        float angleRad = connectionID * ProjectLocus.PLAYER_START_ANGLE_DELTA;

        connectionIDToShipIDMap.put(connectionID,
                level.addShipAlive(shipProperty,
                        ProjectLocus.WORLD_HALF_WIDTH +
                                MathUtils.cos(angleRad) * ProjectLocus.PLAYER_START_RADIUS,
                        ProjectLocus.WORLD_HALF_HEIGHT +
                                MathUtils.sin(angleRad) * ProjectLocus.PLAYER_START_RADIUS,
                        angleRad + ProjectLocus.PI_BY_TWO,
                        connectionID == 0));

    }

    private void removePlayer(int connectionID) {
        if (playerMap.containsKey(connectionID)) {
            playerMap.remove(connectionID);
            // Remove returns the deleted value.
            Ship shipRemoved = level.removeShipAlive(connectionIDToShipIDMap.remove(connectionID));
            if (isGameStarted) {
                if (playerMap.size() > 1) {
                    if (shipRemoved != null) {
                        server.sendToAllTCP(new Network.RemoveShip(shipRemoved.getID()));
                    }
                } else {
                    allLeft();
                }
            } else {
                sendUpdateLobby();
            }
        }
    }

    private void sendUpdateLobby() {

        server.sendToAllTCP(new Network.UpdateLobby(playerMap));

        lobbyScreen.setPlayerMap(playerMap);

        // More than one player is needed for Multi Player.
        if (playerMap.size() > 1) {

            // Check if all of the players are Ready so we can start the game.
            boolean areAllReady = true;

            for (Player player : playerMap.values()) {
                if (!player.isReady) {
                    areAllReady = false;
                    break;
                }
            }

            if (areAllReady) {
                lobbyScreen.allReady();
                // To be removed.
                sendStartGame();
            }

        }

    }

    private void sendStartGame() {
        ArrayList<Network.AddShip> addShipList = new ArrayList<Network.AddShip>();
        Ship ship;
        Vector2 bodyPosition;
        for (Integer connectionID : playerMap.keySet()) {
            ship = level.getShipAlive(connectionIDToShipIDMap.get(connectionID));
            bodyPosition = ship.getBodyPosition();
            addShipList.add(
                    new Network.AddShip(
                            connectionID,
                            playerMap.get(connectionID).property,
                            new ShipState(
                                    ship.getID(),
                                    bodyPosition.x, bodyPosition.y,
                                    ship.getRotation(),
                                    ship.getHealth())));
        }
        server.sendToAllTCP(new Network.StartGame(addShipList));
        lobbyScreen.startGame(ProjectLocus.GAME_COUNT_DOWN);
        isGameStarted = true;
    }

    public void sendReadyState(boolean isReady) {

        // Set Host's ready state.
        playerMap.get(0).isReady = isReady;

        sendUpdateLobby();

    }

    public void sendGameState() {

        if (level.getAlivePlayerCount() > 1) {
            gameState.planetState = level.getPlanetState();
            gameState.moonStateList = level.getMoonStateList();
            gameState.shipStateList = level.getShipStateList();
            server.sendToAllTCP(gameState);
        } else {
            endGame();
        }

    }

    private void endGame() {
        server.sendToAllTCP(new Network.EndGame(level.getShipStateList()));
        projectLocus.setScreen(new ScoreBoardScreen(projectLocus, lobbyScreen,
                gameState.shipStateList));
        isGameEnded = true;
    }

    private void allLeft() {
        if (!isGameEnded) {
            stop();
            projectLocus.setScreen(new ErrorScreen(projectLocus, "All Clients Disconnected"));
            lobbyScreen.dispose();
        }
    }

    public void stop() {
        server.close();
        server.stop();
    }

    @Override
    public void applyThrust(boolean isForward) {
        hostShip.applyThrust(isForward);
    }

    @Override
    public void applyRotation(boolean isClockwise) {
        hostShip.applyRotation(isClockwise);
    }

    @Override
    public void fire(boolean isPrimaryBulletEnabled, boolean isSecondaryBulletEnabled,
                     boolean doPrimaryReset, boolean doSecondaryReset) {
        hostShip.fire(isPrimaryBulletEnabled, isSecondaryBulletEnabled, doPrimaryReset,
                doSecondaryReset);
        server.sendToAllTCP(new Network.FireState(hostShip.getID(), isPrimaryBulletEnabled,
                isSecondaryBulletEnabled, doPrimaryReset, doSecondaryReset));
    }

    @Override
    public void applyControls(boolean isThrustEnabled, boolean isThrustForward,
                              boolean isRotationEnabled, boolean isRotationClockwise,
                              boolean isPrimaryBulletEnabled, boolean isSecondaryBulletEnabled,
                              boolean doPrimaryReset, boolean doSecondaryReset) {
    }
}
