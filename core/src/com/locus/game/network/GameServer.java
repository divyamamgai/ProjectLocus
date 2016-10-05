package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Server
 */

public class GameServer {

    private Server server;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private HashMap<Integer, Player> playerMap;
    private HashMap<Integer, ShipState> shipStateMap;

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server();
        Network.registerClasses(server);

    }

    public void initializeMap() {

        playerMap = new HashMap<Integer, Player>();
        shipStateMap = new HashMap<Integer, ShipState>();

        // Add the Host itself initially.
        playerMap.put(0, new Player(projectLocus.playerShipProperty, false));
        shipStateMap.put(0, createShipState(projectLocus.playerShipProperty.type, 0));

    }

    private ShipState createShipState(Ship.Type shipType, int shipIndex) {
        ShipState shipState = new ShipState();
        EntityLoader.Definition playerShipDefinition = projectLocus.entityLoader
                .getShip(shipType.ordinal());
        shipState.health = playerShipDefinition.maxHealth;
        shipState.angleRad = ProjectLocus.PLAYER_START_ANGLE_DELTA * shipIndex;
        shipState.position.set(ProjectLocus.PLAYER_START_RADIUS * MathUtils.cos(shipState.angleRad),
                ProjectLocus.PLAYER_START_RADIUS * MathUtils.sin(shipState.angleRad));
        return shipState;
    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        server.addListener(new HostListener(this));

        try {

            server.bind(Network.SERVER_TCP_PORT, Network.SERVER_UDP_PORT);
            server.start();

            lobbyScreen.state = LobbyScreen.State.Started;

            Gdx.app.log("Lobby Host", "Server Started @ TCP : " + Network.SERVER_TCP_PORT +
                    " UDP : " + Network.SERVER_UDP_PORT);

            lobbyScreen.playerMap = playerMap;
            lobbyScreen.isLobbyToBeUpdated = true;

            lobbyScreen.shipStateMap = shipStateMap;
            lobbyScreen.isShipStateToBeUpdated = true;

        } catch (IOException e) {
            lobbyScreen.state = LobbyScreen.State.Failed;
            e.printStackTrace();
        }

    }

    void onConnected(Connection connection) {

    }

    void onReceived(Connection connection, Object object) {
        int connectionID = connection.getID();
        if (object instanceof Network.PlayerJoinRequest) {
            if (lobbyScreen.isGameStarted) {
                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Game Already Started"));
            } else {
                Network.PlayerJoinRequest playerJoinRequest = (Network.PlayerJoinRequest) object;
                if (playerMap.containsKey(connectionID)) {
                    connection.sendTCP(new Network.PlayerJoinRequestRejected(
                            "Already Existing Client With ID : " + connectionID));
                } else {

                    playerMap.put(connectionID, new Player(playerJoinRequest.property, false));
                    shipStateMap.put(connectionID,
                            createShipState(playerJoinRequest.property.type, shipStateMap.size()));

                    sendUpdateLobby();
                    sendInitializeShipState();

                    connection.sendTCP(
                            new Network.LevelProperty(
                                    lobbyScreen.multiPlayerPlayScreen.level.property));

                }
            }
        } else if (object instanceof Network.PlayerReadyRequest) {
            Network.PlayerReadyRequest playerReadyRequest = (Network.PlayerReadyRequest) object;
            if (playerMap.containsKey(connectionID)) {

                playerMap.get(connectionID).isReady = playerReadyRequest.isReady;

                sendUpdateLobby();

                Gdx.app.log("Host", "Player #" + connectionID + " Is Ready");

            } else {
                connection.sendTCP(new Network.Error("Player Not Found"));
            }
        }
    }

    void onDisconnected(Connection connection) {
        int connectionID = connection.getID();
        if (playerMap.containsKey(connectionID)) {

            playerMap.remove(connectionID);

            server.sendToAllTCP(new Network.UpdateLobby(playerMap));

            lobbyScreen.playerMap = playerMap;
            lobbyScreen.isLobbyToBeUpdated = true;

        }
    }

    private void sendUpdateLobby() {

        server.sendToAllTCP(new Network.UpdateLobby(playerMap));

        lobbyScreen.playerMap = playerMap;
        lobbyScreen.isLobbyToBeUpdated = true;

        // Check if all of the players are Ready so we can start the game.
        boolean areAllReady = true;
        for (Integer playerConnectionID : playerMap.keySet()) {
            if (!playerMap.get(playerConnectionID).isReady) {
                areAllReady = false;
                break;
            }
        }

        if (areAllReady) {
            server.sendToAllTCP(new Network.StartGame());
            Gdx.app.log("Host", "All Ready, Starting Game In 10...");
            lobbyScreen.isGameToBeStarted = true;
        }

    }

    private void sendInitializeShipState() {

        server.sendToAllTCP(new Network.InitializeShipState(shipStateMap));

        lobbyScreen.shipStateMap = shipStateMap;
        lobbyScreen.isShipStateToBeUpdated = true;

    }

    public void sendReadyState(boolean isReady) {

        // Set Host's ready state.
        playerMap.get(0).isReady = isReady;

        sendUpdateLobby();

    }

    public void stop() {
        server.close();
        server.stop();
    }

}
