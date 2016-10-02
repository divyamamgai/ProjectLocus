package com.locus.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Timer;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class Level implements Disposable {

//    private static final Circle LEVEL_CIRCLE = new Circle(ProjectLocus.WORLD_HALF_WIDTH,
//            ProjectLocus.WORLD_HALF_HEIGHT, 512f);

    private static final float CAMERA_FOLLOW_SPEED = 3f;

    private InputController inputController;
    private OrthographicCamera camera;

    public ProjectLocus projectLocus;
    public Timer timer;
    public World world;
    public ArrayList<Bullet> bulletList;
    public Stack<Bullet> destroyBulletStack;
    public ArrayList<Entity> entityList;
    public Stack<Entity> destroyEntityStack;
    public ArrayList<Moon> moonList;
    public TextureRegion barBackgroundTexture, barForegroundTexture;

    private Ship player;
    private Planet planet;
    private TiledMapRenderer tiledMapRenderer;

    // Debugging
    private Box2DDebugRenderer box2DDebugRenderer;
    private OrthographicCamera fpsCamera;
    private BitmapFont fpsFont;

    public Level(ProjectLocus projectLocus, Planet.Type planetType,
                 ArrayList<Moon.Property> moonPropertyList, int backgroundType) {

        this.projectLocus = projectLocus;

        timer = new Timer();

        camera = new OrthographicCamera(ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);

        // Box2D Variables
        world = new World(ProjectLocus.GRAVITY, true);
        world.setContactListener(new CollisionDetector());

        bulletList = new ArrayList<Bullet>();
        destroyBulletStack = new Stack<Bullet>();
        entityList = new ArrayList<Entity>();
        destroyEntityStack = new Stack<Entity>();

        entityList.add(player = new Ship(this, projectLocus.playerShipProperty,
                ProjectLocus.WORLD_HALF_WIDTH + 250f, ProjectLocus.WORLD_HALF_HEIGHT));

        planet = new Planet(this, planetType, ProjectLocus.WORLD_HALF_WIDTH,
                ProjectLocus.WORLD_HALF_HEIGHT);

        moonList = new ArrayList<Moon>();
        for (Moon.Property moonProperty : moonPropertyList) {
            moonList.add(new Moon(this, ProjectLocus.WORLD_HALF_WIDTH,
                    ProjectLocus.WORLD_HALF_HEIGHT, moonProperty));
        }

        TiledMap tiledMap = projectLocus.tiledMapList.get(backgroundType);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, ProjectLocus.TILED_MAP_SCALE);

        barBackgroundTexture = projectLocus.uiTextureAtlas.findRegion("barBackground");
        barForegroundTexture = projectLocus.uiTextureAtlas.findRegion("barForeground");

        inputController = new InputController(player);
        Gdx.input.setInputProcessor(inputController);

        // Debugging
        box2DDebugRenderer = new Box2DDebugRenderer();
        fpsCamera = new OrthographicCamera(ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        fpsFont = new BitmapFont();
        fpsFont.getData().setScale(0.25f);
        fpsFont.setColor(Color.YELLOW);

    }

    public void update(float delta) {

        while (destroyEntityStack.size() > 0) {
            destroyEntityStack.pop().destroy();
        }

        while (destroyBulletStack.size() > 0) {
            destroyBulletStack.pop().destroy();
        }

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

        for (Entity entity : entityList) {
            entity.update();
            planet.applyGravitationalForce(entity);
            for (Moon moon : moonList) {
                moon.applyGravitationalForce(entity);
            }
        }

        for (Bullet bullet : bulletList) {
            bullet.update();
        }

        world.step(ProjectLocus.FPS, ProjectLocus.VELOCITY_ITERATIONS,
                ProjectLocus.POSITION_ITERATIONS);

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

        for (Entity entity : entityList) {
            entity.draw(spriteBatch, camera.frustum);
        }

        for (Bullet bullet : bulletList) {
            bullet.draw(spriteBatch, camera.frustum);
        }

        spriteBatch.end();

        // Debugging
//        box2DDebugRenderer.render(world, camera.combined);

        spriteBatch.setProjectionMatrix(fpsCamera.combined);
        spriteBatch.begin();
        fpsFont.draw(spriteBatch, String.valueOf(Gdx.graphics.getFramesPerSecond()),
                ProjectLocus.worldCameraWidth - 5f, ProjectLocus.worldCameraHeight - 2f);
        spriteBatch.end();

    }

    public void resize() {
        camera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
        fpsCamera.setToOrtho(false, ProjectLocus.worldCameraWidth, ProjectLocus.worldCameraHeight);
    }

//    public boolean isInLevel(Vector2 positionUI) {
//        return LEVEL_CIRCLE.contains(positionUI);
//    }

    @Override
    public void dispose() {
        timer.clear();
        world.dispose();
        box2DDebugRenderer.dispose();
    }
}
