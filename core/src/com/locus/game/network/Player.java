package com.locus.game.network;

import com.locus.game.sprites.entities.Ship;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Player
 */

public class Player {

    int connectionID;
    Ship.Property property;
    boolean isReady;

    Player() {

    }

    Player(int connectionID, Ship.Property property, boolean isReady) {
        this.connectionID = connectionID;
        this.property = property;
        this.isReady = isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

}
