package com.locus.game.network;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.Level;
import com.locus.game.screens.LobbyScreen;
import com.locus.game.sprites.entities.Ship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Server
 */

public class GameServer {

    private Server server;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private LinkedHashMap<Integer, Player> playerMap;
    private HashMap<Integer, Integer> shipIndexMap;
    private Network.GameState gameState;
    private Level level;

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server(10240, 10240);
        Network.registerClasses(server);

        playerMap = new LinkedHashMap<Integer, Player>();
        shipIndexMap = new HashMap<Integer, Integer>();
        gameState = new Network.GameState();

    }

    private void addPlayer(int connectionID, Ship.Property shipProperty) {

        playerMap.put(connectionID, new Player(shipProperty, false));

        float angleRad = connectionID * ProjectLocus.PLAYER_START_ANGLE_DELTA;

        shipIndexMap.put(connectionID,
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
            level.removeShipAlive(shipIndexMap.remove(connectionID));
            sendUpdateLobby();
        }
    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        level = lobbyScreen.getLevel();

        playerMap.clear();
        shipIndexMap.clear();

        server.addListener(new HostListener(this));

        try {

            server.bind(Network.SERVER_TCP_PORT, Network.SERVER_UDP_PORT);
            server.start();

            lobbyScreen.setState(LobbyScreen.State.Started);

            // Add the Host itself initially.
            addPlayer(0, projectLocus.playerShipProperty);

            lobbyScreen.setPlayerMap(playerMap);

        } catch (IOException e) {

            lobbyScreen.setState(LobbyScreen.State.Failed);

            e.printStackTrace();

        }

    }

    void onConnected(Connection connection) {

    }

    void onReceived(Connection connection, Object object) {

        int connectionID = connection.getID();

        if (object instanceof Network.ControllerState) {

            Network.ControllerState controllerState = (Network.ControllerState) object;
            Ship ship = level.getShipAlive(shipIndexMap.get(connectionID));

            if (controllerState.isThrustEnabled) {
                ship.applyThrust(controllerState.isThrustForward);
            }
            if (controllerState.isRotationEnabled) {
                ship.applyRotation(controllerState.isRotationClockwise);
            }
            if (controllerState.isFireEnabled) {
                ship.fire();
            }

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
        removePlayer(connection.getID());
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

                ArrayList<Network.CreateShip> createShipList = new ArrayList<Network.CreateShip>();
                Ship ship;
                Vector2 bodyPosition;
                for (Integer connectionID : playerMap.keySet()) {
                    ship = level.getShipAlive(shipIndexMap.get(connectionID));
                    bodyPosition = ship.getBodyPosition();
                    createShipList.add(
                            new Network.CreateShip(
                                    connectionID,
                                    playerMap.get(connectionID).property,
                                    new ShipState(
                                            ship.getID(),
                                            bodyPosition.x, bodyPosition.y,
                                            ship.getRotation(),
                                            ship.getHealth())));
                }

                server.sendToAllTCP(new Network.StartGame(createShipList));

                lobbyScreen.startGame(ProjectLocus.GAME_COUNT_DOWN);

            }

        }

    }

    public void sendReadyState(boolean isReady) {

        // Set Host's ready state.
        playerMap.get(0).isReady = isReady;

        sendUpdateLobby();

    }

    public void sendGameState() {

        gameState.planetState = level.getPlanetState();
        gameState.moonStateList = level.getMoonStateList();
        gameState.shipStateList = level.getShipStateList();
        gameState.bulletAliveStateList = level.getBulletAliveStateList();
        gameState.bulletKilledArray = level.getBulletKilledArray();

        server.sendToAllTCP(gameState);

        gameState.bulletKilledArray.clear();

    }

    public void stop() {
        server.close();
        server.stop();
    }

}
