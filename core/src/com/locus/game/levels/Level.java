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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Queue;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class Level implements Disposable {

    private static final float CAMERA_FOLLOW_SPEED = 4f;

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

    }

    private InputController inputController;
    private OrthographicCamera camera;
    private ProjectLocus projectLocus;
    private World world;
    private ArrayList<Bullet> bulletAliveList;
    private HashMap<Bullet.Type, Queue<Bullet>> bulletDeadQueueMap;
    private ArrayList<Ship> shipAliveList;
    private HashMap<Ship.Type, Queue<Ship>> shipDeadQueueMap;
    private ArrayList<Moon> moonList;
    private TextureRegion barBackgroundTexture, barForegroundTexture;
    private Ship player;
    private Planet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;
    private Level.Property property;

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

    public Level(ProjectLocus projectLocus, Property property) {

        this.projectLocus = projectLocus;
        this.property = property;

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth,
                ProjectLocus.worldCameraHeight);

        // Box2D Variables
        world = new World(ProjectLocus.GRAVITY, true);
        world.setContactListener(new CollisionDetector());

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

        shipAliveList = new ArrayList<Ship>();

        // We know that how many Type of Ships we have so we pass the capacity too.
        Ship.Type[] shipTypeArray = Ship.Type.values();
        shipDeadQueueMap = new HashMap<Ship.Type, Queue<Ship>>(shipTypeArray.length);
        // Initialize the shipDeadQueueMap with the available Ship Types.
        for (Ship.Type shipType : shipTypeArray) {
            shipDeadQueueMap.put(shipType, new Queue<Ship>());
        }

        planet = new Planet(this, property.planetType, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);

        moonList = new ArrayList<Moon>();
        for (Moon.Property moonProperty : property.moonPropertyList) {
            moonList.add(new Moon(this, ProjectLocus.WORLD_HALF_WIDTH,
                    ProjectLocus.WORLD_HALF_HEIGHT, moonProperty));
        }

        inputMultiplexer = new InputMultiplexer();

    }

    public void bindController() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public Ship getShipAlive(int shipIndex) {
        return shipAliveList.get(shipIndex);
    }

    public int addShipAlive(Ship.Property shipProperty, float x, float y, float angleRad,
                            boolean isPlayer) {

        Ship ship;
        Queue<Ship> shipDeadQueue = shipDeadQueueMap.get(shipProperty.type);

        if (shipDeadQueue.size > 0) {
            ship = shipDeadQueue.removeFirst();
            ship.resurrect(shipProperty.color, x, y, angleRad);
        } else {
            ship = new Ship(this, shipProperty, x, y, angleRad);
        }

        shipAliveList.add(ship);

        if (isPlayer) {
            inputController = new InputController(player = ship);
            inputMultiplexer.clear();
            inputMultiplexer.addProcessor(inputController);
            inputMultiplexer.addProcessor(new GestureDetector(inputController));
        }

        // Return it's index for later use if needed.
        return (shipAliveList.size() - 1);

    }

    public void removeShipAlive(int shipIndex) {
        if (shipIndex >= 0 && shipIndex < shipAliveList.size()) {
            shipAliveList.remove(shipIndex);
        }
    }

    public void addBulletAlive(Bullet.Type bulletType, Ship ship, Vector2 bulletPosition,
                               float angleRad) {
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

    public void update(float delta) {

        if (player != null && player.isAlive()) {
            inputController.update();
            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
        }

        planet.update();

        for (Moon moon : moonList) {
            moon.update();
        }

        Iterator<Ship> shipIterator = shipAliveList.iterator();
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
                shipDeadQueueMap.get(ship.getType()).addLast(ship);
                shipIterator.remove();
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

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

    }

    public void render(SpriteBatch spriteBatch) {

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.draw(spriteBatch, camera.frustum);

        for (Moon moon : moonList) {
            moon.draw(spriteBatch, camera.frustum);
        }

        for (Entity entity : shipAliveList) {
            entity.draw(spriteBatch, camera.frustum);
        }

        for (Bullet bullet : bulletAliveList) {
            bullet.draw(spriteBatch, camera.frustum);
        }

        spriteBatch.end();

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        camera.position.set(ProjectLocus.WORLD_HALF_WIDTH, ProjectLocus.WORLD_HALF_HEIGHT, 0);
        camera.update();
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
