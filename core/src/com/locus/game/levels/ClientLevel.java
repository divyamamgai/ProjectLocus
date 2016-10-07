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
import com.badlogic.gdx.utils.ShortArray;
import com.locus.game.ProjectLocus;
import com.locus.game.network.BulletState;
import com.locus.game.network.ShipState;
import com.locus.game.network.MoonState;
import com.locus.game.network.PlanetState;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.bullets.ClientBullet;
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
    private HashMap<Short, ClientShip> shipMap;
    private HashMap<Short, ClientBullet> bulletMap;
    private HashMap<Bullet.Type, Queue<ClientBullet>> bulletDeadQueueMap;
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

    public synchronized void setShipStateList(ArrayList<ShipState> shipStateList) {
        Iterator<ShipState> shipStateIterator = shipStateList.iterator();
        ShipState shipState;
        while (shipStateIterator.hasNext()) {
            shipState = shipStateIterator.next();
            shipMap.get(shipState.ID).update(shipState);
        }
    }

    public synchronized void setBulletAliveStateList(ArrayList<BulletState> bulletAliveStateList) {
        Iterator<BulletState> bulletStateIterator = bulletAliveStateList.iterator();
        BulletState bulletState;
        while (bulletStateIterator.hasNext()) {
            bulletState = bulletStateIterator.next();
            if (bulletMap.containsKey(bulletState.ID)) {
                bulletMap.get(bulletState.ID).update(bulletState);
            } else {
//                Queue<Bullet> bulletDeadQueue = bulletDeadQueueMap.get(bulletState.);
//                if (bulletDeadQueue.size > 0) {
//                    bullet = bulletDeadQueue.removeFirst();
//                    bullet.resurrect(ship, bulletPosition, angleRad);
//                } else {
//                    bullet = new Bullet(this, bulletType, ship, bulletPosition, angleRad);
//                }
            }
        }
    }

    public synchronized void setBulletKilledArray(ShortArray bulletKilledArray) {
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

        shipMap = new HashMap<Short, ClientShip>(ProjectLocus.MAX_PLAYER_COUNT);

        bulletMap = new HashMap<Short, ClientBullet>(ProjectLocus.MAX_BULLET_COUNT);

        // We know that how many Type of Bullets we have so we pass the capacity too.
        Bullet.Type[] bulletTypeArray = Bullet.Type.values();
        bulletDeadQueueMap = new HashMap<Bullet.Type, Queue<ClientBullet>>(bulletTypeArray.length);
        // Initialize the bulletDeadQueueMap with the available Bullet Types.
        for (Bullet.Type bulletType : bulletTypeArray) {
            bulletDeadQueueMap.put(bulletType, new Queue<ClientBullet>());
        }

        inputMultiplexer = new InputMultiplexer();
        inputController = new InputController(projectLocus.gameClient, false);
        inputMultiplexer.addProcessor(inputController);
        inputMultiplexer.addProcessor(new GestureDetector(inputController));

    }

    public synchronized void addShipAlive(Ship.Property shipProperty, ShipState shipState,
                                          boolean isPlayer) {

        ClientShip ship;

        ship = new ClientShip(this, shipProperty, shipState);

        shipMap.put(shipState.ID, ship);

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

        for (ClientShip ship : shipMap.values()) {
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

        for (ClientEntity entity : shipMap.values()) {
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


