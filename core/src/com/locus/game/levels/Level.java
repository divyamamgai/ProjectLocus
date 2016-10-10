package com.locus.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Queue;
import com.locus.game.ProjectLocus;
import com.locus.game.network.MoonState;
import com.locus.game.network.PlanetState;
import com.locus.game.network.ShipState;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.Asteroid;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
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
public class Level implements Disposable {

    private static final float CAMERA_FOLLOW_SPEED = 4f;
    public static final Circle LEVEL_CIRCLE = new Circle(ProjectLocus.WORLD_HALF_WIDTH,
            ProjectLocus.WORLD_HALF_HEIGHT, 512f);

    public static class Property {

        Planet.Type planetType;
        ArrayList<Moon.Property> moonPropertyList;
        int backgroundType;

        public Property() {
        }

        public Property(Planet.Type planetType, ArrayList<Moon.Property> moonPropertyList,
                        int backgroundType) {
            this.planetType = planetType;
            this.moonPropertyList = moonPropertyList;
            this.backgroundType = backgroundType;
        }

        public static Level.Property generateRandom() {
            Level.Property property = new Level.Property();
            Planet.Type[] planetTypeArray = Planet.Type.values();
            property.planetType = planetTypeArray[MathUtils.random(0, planetTypeArray.length - 1)];
            Moon.Type[] moonTypeArray = Moon.Type.values();
            float[] moonRadiusArray = {200f, 250f, 300f, 350f, 400f};
            int numberOfMoons = MathUtils.random(2, 4), moonRadiusIndex;
            property.moonPropertyList = new ArrayList<Moon.Property>();
            ArrayList<Integer> moonRadiusTakenList = new ArrayList<Integer>();
            for (int i = 0; i < numberOfMoons; i++) {
                moonRadiusIndex = MathUtils.random(0, 4);
                while (moonRadiusTakenList.contains(moonRadiusIndex)) {
                    moonRadiusIndex = MathUtils.random(0, 4);
                }
                moonRadiusTakenList.add(moonRadiusIndex);
                property.moonPropertyList.add(
                        new Moon.Property(
                                moonTypeArray[MathUtils.random(0, moonTypeArray.length - 1)],
                                moonRadiusArray[moonRadiusIndex],
                                i * 90f));
            }
            property.backgroundType = MathUtils.random(0, 7);
            return property;
        }

    }

    private OrthographicCamera camera, foregroundCamera;
    private ProjectLocus projectLocus;
    private Hud hud;
    private Level.Property property;
    private boolean isMultiPlayer;
    private World world;
    private ArrayList<Bullet> bulletAliveList;
    private HashMap<Bullet.Type, Queue<Bullet>> bulletDeadQueueMap;
    private ArrayList<Asteroid> asteroidAliveList;
    private HashMap<Asteroid.Type, Queue<Asteroid>> asteroidDeadQueueMap;
    private HashMap<Short, Ship> shipAliveMap;
    private HashMap<Ship.Type, Queue<Ship>> shipDeadQueueMap;
    private ArrayList<Moon> moonList;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private Ship player, followShip;
    private Planet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private InputController inputController;

    private PlanetState planetState;
    private ArrayList<MoonState> moonStateList;
    private ArrayList<ShipState> shipStateList;

    private float followShipTimePassed;
    private ArrayList<Ship> followShipArray;
    private int followShipIndex;

    private float outOfLevelTimePassed;
    private short outOfLevelTimer;
    private boolean playerIsOutOfLevel, isPlayerDyingSoundPlaying;
    private Text messageText, countDownText;
    private Sprite redTransparentSprite;
    private boolean isRedTransparentAlphaIncreasing;

    private short alivePlayerCount;

    private float gameStateUpdateTimePassed;

//    private Box2DDebugRenderer box2DDebugRenderer;

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

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public PlanetState getPlanetState() {
        return planetState;
    }

    public ArrayList<MoonState> getMoonStateList() {
        return moonStateList;
    }

