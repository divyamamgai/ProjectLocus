package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.locus.game.ProjectLocus;
import com.locus.game.screens.LobbyScreen;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Client
 */

public class GameClient {

    private Client client;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    public int connectionID;
    private String hostAddress;

    private class GameClientConnectRunnable implements Runnable {

        private LobbyScreen lobbyJoinScreen;

        GameClientConnectRunnable(LobbyScreen lobbyScreen) {
            this.lobbyJoinScreen = lobbyScreen;
        }

        @Override
        public void run() {
            List<InetAddress> addressList = client.discoverHosts(Network.SERVER_UDP_PORT,
                    Network.CONNECTION_TIMEOUT);
            Gdx.app.log("Lobby Client", addressList.toString());
            lobbyJoinScreen.state = LobbyScreen.State.Connecting;
            for (InetAddress address : addressList) {
                try {
                    // We only need IPv4 addresses.
                    if (address instanceof Inet4Address) {
                        client.connect(Network.CONNECTION_TIMEOUT,
                                address.getHostAddress(), Network.SERVER_TCP_PORT,
                                Network.SERVER_UDP_PORT);
                        Gdx.app.log("Lobby Client", "Connected To Host @ " +
                                address.getHostAddress() + ":" + Network.SERVER_TCP_PORT);
                        // For now break at the first successfully connected server.
                        lobbyJoinScreen.state = LobbyScreen.State.Connected;
                    }
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Check if the connecting failed with everyone.
            if (lobbyJoinScreen.state == LobbyScreen.State.Connecting) {
                lobbyJoinScreen.state = LobbyScreen.State.Failed;
            }
        }
    }

    public GameClient(ProjectLocus projectLocus) {

        this.projectLocus = projectLocus;

        client = new Client();
        Network.registerClasses(client);

    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        client.addListener(new ClientListener(this));
        client.start();

        // Launch a dirty |:P} Thread to retrieve list of the IP Addresses.
        new Thread(new GameClientConnectRunnable(lobbyScreen)).start();

    }

    void onConnected(Connection connection) {

        connectionID = connection.getID();

        hostAddress = connection.getRemoteAddressTCP().toString();

        client.sendTCP(new Network.PlayerJoinRequest(projectLocus.playerShipProperty));
        client.updateReturnTripTime();

    }

    void onReceived(Connection connection, Object object) {

        if (object instanceof Network.UpdateShipState) {

            Network.UpdateShipState updateShipState = (Network.UpdateShipState) object;

            lobbyScreen.multiPlayerPlayScreen.level.updateShip(updateShipState.shipState,
                    updateShipState.connectionID);

            Gdx.app.log("Client", "Received Ship State For : " + updateShipState.connectionID);

        } else if (object instanceof Network.UpdateLobby) {

            lobbyScreen.playerMap = ((Network.UpdateLobby) object).playerMap;
            lobbyScreen.isLobbyToBeUpdated = true;

            Gdx.app.log("Client", "Accepted Player Count : " +
                    String.valueOf(lobbyScreen.playerMap.size()));

        } else if (object instanceof Network.PlayerJoinRequestRejected) {

            projectLocus.setScreen(lobbyScreen.selectModeScreen);
            Gdx.app.log("Client", ((Network.PlayerJoinRequestRejected) object).reason);

        } else if (object instanceof Network.LevelProperty) {

            lobbyScreen.levelProperty = ((Network.LevelProperty) object).levelProperty;
            lobbyScreen.initializePlayScreen = true;

            Gdx.app.log("Client", "Received Level Property");

        } else if (object instanceof Network.UpdateAllShipState) {

            lobbyScreen.shipStateMap = ((Network.UpdateAllShipState) object).shipStateMap;
            lobbyScreen.isShipStateToBeUpdated = true;

            Gdx.app.log("Client", "Received Ship State Map, Initialized.");

        } else if (object instanceof Network.StartGame) {

            float gameStartTime = ProjectLocus.GAME_COUNT_DOWN -
                    ((float) connection.getReturnTripTime() / 1000f);

            Gdx.app.log("Client", "Received Start Game, Starting Game In " + gameStartTime);

            lobbyScreen.gameStartTime = gameStartTime;
            lobbyScreen.isGameToBeStarted = true;

        }

    }

    void onDisconnected(Connection connection) {

        projectLocus.setScreen(lobbyScreen.selectModeScreen);

    }

    public void sendReadyState(boolean isReady) {
        client.sendTCP(new Network.PlayerReadyRequest(isReady));
        client.updateReturnTripTime();
    }

    public void sendShipState(ShipState shipState) {
        client.sendTCP(new Network.UpdateShipState(shipState, connectionID));
        client.updateReturnTripTime();
    }

    public void stop() {
        client.close();
        client.stop();
    }

}
