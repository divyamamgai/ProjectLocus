package com.locus.game.network;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.locus.game.sprites.entities.Ship;

import java.util.ArrayList;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Network
 */

class Network {

    static final int SERVER_TCP_PORT = 54556;
    static final int SERVER_UDP_PORT = 54778;
    static final int CONNECTION_TIMEOUT = 10000;

    static void registerClasses(EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(PlayerJoinRequest.class);
        kryo.register(Ship.class);
        kryo.register(Ship.Type.class);
        kryo.register(Color.class);
        kryo.register(Ship.Property.class);
        kryo.register(UpdateLobby.class);
        kryo.register(ArrayList.class);
        kryo.register(Player.class);
        kryo.register(PlayerJoinRequestRejected.class);
    }

    static class PlayerJoinRequest {

        Ship.Property property;

        PlayerJoinRequest() {

        }

        PlayerJoinRequest(Ship.Property property) {
            this.property = property;
        }

    }

    static class UpdateLobby {

        ArrayList<Player> playerList;

        UpdateLobby() {

        }

        UpdateLobby(ArrayList<Player> playerList) {
            this.playerList = playerList;
        }

    }

    static class PlayerJoinRequestRejected {

        String reason;

        PlayerJoinRequestRejected() {

        }

        PlayerJoinRequestRejected(String reason) {
            this.reason = reason;
        }

    }

}
