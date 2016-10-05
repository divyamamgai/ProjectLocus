package com.locus.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.locus.game.ProjectLocus;
import com.locus.game.network.Player;
import com.locus.game.network.ShipState;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.entities.Entity;
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

//    private static final Circle LEVEL_CIRCLE = new Circle(ProjectLocus.WORLD_HALF_WIDTH,
//            ProjectLocus.WORLD_HALF_HEIGHT, 512f);

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

    public ProjectLocus projectLocus;
    public World world;
    public ArrayList<Bullet> bulletAliveList;
    public ArrayList<Bullet> bulletDeadList;
    private Iterator<Bullet> bulletIterator;
    private ArrayList<Entity> entityAliveList;
    private ArrayList<Entity> entityDeadList;
    private Iterator<Entity> entityIterator;
    private ArrayList<Moon> moonList;
    public TextureRegion barBackgroundTexture, barForegroundTexture;

    private Ship player;
    private Planet planet;
    private TiledMapRenderer tiledMapRenderer;
    private InputMultiplexer inputMultiplexer;

    public Level.Property property;

    // Debugging
    private Box2DDebugRenderer box2DDebugRenderer;

    private HashMap<Integer, Ship> shipHashMap;

    public Level(ProjectLocus projectLocus, Property property) {

        this.projectLocus = projectLocus;
        this.property = property;

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);

        // Box2D Variables
        world = new World(ProjectLocus.GRAVITY, true);
        world.setContactListener(new CollisionDetector());

        bulletAliveList = new ArrayList<Bullet>();
        bulletDeadList = new ArrayList<Bullet>();
        entityAliveList = new ArrayList<Entity>();
        entityDeadList = new ArrayList<Entity>();

        planet = new Planet(this, property.planetType, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);

        moonList = new ArrayList<Moon>();
        for (Moon.Property moonProperty : property.moonPropertyList) {
            moonList.add(new Moon(this, ProjectLocus.WORLD_HALF_WIDTH,
                    ProjectLocus.WORLD_HALF_HEIGHT, moonProperty));
        }

        TiledMap tiledMap = projectLocus.tiledMapList.get(property.backgroundType);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        // Debugging
        box2DDebugRenderer = new Box2DDebugRenderer();

        shipHashMap = new HashMap<Integer, Ship>();

    }

    public void bindController() {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public void addShip(ShipState shipState, Ship.Property property, int connectionID,
                        boolean isPlayer, boolean isHost) {
        Ship ship = new Ship(this, property, shipState.position.x, shipState.position.y,
                shipState.angleRad);
        if (isPlayer) {
            Gdx.app.log("Player Is Host ", String.valueOf(isHost));
            player = ship;
            inputController = new InputController(projectLocus, player, isHost, shipState);
            inputMultiplexer = new InputMultiplexer();
            inputMultiplexer.addProcessor(inputController);
            inputMultiplexer.addProcessor(new GestureDetector(inputController));
        }
        entityAliveList.add(ship);
        shipHashMap.put(connectionID, ship);
    }

    public void updateShip(ShipState shipState, int connectionID) {
        Ship ship = shipHashMap.get(connectionID);
        if (shipState.isRotationEnabled) {
            ship.applyRotation(shipState.rotationDirection);
        }
        if (shipState.isThrustEnabled) {
            ship.applyThrust(shipState.thrustDirection);
        }
        if (shipState.isFireEnabled) {
            ship.fire();
        }
    }

    public void update(float delta) {

        if (player != null && player.isAlive) {
            inputController.update();
            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
        }

        planet.update();

        for (Moon moon : moonList) {
            moon.update();
        }

        entityIterator = entityAliveList.iterator();
        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            if (entity.isAlive) {
                entity.update();
                planet.applyGravitationalForce(entity);
                for (Moon moon : moonList) {
                    moon.applyGravitationalForce(entity);
                }
            } else {
                entity.killBody();
                entityDeadList.add(entity);
                entityIterator.remove();
            }
        }

        bulletIterator = bulletAliveList.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (bullet.isAlive) {
                bullet.update();
            } else {
                bullet.killBody();
                bulletDeadList.add(bullet);
                bulletIterator.remove();
            }
        }

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

//        Gdx.app.log("Bullet Count", String.valueOf(bulletAliveList.size() + bulletDeadList.size()));

    }

    public void render(SpriteBatch spriteBatch) {

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.draw(spriteBatch, camera.frustum);

        for (Moon moon : moonList) {
            moon.draw(spriteBatch, camera.frustum);
        }

        for (Entity entity : entityAliveList) {
            entity.draw(spriteBatch, camera.frustum);
        }

        for (Bullet bullet : bulletAliveList) {
            bullet.draw(spriteBatch, camera.frustum);
        }

        spriteBatch.end();

        // Debugging
//        box2DDebugRenderer.render(world, camera.combined);

    }

    public void render(SpriteBatch spriteBatch, float delta) {

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

        if (player != null && player.isAlive) {
            inputController.update();
            camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
            camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;
            camera.update();
        }

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();

        planet.update();
        planet.draw(spriteBatch, camera.frustum);

        for (Moon moon : moonList) {
            moon.update();
            moon.draw(spriteBatch, camera.frustum);
        }

        entityIterator = entityAliveList.iterator();
        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            if (entity.isAlive) {
                entity.update();
                planet.applyGravitationalForce(entity);
                for (Moon moon : moonList) {
                    moon.applyGravitationalForce(entity);
                }
                entity.draw(spriteBatch);
            } else {
                entityDeadList.add(entity);
                entityIterator.remove();
            }
        }

        bulletIterator = bulletAliveList.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (bullet.isAlive) {
                bullet.update();
                bullet.draw(spriteBatch);
            } else {
                bullet.killBody();
                bulletDeadList.add(bullet);
                bulletIterator.remove();
            }
        }

        spriteBatch.end();

        // Debugging
//        box2DDebugRenderer.render(world, camera.combined);

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
    }

//    public boolean isInLevel(Vector2 positionUI) {
//        return LEVEL_CIRCLE.contains(positionUI);
//    }

    @Override
    public void dispose() {
        world.dispose();
        box2DDebugRenderer.dispose();
    }
}
