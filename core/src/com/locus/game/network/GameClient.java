package com.locus.game.network;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.locus.game.ProjectLocus;
import com.locus.game.levels.ClientLevel;
import com.locus.game.screens.LobbyScreen;
import com.locus.game.screens.ErrorScreen;
import com.locus.game.tools.InputController;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Game Client
 */

public class GameClient implements InputController.InputCallBack {

    private Client client;
    private ProjectLocus projectLocus;
    private LobbyScreen lobbyScreen;
    private ClientLevel level;
    private Network.ControllerState controllerState;

    public void setLevel(ClientLevel level) {
        this.level = level;
    }

    @Override
    public void applyThrust(boolean isForward) {
    }

    @Override
    public void applyRotation(boolean isClockwise) {
    }

    @Override
    public void fire() {
    }

    @Override
    public void applyControls(boolean isThrustEnabled, boolean isThrustForward,
                              boolean isRotationEnabled, boolean isRotationClockwise,
                              boolean isFireEnabled) {

        controllerState.isThrustEnabled = isThrustEnabled;
        controllerState.isThrustForward = isThrustForward;
        controllerState.isRotationEnabled = isRotationEnabled;
        controllerState.isRotationClockwise = isRotationClockwise;
        controllerState.isFireEnabled = isFireEnabled;

        client.sendUDP(controllerState);

    }

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

        client = new Client(10240, 10240);
        Network.registerClasses(client);

        controllerState = new Network.ControllerState();

    }

    public void start(LobbyScreen lobbyScreen) {

        this.lobbyScreen = lobbyScreen;

        client.addListener(new ClientListener(this));
        client.start();

        new Thread(new GameClientConnectRunnable(lobbyScreen)).start();

    }

    void onConnected() {
        client.sendTCP(new Network.PlayerJoinRequest(projectLocus.playerShipProperty));
    }

    void onReceived(Connection connection, Object object) {

        if (object instanceof Network.GameState) {

            Network.GameState gameState = (Network.GameState) object;

            level.setPlanetState(gameState.planetState);
            level.setMoonStateList(gameState.moonStateList);
            level.setShipStateList(gameState.shipStateList);
            level.setBulletKilledArray(gameState.bulletKilledArray);
            level.setBulletAliveStateList(gameState.bulletAliveStateList);

        } else if (object instanceof Network.UpdateLobby) {

            lobbyScreen.setPlayerMap(((Network.UpdateLobby) object).playerMap);

        } else if (object instanceof Network.PlayerJoinRequestRejected) {

            projectLocus.setScreen(new ErrorScreen(projectLocus, lobbyScreen.selectModeScreen,
                    ((Network.PlayerJoinRequestRejected) object).reason));
            lobbyScreen.dispose();

        } else if (object instanceof Network.LevelProperty) {

            lobbyScreen.setLevelProperty(((Network.LevelProperty) object).levelProperty);

        } else if (object instanceof Network.StartGame) {

            ArrayList<Network.CreateShip> createShipList =
                    ((Network.StartGame) object).createShipList;

            float startGameIn = ProjectLocus.GAME_COUNT_DOWN -
                    ((float) connection.getReturnTripTime() / 1000f);

            lobbyScreen.startGame(startGameIn);

            for (Network.CreateShip createShip : createShipList) {
                level.addShipAlive(createShip.property, createShip.shipState,
                        createShip.connectionID == connection.getID());
            }

        }

    }

    void onDisconnected() {
        stop();
        projectLocus.setScreen(new ErrorScreen(projectLocus, lobbyScreen.selectModeScreen,
                "Host Disconnected"));
        lobbyScreen.dispose();
    }

    public void sendReadyState(boolean isReady) {
        client.sendTCP(new Network.PlayerReadyRequest(isReady));
    }

    public void stop() {
        client.close();
        client.stop();
    }

}
