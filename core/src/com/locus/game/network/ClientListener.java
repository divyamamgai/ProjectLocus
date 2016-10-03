package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/**
 * Created by Divya Mamgai on 10/2/2016.
 * Client Listener
 */

class ClientListener extends Listener {

    private GameClient gameClient;

    ClientListener(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void connected(Connection connection) {
        Gdx.app.log("Client Connected", String.valueOf(connection.getID()));
        gameClient.onConnected(connection);
    }

    public void received(Connection connection, Object object) {
        Gdx.app.log("Client Received", String.valueOf(connection.getID()));
        gameClient.onReceived(connection, object);
    }

    public void disconnected(Connection connection) {
        Gdx.app.log("Client Disconnected", String.valueOf(connection.getID()));
        gameClient.onDisconnected(connection);
    }

}
