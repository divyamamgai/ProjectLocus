package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;

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

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server();
        Network.registerClasses(server);

        playerMap = new HashMap<Integer, Player>();
        playerMap.put(0, new Player(projectLocus.playerShipProperty, true));

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
            Network.PlayerJoinRequest playerJoinRequest = (Network.PlayerJoinRequest) object;
            if (playerMap.containsKey(connectionID)) {
                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Already Existing Client With ID : " + connectionID));
            } else {

                playerMap.put(connectionID, new Player(playerJoinRequest.property, false));

                server.sendToAllTCP(new Network.UpdateLobby(playerMap));

                lobbyScreen.playerMap = playerMap;
                lobbyScreen.isLobbyToBeUpdated = true;

                connection.sendTCP(
                        new Network.LevelProperty(
                                lobbyScreen.multiPlayerPlayScreen.level.property));

            }
        } else if (object instanceof Network.PlayerReadyRequest) {
            Network.PlayerReadyRequest playerReadyRequest = (Network.PlayerReadyRequest) object;
            if (playerMap.containsKey(connectionID)) {

                playerMap.get(connectionID).isReady = playerReadyRequest.isReady;

                server.sendToAllTCP(new Network.UpdateLobby(playerMap));

                lobbyScreen.playerMap = playerMap;
                lobbyScreen.isLobbyToBeUpdated = true;

                Gdx.app.log("Host", "Player #" + connectionID + " Is Ready");

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

    public void stop() {
        server.close();
        server.stop();
    }

}
