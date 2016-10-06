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

    private class GameClientConnectRunnable implements Runnable {

        private LobbyScreen lobbyJoinScreen;

        GameClientConnectRunnable(LobbyScreen lobbyScreen) {
            this.lobbyJoinScreen = lobbyScreen;
        }

        @Override
        public void run() {

            List<InetAddress> addressList = client.discoverHosts(Network.SERVER_UDP_PORT,
                    Network.CONNECTION_TIMEOUT);

            lobbyJoinScreen.setState(LobbyScreen.State.Connecting);

            for (InetAddress address : addressList) {
                try {

                    // We only need IPv4 addresses.
                    if (address instanceof Inet4Address) {

                        client.connect(Network.CONNECTION_TIMEOUT,
                                address.getHostAddress(), Network.SERVER_TCP_PORT,
                                Network.SERVER_UDP_PORT);

                        lobbyJoinScreen.setState(LobbyScreen.State.Connected);

                        Gdx.app.log("Lobby Client", "Connected To Host @ " +
                                address.getHostAddress() + ":" + Network.SERVER_TCP_PORT);

                        // For now break at the first successfully connected server.
                        break;

                    }

                } catch (IOException e) {

                    lobbyJoinScreen.setState(LobbyScreen.State.Failed);

                    e.printStackTrace();

                }
            }

            // Check if the connecting failed with everyone.
            if (lobbyJoinScreen.checkState(LobbyScreen.State.Connecting)) {
                lobbyJoinScreen.setState(LobbyScreen.State.Failed);
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

        new Thread(new GameClientConnectRunnable(lobbyScreen)).start();

    }

    void onConnected(Connection connection) {
        Gdx.app.log("Client On Connected", String.valueOf(connection.getID()));
        client.sendTCP(new Network.PlayerJoinRequest(projectLocus.playerShipProperty));
        client.updateReturnTripTime();
    }

    void onReceived(Connection connection, Object object) {

        if (object instanceof Network.UpdateLobby) {

            lobbyScreen.setPlayerMap(((Network.UpdateLobby) object).playerMap);

        } else if (object instanceof Network.PlayerJoinRequestRejected) {

            projectLocus.setScreen(lobbyScreen.selectModeScreen);
            lobbyScreen.dispose();
            Gdx.app.log("Client", ((Network.PlayerJoinRequestRejected) object).reason);

        } else if (object instanceof Network.LevelProperty) {

            lobbyScreen.setLevelProperty(((Network.LevelProperty) object).levelProperty);

            Gdx.app.log("Client", "Received Level Property");

        } else if (object instanceof Network.StartGame) {

            float startGameIn = ProjectLocus.GAME_COUNT_DOWN -
                    ((float) connection.getReturnTripTime() / 1000f);

            Gdx.app.log("Client", "Received Start Game, Starting Game In " + startGameIn);

            lobbyScreen.startGame(startGameIn);
        }

    }

    void onDisconnected(Connection connection) {
        stop();
        projectLocus.setScreen(lobbyScreen.selectModeScreen);
        lobbyScreen.dispose();
    }

    public void sendReadyState(boolean isReady) {
        client.sendTCP(new Network.PlayerReadyRequest(isReady));
        client.updateReturnTripTime();
    }

    public void stop() {
        client.close();
        client.stop();
    }

}