    public ArrayList<ShipState> getShipStateList() {
        return shipStateList;
    }

    public InputMultiplexer getInputMultiplexer() {
        return inputMultiplexer;
    }

    public short getAlivePlayerCount() {
        return alivePlayerCount;
    }

    public Level(ProjectLocus projectLocus, Hud hud, OrthographicCamera foregroundCamera,
                 Property property, boolean isMultiPlayer) {

        // Reset Entity Count
        Entity.EntityCount = 0;

        this.projectLocus = projectLocus;
        this.hud = hud;
        this.foregroundCamera = foregroundCamera;
        this.property = property;
        this.isMultiPlayer = isMultiPlayer;

        outOfLevelTimer = 5;
        alivePlayerCount = 0;
        outOfLevelTimePassed = gameStateUpdateTimePassed = 0;
        playerIsOutOfLevel = false;

        messageText = new Text(projectLocus.font32, "Get Back To Planet Or Die In");
        countDownText = new Text(projectLocus.font72, "5");
        redTransparentSprite = projectLocus.uiTextureAtlas.createSprite("redTransparentBackground");
        redTransparentSprite.setAlpha(0);
        isRedTransparentAlphaIncreasing = true;
        isPlayerDyingSoundPlaying = false;

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        // Box2D Variables
        world = new World(ProjectLocus.GRAVITY, true);
        world.setContactListener(new CollisionDetector());
//        box2DDebugRenderer = new Box2DDebugRenderer();

        TiledMap tiledMap = projectLocus.tiledMapList.get(property.backgroundType);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        bulletAliveList = new ArrayList<Bullet>();

        // We know that how many Type of Bullets we have so we pass the capacity too.
        Bullet.Type[] bulletTypeArray = Bullet.Type.values();
        bulletDeadQueueMap = new HashMap<Bullet.Type, Queue<Bullet>>(bulletTypeArray.length);
        // Initialize the bulletDeadQueueMap with the available Bullet Types.
        for (Bullet.Type bulletType : bulletTypeArray) {
            bulletDeadQueueMap.put(bulletType, new Queue<Bullet>());
        }

        asteroidAliveList = new ArrayList<Asteroid>();

        // We know that how many Type of Asteroids we have so we pass the capacity too.
        Asteroid.Type[] asteroidTypeArray = Asteroid.Type.values();
        asteroidDeadQueueMap = new HashMap<Asteroid.Type, Queue<Asteroid>>(asteroidTypeArray.length);
        // Initialize the asteroidDeadQueueMap with the available Asteroid Types.
        for (Asteroid.Type asteroidType : asteroidTypeArray) {
            asteroidDeadQueueMap.put(asteroidType, new Queue<Asteroid>());
        }

        shipAliveMap = new HashMap<Short, Ship>();
        shipStateList = new ArrayList<ShipState>();

        // We know that how many Type of Ships we have so we pass the capacity too.
        Ship.Type[] shipTypeArray = Ship.Type.values();
        shipDeadQueueMap = new HashMap<Ship.Type, Queue<Ship>>(shipTypeArray.length);
        // Initialize the shipDeadQueueMap with the available Ship Types.
        for (Ship.Type shipType : shipTypeArray) {
            shipDeadQueueMap.put(shipType, new Queue<Ship>());
        }

        planet = new Planet(this, property.planetType, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);
        planetState = planet.getPlanetState();

        moonList = new ArrayList<Moon>();
        moonStateList = new ArrayList<MoonState>();
        Moon moon;
        for (Moon.Property moonProperty : property.moonPropertyList) {
            moonList.add((moon = new Moon(this, moonProperty)));
            moonStateList.add(moon.getMoonState());
        }

        inputMultiplexer = new InputMultiplexer();
        if (isMultiPlayer) {
            inputController = new InputController(projectLocus.gameServer, true);
            inputMultiplexer.addProcessor(inputController);
            inputMultiplexer.addProcessor(new GestureDetector(inputController));
        }

        followShipArray = new ArrayList<Ship>();
        followShipTimePassed = 15f;
        followShipIndex = 0;

    }

