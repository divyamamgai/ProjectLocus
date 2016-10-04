package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;

import java.io.IOException;
import java.util.ArrayList;
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
        playerMap.put(0, new Player(projectLocus.playerShipProperty, false));

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
            lobbyScreen.updateLobby(playerMap);
        } catch (IOException e) {
            lobbyScreen.state = LobbyScreen.State.Failed;
            e.printStackTrace();
        }

    }

    void onConnected(Connection connection) {

    }

    void onReceived(Connection connection, Object object) {
        if (object instanceof Network.PlayerJoinRequest) {
            Network.PlayerJoinRequest playerJoinRequest = (Network.PlayerJoinRequest) object;
            int connectionID = connection.getID();
            if (playerMap.containsKey(connectionID)) {
                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Already Existing Client With ID : " + connectionID));
            } else {
                playerMap.put(connectionID, new Player(playerJoinRequest.property, false));
                server.sendToAllTCP(new Network.UpdateLobby(playerMap));
                lobbyScreen.updateLobby(playerMap);
            }
        }
    }

    void onDisconnected(Connection connection) {
        int connectionID = connection.getID();
        if (playerMap.containsKey(connectionID)) {
            playerMap.remove(connectionID);
            server.sendToAllTCP(new Network.UpdateLobby(playerMap));
            lobbyScreen.updateLobby(playerMap);
        }
    }

    public void stop() {
        server.close();
        server.stop();
    }

}
