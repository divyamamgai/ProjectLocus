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
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Queue;
import com.locus.game.ProjectLocus;
import com.locus.game.network.ShipState;
import com.locus.game.network.MoonState;
import com.locus.game.network.PlanetState;
import com.locus.game.sprites.ClientCollisionDetector;
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
import com.locus.game.tools.Hud;
import com.locus.game.tools.InputController;
import com.locus.game.tools.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class ClientLevel {

    private static final float CAMERA_FOLLOW_SPEED = 4f;
    private static final Circle LEVEL_CIRCLE = new Circle(ProjectLocus.WORLD_HALF_WIDTH,
            ProjectLocus.WORLD_HALF_HEIGHT, 512f);

    private OrthographicCamera camera, foregroundCamera;
    private ProjectLocus projectLocus;
    private Hud hud;
    private World world;
    private HashMap<Short, ClientShip> shipMap;
    private ArrayList<ClientBullet> bulletAliveList;
    private final HashMap<Bullet.Type, Queue<ClientBullet>> bulletDeadQueueMap;
    private ClientShip player, followShip;
    private ArrayList<ClientMoon> moonList;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private ClientPlanet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private InputController inputController;

    private float followShipTimePassed;
    private ArrayList<ClientShip> followShipArray;
    private int followShipIndex;
    private TextureRegion redTransparentBackground;

    private float outOfLevelTimePassed;
    private short outOfLevelTimer;
    private boolean playerIsOutOfLevel, isPlayerDyingSoundPlaying;
    private Text messageText, countDownText;

    public ProjectLocus getProjectLocus() {
        return projectLocus;
    }

    public EntityLoader getEntityLoader() {
        return projectLocus.entityLoader;
    }

    public BulletLoader getBulletLoader() {
        return projectLocus.bulletLoader;
    }

    public World getWorld() {
        return world;
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
            if (shipMap.containsKey(shipState.ID)) {
                shipMap.get(shipState.ID).update(shipState);
            } else {
                Gdx.app.log("Client Level", "Does Not Contain Ship #" + shipState.ID);
            }
        }
        hud.update(shipStateList);
    }

    public synchronized ClientShip addShipAlive(Ship.Property shipProperty, ShipState shipState,
                                                boolean isPlayer) {

        ClientShip ship;

        ship = new ClientShip(this, shipProperty, shipState);

        shipMap.put(shipState.ID, ship);

        followShipArray.add(ship);

        hud.addPlayerData(shipState, isPlayer);

        if (isPlayer) {
            player = followShip = ship;
            return ship;
        }

        return null;

    }

    public synchronized void removeShipAlive(short shipID) {
        if (shipMap.containsKey(shipID)) {
            ClientShip ship = shipMap.remove(shipID);
            ship.killBody();
        }
    }

    public synchronized ClientShip getShipAlive(short ID) {
        return shipMap.get(ID);
    }

    public synchronized void addBulletAlive(Bullet.Type bulletType, ClientShip ship,
                                            Vector2 bulletPosition, float angleRad) {
        ClientBullet bullet;
        Queue<ClientBullet> bulletDeadQueue = bulletDeadQueueMap.get(bulletType);

        if (bulletDeadQueue.size > 0) {
            bullet = bulletDeadQueue.removeFirst();
            bullet.resurrect(ship, bulletPosition, angleRad);
        } else {
            bullet = new ClientBullet(this, bulletType, ship, bulletPosition, angleRad);
        }

        bulletAliveList.add(bullet);
    }

    public ClientLevel(ProjectLocus projectLocus, Hud hud, OrthographicCamera foregroundCamera,
                       Level.Property property) {

        this.projectLocus = projectLocus;
        this.hud = hud;
        this.foregroundCamera = foregroundCamera;

        outOfLevelTimer = 5;
        outOfLevelTimePassed = 0;
        playerIsOutOfLevel = false;

        messageText = new Text(projectLocus.font32, "Get Back To Planet Or Die In");
        countDownText = new Text(projectLocus.font72, "5");
        redTransparentBackground = projectLocus.uiTextureAtlas.
                findRegion("redTransparentBackground");
        isPlayerDyingSoundPlaying = false;

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        TiledMap tiledMap = projectLocus.tiledMapList.get(property.backgroundType);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        world = new World(ProjectLocus.GRAVITY, true);
        world.setContactListener(new ClientCollisionDetector());

        bulletAliveList = new ArrayList<ClientBullet>();

        // We know that how many Type of Bullets we have so we pass the capacity too.
        Bullet.Type[] bulletTypeArray = Bullet.Type.values();
        bulletDeadQueueMap = new HashMap<Bullet.Type, Queue<ClientBullet>>(bulletTypeArray.length);
        // Initialize the bulletDeadQueueMap with the available Bullet Types.
        for (Bullet.Type bulletType : bulletTypeArray) {
            bulletDeadQueueMap.put(bulletType, new Queue<ClientBullet>());
        }

        planet = new ClientPlanet(this, property.planetType);

        moonList = new ArrayList<ClientMoon>();
        for (Moon.Property moonProperty : property.moonPropertyList) {
            moonList.add(new ClientMoon(this, moonProperty));
        }

        shipMap = new HashMap<Short, ClientShip>(ProjectLocus.MAX_PLAYER_COUNT);

        inputMultiplexer = new InputMultiplexer();
        inputController = new InputController(projectLocus.gameClient, false);
        inputMultiplexer.addProcessor(inputController);
        inputMultiplexer.addProcessor(new GestureDetector(inputController));

        followShipArray = new ArrayList<ClientShip>();
        followShipTimePassed = 15f;
        followShipIndex = 0;

    }

    public void onShow() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public synchronized void update(float delta) {

        if (player != null && player.isAlive()) {
            inputController.update();
            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
            if (!LEVEL_CIRCLE.contains(player.getBodyPosition())) {
                playerIsOutOfLevel = true;
                outOfLevelTimePassed += delta;
                if (outOfLevelTimePassed >= 1f && !isPlayerDyingSoundPlaying) {
                    try {
                        projectLocus.dyingSound.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isPlayerDyingSoundPlaying = true;
                }
                if (outOfLevelTimePassed >= 1f) {
                    if (--outOfLevelTimer <= 0) {
                        // Kill the player.
                        player.kill();
                        projectLocus.gameClient.sendShipKill(player.getID());
                        playerIsOutOfLevel = false;
                    }
                    countDownText.setTextFast(String.valueOf(outOfLevelTimer));
                    outOfLevelTimePassed = 0;
                }
            } else {
                playerIsOutOfLevel = false;
                outOfLevelTimer = 5;
                outOfLevelTimePassed = 0;
                isPlayerDyingSoundPlaying = false;
                try {
                    projectLocus.dyingSound.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownText.setTextFast("5");
            }
        } else {
            camera.zoom = 1.5f;
            camera.position.x += (followShip.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (followShip.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
            followShipTimePassed += delta;
            if (followShipTimePassed >= 15f) {
                while (followShipArray.get(followShipIndex).isAlive()) {
                    followShip = followShipArray.get(followShipIndex);
                    followShipIndex++;
                    if (followShipIndex == shipMap.size()) {
                        followShipIndex = 0;
                    }
                }
                followShipTimePassed = 0;
            }
        }

        planet.interpolate(delta);

        for (ClientMoon moon : moonList) {
            moon.interpolate(delta);
        }

        for (ClientShip ship : shipMap.values()) {
            ship.interpolate(delta);
        }

        Iterator<ClientBullet> bulletIterator = bulletAliveList.iterator();
        while (bulletIterator.hasNext()) {
            ClientBullet bullet = bulletIterator.next();
            if (bullet.isAlive()) {
                bullet.update();
            } else {
                bullet.killBody();
                bulletDeadQueueMap.get(bullet.getType()).addLast(bullet);
                bulletIterator.remove();
            }
        }

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

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
            if (entity.isAlive()) {
                entity.draw(spriteBatch, camera.frustum);
            }
        }

        for (ClientBullet bullet : bulletAliveList) {
            bullet.draw(spriteBatch, camera.frustum);
        }

        spriteBatch.end();

        if (playerIsOutOfLevel) {
            spriteBatch.setProjectionMatrix(foregroundCamera.combined);
            spriteBatch.begin();
            spriteBatch.draw(redTransparentBackground,
                    0, 0,
                    ProjectLocus.screenCameraWidth, ProjectLocus.screenCameraHeight);
            messageText.draw(spriteBatch);
            countDownText.draw(spriteBatch);
            spriteBatch.end();
        }

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        camera.position.set(player.getX(), player.getY(), 0);
        messageText.setPosition(
                ProjectLocus.screenCameraHalfWidth - messageText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + countDownText.getHeight() - 24);
        camera.update();
        countDownText.setPosition(
                ProjectLocus.screenCameraHalfWidth - countDownText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight - countDownText.getHalfHeight());
    }

}