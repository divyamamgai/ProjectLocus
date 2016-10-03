package com.locus.game.network;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

/**
 * Created by Divya Mamgai on 10/2/2016.
 * Host Listener
 */

class HostListener extends Listener {

    private GameServer gameServer;

    HostListener(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void connected(Connection connection) {
        Gdx.app.log("Host Connected", String.valueOf(connection.getID()));
        gameServer.onConnected(connection);
    }

    public void received(Connection connection, Object object) {
        Gdx.app.log("Host Received", String.valueOf(connection.getID()));
        gameServer.onReceived(connection, object);
    }

    public void disconnected(Connection connection) {
        Gdx.app.log("Host Disconnected", String.valueOf(connection.getID()));
        gameServer.onDisconnected(connection);
    }

}
