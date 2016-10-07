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
import com.badlogic.gdx.utils.ShortArray;
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
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class ClientLevel {

    private static final float CAMERA_FOLLOW_SPEED = 4f;

    private OrthographicCamera camera;
    private ProjectLocus projectLocus;
    private HashMap<Short, ClientShip> shipAliveMap;
    private HashMap<Short, ClientShip> shipDeadMap;
    private ClientShip player;
    private ArrayList<ClientMoon> moonList;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private ClientPlanet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private InputController inputController;

    public EntityLoader getEntityLoader() {
        return projectLocus.entityLoader;
    }

    public BulletLoader getBulletLoader() {
        return projectLocus.bulletLoader;
    }

    public TextureRegion getBarBackgroundTexture() {
        return barBackgroundTexture;
    }

    public TextureRegion getBarForegroundTexture() {
        return barForegroundTexture;
    }

    public synchronized void setPlanetState(PlanetState planetState) {
        planet.update(planetState);
    }

    public synchronized void setMoonStateList(ArrayList<MoonState> moonStateList) {
        Iterator<MoonState> moonStateIterator = moonStateList.iterator();
        Iterator<ClientMoon> moonIterator = moonList.iterator();
        while (moonStateIterator.hasNext()) {
            moonIterator.next().update(moonStateIterator.next());
        }
    }

    public synchronized void setShipAliveStateList(ArrayList<ShipState> shipAliveStateList) {
        Iterator<ShipState> shipStateIterator = shipAliveStateList.iterator();
        ShipState shipState;
        Short ID;
        while (shipStateIterator.hasNext()) {
            shipState = shipStateIterator.next();
            ID = shipState.ID;
            if (shipAliveMap.containsKey(ID)) {
                shipAliveMap.get(ID).update(shipState);
            } else {
                shipAliveMap.put(ID, shipDeadMap.get(ID));
            }
        }
    }

    public synchronized void setShipKilledArray(ShortArray shipKilledArray) {
        Short ID;
        for (int i = 0; i < shipKilledArray.size; i++) {
            ID = shipKilledArray.get(i);
            shipDeadMap.put(ID, shipAliveMap.remove(ID));
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
        for (Moon.Property moonProperty : property.moonPropertyList) {
            moonList.add(new ClientMoon(this, moonProperty));
        }

        shipAliveMap = new HashMap<Short, ClientShip>(ProjectLocus.MAX_PLAYER_COUNT);
        shipDeadMap = new HashMap<Short, ClientShip>(ProjectLocus.MAX_PLAYER_COUNT);

        inputMultiplexer = new InputMultiplexer();
        inputController = new InputController(projectLocus.gameClient, false);
        inputMultiplexer.addProcessor(inputController);
        inputMultiplexer.addProcessor(new GestureDetector(inputController));

    }

    public synchronized void addShipAlive(Ship.Property shipProperty, ShipState shipState,
                                          boolean isPlayer) {

        ClientShip ship;
        Short ID = shipState.ID;

        if (shipDeadMap.containsKey(ID)) {
            ship = shipDeadMap.remove(ID);
            ship.resurrect(shipProperty.color, shipState);
        } else {
            ship = new ClientShip(this, shipProperty, shipState);
        }

        shipAliveMap.put(ID, ship);

        if (isPlayer) {
            player = ship;
        }

    }

    public void bindController() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public synchronized void update(float delta) {

        if (player != null) {
            inputController.update();
            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
        }

        planet.interpolate(delta);

        for (ClientMoon moon : moonList) {
            moon.interpolate(delta);
        }

        for (ClientShip ship : shipAliveMap.values()) {
            ship.interpolate(delta);
        }

    }

    public synchronized void render(SpriteBatch spriteBatch) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.draw(spriteBatch, camera.frustum);

        for (ClientMoon moon : moonList) {
            moon.draw(spriteBatch, camera.frustum);
        }

        for (ClientEntity entity : shipAliveMap.values()) {
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


