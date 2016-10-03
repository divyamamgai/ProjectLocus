package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Server
 */

public class GameServer {

    private Server server;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private ArrayList<Player> playerList;

    public GameServer(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        server = new Server();
        Network.registerClasses(server);

        playerList = new ArrayList<Player>();
        playerList.add(new Player(0, projectLocus.playerShipProperty, false));

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
            boolean isAlreadyExisting = false;
            int connectionID = connection.getID();
            for (Player player : playerList) {
                if (player.connectionID == connectionID) {
                    isAlreadyExisting = true;
                    break;
                }
            }
            if (isAlreadyExisting) {
                connection.sendTCP(new Network.PlayerJoinRequestRejected(
                        "Already Existing Client With ID : " + connectionID));
            } else {
                playerList.add(
                        new Player(connection.getID(), playerJoinRequest.property, false));
                connection.sendTCP(new Network.PlayerJoinResponse(playerList));
            }
        }
    }

    void onDisconnected(Connection connection) {

    }

    public void stop() {
        server.close();
        server.stop();
    }

}