    public void onShow() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public Ship getShipAlive(short shipID) {
        return shipAliveMap.get(shipID);
    }

    public synchronized short addShipAlive(Ship.Property shipProperty, float x, float y, float angleRad,
                                           boolean isPlayer) {

        Ship ship;
        Queue<Ship> shipDeadQueue = shipDeadQueueMap.get(shipProperty.type);

        if (shipDeadQueue.size > 0) {
            ship = shipDeadQueue.removeFirst();
            ship.resurrect(shipProperty.color, x, y, angleRad);
        } else {
            ship = new Ship(this, shipProperty, x, y, angleRad);
        }

        shipAliveMap.put(ship.getID(), ship);
        shipStateList.add(ship.getShipState());
        followShipArray.add(ship);

        if (isMultiPlayer) {
            hud.addPlayerData(ship.getShipState(), isPlayer);
        }

        if (isPlayer) {
            player = followShip = ship;
            // If it's single player we need to bind controls to the Player's Ship.
            if (!isMultiPlayer) {
                inputController = new InputController(ship, true);
                inputMultiplexer.clear();
                inputMultiplexer.addProcessor(inputController);
                inputMultiplexer.addProcessor(new GestureDetector(inputController));
            }
        }

        alivePlayerCount++;

        // Return it's index for later use if needed.
        return ship.getID();

    }

    public synchronized Ship removeShipAlive(short shipID) {
        if (shipAliveMap.containsKey(shipID)) {
            Ship ship = shipAliveMap.remove(shipID);
            shipStateList.remove(ship.getShipState());
            alivePlayerCount--;
            return ship;
        } else {
            return null;
        }
    }

    public synchronized void addBulletAlive(Bullet.Type bulletType, Ship ship,
                                            Vector2 bulletPosition, float angleRad) {
        Bullet bullet;
        Queue<Bullet> bulletDeadQueue = bulletDeadQueueMap.get(bulletType);

        if (bulletDeadQueue.size > 0) {
            bullet = bulletDeadQueue.removeFirst();
            bullet.resurrect(ship, bulletPosition, angleRad);
        } else {
            bullet = new Bullet(this, bulletType, ship, bulletPosition, angleRad);
        }

        bulletAliveList.add(bullet);
    }

    public synchronized void addAsteroidAlive(Asteroid.Type asteroidType,
                                              Asteroid.Property asteroidProperty) {
        Asteroid asteroid;
        Queue<Asteroid> asteroidDeadQueue = asteroidDeadQueueMap.get(asteroidType);

        if (asteroidDeadQueue.size > 0) {
            asteroid = asteroidDeadQueue.removeFirst();
            asteroid.resurrect(asteroidProperty);
        } else {
            asteroid = new Asteroid(this, asteroidType, asteroidProperty);
        }

        asteroidAliveList.add(asteroid);
    }

