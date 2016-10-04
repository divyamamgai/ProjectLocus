package com.locus.game.network;

import com.badlogic.gdx.graphics.Color;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.locus.game.levels.Level;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.sprites.entities.Ship;

import java.util.ArrayList;
import java.util.HashMap;

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
        kryo.register(Integer.class);
        kryo.register(HashMap.class);
        kryo.register(Player.class);
        kryo.register(String.class);
        kryo.register(PlayerJoinRequestRejected.class);
        kryo.register(PlayerReadyRequest.class);
        kryo.register(Error.class);
        kryo.register(Planet.class);
        kryo.register(Planet.Type.class);
        kryo.register(Moon.class);
        kryo.register(Moon.Type.class);
        kryo.register(Moon.Property.class);
        kryo.register(Level.class);
        kryo.register(Level.Property.class);
        kryo.register(LevelProperty.class);
        kryo.register(StartGame.class);
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

        HashMap<Integer, Player> playerMap;

        UpdateLobby() {

        }

        UpdateLobby(HashMap<Integer, Player> playerMap) {
            this.playerMap = playerMap;
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

    static class PlayerReadyRequest {

        boolean isReady;

        PlayerReadyRequest() {

        }

        PlayerReadyRequest(boolean isReady) {

            this.isReady = isReady;

        }

    }

    static class Error {

        String message;

        Error() {

        }

        Error(String message) {
            this.message = message;
        }

    }

    static class LevelProperty {

        Level.Property levelProperty;

        LevelProperty() {

        }

        LevelProperty(Level.Property levelProperty) {
            this.levelProperty = levelProperty;
        }

    }

    static class StartGame {

        long serverStartTime;
        float timeout;

        StartGame() {

        }

        StartGame(long serverStartTime, float timeout) {
            this.serverStartTime = serverStartTime;
            this.timeout = timeout;
        }

    }

}
