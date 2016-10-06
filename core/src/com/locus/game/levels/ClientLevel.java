package com.locus.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Queue;
import com.locus.game.ProjectLocus;
import com.locus.game.network.ShipState;
import com.locus.game.network.MoonState;
import com.locus.game.network.PlanetState;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.ClientEntity;
import com.locus.game.sprites.entities.ClientMoon;
import com.locus.game.sprites.entities.ClientPlanet;
import com.locus.game.sprites.entities.ClientShip;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class ClientLevel {

    private static final float CAMERA_FOLLOW_SPEED = 4f;

    private OrthographicCamera camera;
    private ProjectLocus projectLocus;
    private ArrayList<ClientShip> shipAliveList;
    private HashMap<Ship.Type, Queue<ClientShip>> shipDeadQueueMap;
    private ArrayList<ClientMoon> moonList;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private ClientPlanet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;

    private PlanetState planetState;
    private static boolean isPlanetStateToBeUpdated;
    private ArrayList<MoonState> moonStateList;
    private static boolean isMoonListStateToBeUpdated;
    private static int numberOfMoons;
    private ArrayList<ShipState> shipAliveStateList;
    private static boolean isShipAliveListStateToBeUpdated;

    public EntityLoader getEntityLoader() {
        return projectLocus.entityLoader;
    }

    public BulletLoader getBulletLoader() {
        return projectLocus.bulletLoader;
    }

    public TextureRegion getBarBackgroundTexture() {
        return barBackgroundTexture;
    }

    public void setBarBackgroundTexture(TextureRegion barBackgroundTexture) {
        this.barBackgroundTexture = barBackgroundTexture;
    }

    public TextureRegion getBarForegroundTexture() {
        return barForegroundTexture;
    }

    public void setBarForegroundTexture(TextureRegion barForegroundTexture) {
        this.barForegroundTexture = barForegroundTexture;
    }

    public void setPlanetState(PlanetState planetState) {
        if (!isPlanetStateToBeUpdated) {
            this.planetState = planetState;
            isPlanetStateToBeUpdated = true;
        }
    }

    public void setMoonStateList(ArrayList<MoonState> moonStateList) {
        if (!isMoonListStateToBeUpdated) {
            this.moonStateList = moonStateList;
            isMoonListStateToBeUpdated = true;
        }
    }

    public void setShipAliveStateList(ArrayList<ShipState> shipAliveStateList) {
        if (!isShipAliveListStateToBeUpdated) {
            this.shipAliveStateList = shipAliveStateList;
            isShipAliveListStateToBeUpdated = true;
        }
    }

    public ClientLevel(ProjectLocus projectLocus, Level.Property property) {

        this.projectLocus = projectLocus;

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        TiledMap tiledMap = projectLocus.tiledMapList.get(property.backgroundType);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        planet = new ClientPlanet(this, property.planetType);

        moonList = new ArrayList<ClientMoon>();
        numberOfMoons = property.moonPropertyList.size();
        for (int i = 0; i < numberOfMoons; i++) {
            moonList.add(new ClientMoon(this, property.moonPropertyList.get(i)));
        }

        shipAliveList = new ArrayList<ClientShip>();

        // We know that how many Type of Ships we have so we pass the capacity too.
        Ship.Type[] shipTypeArray = Ship.Type.values();
        shipDeadQueueMap = new HashMap<Ship.Type, Queue<ClientShip>>(shipTypeArray.length);
        // Initialize the shipDeadQueueMap with the available Ship Types.
        for (Ship.Type shipType : shipTypeArray) {
            shipDeadQueueMap.put(shipType, new Queue<ClientShip>());
        }

        isPlanetStateToBeUpdated = isMoonListStateToBeUpdated =
                isShipAliveListStateToBeUpdated = false;

        inputMultiplexer = new InputMultiplexer();
        InputController inputController = new InputController(new InputController.InputCallBack() {

            @Override
            public void applyThrust(boolean isForward) {

            }

            @Override
            public void applyRotation(boolean isClockwise) {

            }

            @Override
            public void fire() {

            }

        });
        inputMultiplexer.addProcessor(inputController);
        inputMultiplexer.addProcessor(new GestureDetector(inputController));

    }

    public void bindController() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void update(float delta) {

//        if (player != null && player.isAlive) {
//            inputController.update();
//            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
//            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
//            camera.update();
//        }

        if (isPlanetStateToBeUpdated) {
            planet.update(planetState);
            isPlanetStateToBeUpdated = false;
        }

        if (isMoonListStateToBeUpdated) {
            for (int i = 0; i < numberOfMoons; i++) {
                moonList.get(i).update(moonStateList.get(i));
            }
            isMoonListStateToBeUpdated = false;
        }

        if (isShipAliveListStateToBeUpdated) {
            for (int i = 0; i < shipAliveList.size(); i++) {
                shipAliveList.get(i).update(shipAliveStateList.get(i));
            }
            isShipAliveListStateToBeUpdated = false;
        }

    }

    public void render(SpriteBatch spriteBatch) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.draw(spriteBatch, camera.frustum);

        for (ClientMoon moon : moonList) {
            moon.draw(spriteBatch, camera.frustum);
        }

        for (ClientEntity entity : shipAliveList) {
            entity.draw(spriteBatch, camera.frustum);
        }

//        for (Bullet bullet : bulletAliveList) {
//            bullet.draw(spriteBatch, camera.frustum);
//        }

        spriteBatch.end();

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        camera.position.set(ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT, 0);
        camera.update();
    }

}


