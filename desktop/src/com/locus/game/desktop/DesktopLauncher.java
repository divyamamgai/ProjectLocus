package com.locus.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.locus.game.Main;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
        config.resizable = false;
        config.samples = 2;
        config.vSyncEnabled = true;
        config.foregroundFPS = 60;
        config.backgroundFPS = 60;
        new LwjglApplication(new Main(), config);
    }
}
