package com.locus.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.locus.game.ProjectLocus;

public class DesktopLauncher {
    public static void main(String[] arg) {

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());

//        config.fullscreen = false;
//        config.width = 854;
//        config.height = 480;
//        config.resizable = false;

        new LwjglApplication(new ProjectLocus(), config);

    }
}
