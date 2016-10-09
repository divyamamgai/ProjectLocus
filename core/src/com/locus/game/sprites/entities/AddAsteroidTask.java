package com.locus.game.sprites.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Timer;
import com.locus.game.levels.Level;

/**
 * Created by Divya Mamgai on 10/9/2016.
 * Add Asteroid Task
 */

public class AddAsteroidTask extends Timer.Task {

    private static Asteroid.Type[] asteroidTypeArray = Asteroid.Type.values();

    private Timer timer;
    Level level;

    public AddAsteroidTask(Timer timer, Level level) {
        this.timer = timer;
        this.level = level;
    }

    @Override
    public void run() {
        short count = 30;
        while (count-- > 0) {
            level.addAsteroidAlive(
                    asteroidTypeArray[MathUtils.random(0, asteroidTypeArray.length - 1)],
                    Asteroid.Property.generateRandom());
        }
        timer.scheduleTask(new AddAsteroidTask(timer, level), 15f);
    }

}