    public synchronized void update(float delta) {

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

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
                        projectLocus.dyingMusic.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isPlayerDyingSoundPlaying = true;
                }
                if (outOfLevelTimePassed >= 1f) {
                    if (--outOfLevelTimer <= 0) {
                        // Kill the player.
                        player.kill();
                        player.killBody();
                        shipDeadQueueMap.get(player.getShipType()).addLast(player);
                        player = null;
                        alivePlayerCount--;
                        if (!isMultiPlayer) {
                            projectLocus.setScreen(projectLocus.mainMenuScreen);
                        }
                        playerIsOutOfLevel = false;
                    }
                    countDownText.setTextFast(String.valueOf(outOfLevelTimer));
                    outOfLevelTimePassed = 0;
                    redTransparentSprite.setAlpha(isRedTransparentAlphaIncreasing ? 1 : 0);
                    isRedTransparentAlphaIncreasing = !isRedTransparentAlphaIncreasing;
                } else {
                    redTransparentSprite.setAlpha(
                            isRedTransparentAlphaIncreasing ? outOfLevelTimePassed :
                                    (1 - outOfLevelTimePassed));
                }
            } else {
                playerIsOutOfLevel = false;
                outOfLevelTimer = 5;
                outOfLevelTimePassed = 0;
                isPlayerDyingSoundPlaying = false;
                try {
                    projectLocus.dyingMusic.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                countDownText.setTextFast("5");
            }
        } else if (isMultiPlayer) {
            camera.zoom = 1.5f;
            camera.position.x += (followShip.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (followShip.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
            followShipTimePassed += delta;
            if (followShipTimePassed >= 15f) {
                while (!followShipArray.get(followShipIndex).isAlive()) {
                    followShipIndex++;
                    if (followShipIndex == followShipArray.size()) {
                        followShipIndex = 0;
                    }
                    followShip = followShipArray.get(followShipIndex);
                }
                followShipTimePassed = 0;
            }
        }

        planet.update();

        for (Moon moon : moonList) {
            moon.update();
        }

        Iterator<Ship> shipIterator = shipAliveMap.values().iterator();
        while (shipIterator.hasNext()) {
            Ship ship = shipIterator.next();
            if (ship.isAlive()) {
                ship.update();
                planet.applyGravitationalForce(ship);
                for (Moon moon : moonList) {
                    moon.applyGravitationalForce(ship);
                }
            } else {
                ship.killBody();
                shipDeadQueueMap.get(ship.getShipType()).addLast(ship);
                shipIterator.remove();
                alivePlayerCount--;
            }
        }

        Iterator<Bullet> bulletIterator = bulletAliveList.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (bullet.isAlive()) {
                bullet.update();
            } else {
                bullet.killBody();
                bulletDeadQueueMap.get(bullet.getType()).addLast(bullet);
                bulletIterator.remove();
            }
        }

        Iterator<Asteroid> asteroidIterator = asteroidAliveList.iterator();
        while (asteroidIterator.hasNext()) {
            Asteroid asteroid = asteroidIterator.next();
            if (asteroid.isAlive()) {
                asteroid.update();
            } else {
                asteroid.killBody();
                asteroidDeadQueueMap.get(asteroid.getAsteroidType()).addLast(asteroid);
                asteroidIterator.remove();
            }
        }

        if (isMultiPlayer) {
            gameStateUpdateTimePassed += delta;
            if (gameStateUpdateTimePassed >= 0.0035f) {
                projectLocus.gameServer.sendGameState();
                gameStateUpdateTimePassed = 0;
            }
        }

    }

    public synchronized void render(SpriteBatch spriteBatch) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.draw(spriteBatch, camera.frustum);

        for (Moon moon : moonList) {
            moon.draw(spriteBatch, camera.frustum);
        }

        for (Entity entity : shipAliveMap.values()) {
            entity.draw(spriteBatch, camera.frustum);
        }

        for (Bullet bullet : bulletAliveList) {
            bullet.draw(spriteBatch, camera.frustum);
        }

        for (Asteroid asteroid : asteroidAliveList) {
            asteroid.draw(spriteBatch, camera.frustum);
        }

        spriteBatch.end();

        if (playerIsOutOfLevel) {
            spriteBatch.setProjectionMatrix(foregroundCamera.combined);
            spriteBatch.begin();
            redTransparentSprite.draw(spriteBatch);
            messageText.draw(spriteBatch);
            countDownText.draw(spriteBatch);
            spriteBatch.end();
        }

//        box2DDebugRenderer.render(world, camera.combined);

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
        messageText.setPosition(
                ProjectLocus.screenCameraHalfWidth - messageText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight + countDownText.getHeight() - 24);
        countDownText.setPosition(
                ProjectLocus.screenCameraHalfWidth - countDownText.getHalfWidth(),
                ProjectLocus.screenCameraHalfHeight - countDownText.getHalfHeight());
        redTransparentSprite.setPosition(0, 0);
        redTransparentSprite.setSize(ProjectLocus.screenCameraWidth,
                ProjectLocus.screenCameraHeight);
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
