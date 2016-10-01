package com.locus.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.locus.game.Main;
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
import java.util.Stack;

/**
 * Created by Divya Mamgai on 9/19/2016.
 * Level Class
 */
public class Level implements Disposable {

    private static final Circle LEVEL_CIRCLE = new Circle(Main.HALF_WORLD_WIDTH,
            Main.HALF_WORLD_HEIGHT, 400f);

    private static final float CAMERA_FOLLOW_SPEED = 2f;

    private static float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
    private static float cameraHeight = 100f;
    private static float cameraHalfHeight = cameraHeight / 2f;
    private static float cameraWidth = cameraHeight * aspectRatio;
    private static float cameraHalfWidth = cameraWidth / 2f;

    private InputController inputController;
    private OrthographicCamera camera;
    public World world;

    public BulletLoader bulletLoader;
    public EntityLoader entityLoader;
    public ArrayList<Entity> entityList;
    public Stack<Entity> destroyEntityStack;
    public ArrayList<Bullet> bulletList;
    public Stack<Bullet> destroyBulletStack;
    private Ship player;
    private Planet planet;
    public ArrayList<Moon> moonList;
    private TiledMapRenderer tiledMapRenderer;
    public Texture healthBackgroundTexture, healthForegroundTexture;

    // Debugging
    private Box2DDebugRenderer box2DDebugRenderer;
    private OrthographicCamera fpsCamera;
    private BitmapFont fpsFont;

    public Level(Planet.Type planetType, ArrayList<Moon.Property> moonPropertyList,
                 int backgroundType) {

        camera = new OrthographicCamera(cameraWidth, cameraHeight);

        // Box2D Variables
        world = new World(Main.GRAVITY, true);
        world.setContactListener(new CollisionDetector());

        // Can load BulletLoader and EntityLoader only after World has been loaded.
        // This is because without Box2D initialization we cannot get access to the Shapes
        // and its other sub-classes.
        bulletLoader = new BulletLoader();
        entityLoader = new EntityLoader();

        entityList = new ArrayList<Entity>();
        destroyEntityStack = new Stack<Entity>();
        bulletList = new ArrayList<Bullet>();
        destroyBulletStack = new Stack<Bullet>();

        entityList.add(player = new Ship(this, Ship.Type.Human,
                Main.HALF_WORLD_WIDTH + 250f, Main.HALF_WORLD_HEIGHT));

        planet = new Planet(this, planetType, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT);

        moonList = new ArrayList<Moon>();
        for (Moon.Property moonProperty : moonPropertyList) {
            moonList.add(new Moon(this, Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT,
                    moonProperty));
        }

        TiledMap tiledMap = new TmxMapLoader().load("backgrounds/" + backgroundType + ".tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 0.15f);

        inputController = new InputController(player);
        Gdx.input.setInputProcessor(inputController);

        healthBackgroundTexture = new Texture(Gdx.files.internal("ui/health/background.png"));
        healthForegroundTexture = new Texture(Gdx.files.internal("ui/health/foreground.png"));

        // Debugging
        box2DDebugRenderer = new Box2DDebugRenderer();
        fpsCamera = new OrthographicCamera(cameraWidth, cameraHeight);
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

        world.step(Main.FPS, Main.VELOCITY_ITERATIONS, Main.POSITION_ITERATIONS);

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
                cameraHalfWidth - 5f, cameraHalfHeight - 2f);
        spriteBatch.end();

    }

    public void resize(int width, int height) {
        aspectRatio = (float) width / (float) height;
        camera.setToOrtho(
                false,
                (cameraWidth = (cameraHeight * aspectRatio)),
                cameraHeight);
        cameraHalfWidth = cameraWidth / 2f;
    }

//    public boolean isInLevel(Vector2 position) {
//        return LEVEL_CIRCLE.contains(position);
//    }

    @Override
    public void dispose() {
        world.dispose();

        healthForegroundTexture.dispose();
        healthBackgroundTexture.dispose();

        box2DDebugRenderer.dispose();
    }
}
