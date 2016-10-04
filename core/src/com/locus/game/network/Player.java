package com.locus.game.network;

import com.locus.game.sprites.entities.Ship;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Player
 */

public class Player {

    public Ship.Property property;
    public boolean isReady;

    Player() {

    }

    Player(Ship.Property property, boolean isReady) {
        this.property = property;
        this.isReady = isReady;
    }

}
