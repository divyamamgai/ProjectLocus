package com.locus.game.network;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ShortArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import com.locus.game.levels.Level;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.sprites.entities.Ship;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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
        kryo.register(LinkedHashMap.class);
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
        kryo.register(PlanetState.class);
        kryo.register(MoonState.class);
        kryo.register(Vector2.class);
        kryo.register(ShipState.class);
        kryo.register(CreateShip.class);
        kryo.register(StartGame.class);
        kryo.register(short[].class);
        kryo.register(ShortArray.class);
        kryo.register(Bullet.Type.class);
        kryo.register(BulletState.class);
        kryo.register(GameState.class);
        kryo.register(ControllerState.class);
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

        LinkedHashMap<Integer, Player> playerMap;

        UpdateLobby() {

        }

        UpdateLobby(LinkedHashMap<Integer, Player> playerMap) {
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

    static class CreateShip {

        int connectionID;
        Ship.Property property;
        ShipState shipState;

        CreateShip() {

        }

        CreateShip(int connectionID, Ship.Property property, ShipState shipState) {
            this.connectionID = connectionID;
            this.property = property;
            this.shipState = shipState;
        }

    }

    static class StartGame {

        ArrayList<CreateShip> createShipList;

        StartGame() {

        }

        StartGame(ArrayList<CreateShip> createShipList) {
            this.createShipList = createShipList;
        }

    }

    static class GameState {

        PlanetState planetState;
        ArrayList<MoonState> moonStateList;
        ArrayList<ShipState> shipStateList;
        ArrayList<BulletState> bulletAliveStateList;
        ShortArray bulletKilledArray;

        GameState() {

        }

    }

    static class ControllerState {

        boolean isThrustEnabled;
        boolean isThrustForward;
        boolean isRotationEnabled;
        boolean isRotationClockwise;
        boolean isFireEnabled;

        ControllerState() {

        }

    }

}
