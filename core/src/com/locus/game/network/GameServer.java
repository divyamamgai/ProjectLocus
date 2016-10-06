package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
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

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server();
        Network.registerClasses(server);

        playerMap = new LinkedHashMap<Integer, Player>();
        shipIndexMap = new HashMap<Integer, Integer>();

    }

    private void addPlayer(int connectionID, Ship.Property shipProperty) {

        playerMap.put(connectionID, new Player(shipProperty, false));

        float angleRad = connectionID * ProjectLocus.PLAYER_START_ANGLE_DELTA;

        shipIndexMap.put(connectionID,
                lobbyScreen.getLevel().addShipAlive(shipProperty,
                        ProjectLocus.WORLD_HALF_WIDTH +
                                MathUtils.cos(angleRad) * ProjectLocus.PLAYER_START_RADIUS,
                        ProjectLocus.WORLD_HALF_HEIGHT +
                                MathUtils.sin(angleRad) * ProjectLocus.PLAYER_START_RADIUS,
                        angleRad + ProjectLocus.PI_BY_TWO,
                        connectionID == 0));

    }

    private void removePlayer(int connectionID) {
        if (playerMap.containsKey(connectionID)) {
            Gdx.app.log("Host", "Number Of Players : " + playerMap.size());
            playerMap.remove(connectionID);
            Gdx.app.log("Host", "Client Removed #" + connectionID);
            Gdx.app.log("Size", String.valueOf(playerMap.size()));
            Gdx.app.log("Key", playerMap.keySet().toString());
            Gdx.app.log("Value", playerMap.values().toString());
            // Remove returns the deleted value.
            lobbyScreen.getLevel().removeShipAlive(shipIndexMap.remove(connectionID));
            sendUpdateLobby();
        }
    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        playerMap.clear();
        shipIndexMap.clear();

        server.addListener(new HostListener(this));

        try {

            server.bind(Network.SERVER_TCP_PORT, Network.SERVER_UDP_PORT);
            server.start();

            lobbyScreen.setState(LobbyScreen.State.Started);

            Gdx.app.log("Lobby Host", "Server Started @ TCP : " + Network.SERVER_TCP_PORT +
                    " UDP : " + Network.SERVER_UDP_PORT);

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

        if (object instanceof Network.PlayerJoinRequest) {

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
                            "Already Existing Client With ID : " + connectionID));

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

                Gdx.app.log("Host", "Player #" + connectionID + " Is Ready");

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

            Gdx.app.log("Host", String.valueOf(playerMap.size()));

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
                    ship = lobbyScreen.getLevel().getShipAlive(shipIndexMap.get(connectionID));
                    bodyPosition = ship.getBodyPosition();
                    createShipList.add(new Network.CreateShip(
                            playerMap.get(connectionID).property,
                            new ShipState(
                                    ship.getX(), ship.getY(),
                                    bodyPosition.x, bodyPosition.y,
                                    ship.getRotation(),
                                    ship.getHealth())));
                }

                server.sendToAllTCP(new Network.StartGame(createShipList));

                lobbyScreen.startGame(ProjectLocus.GAME_COUNT_DOWN);

                Gdx.app.log("Host", "All Ready, Starting Game...");

            }

        }

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
