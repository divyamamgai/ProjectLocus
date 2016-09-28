package com.locus.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.esotericsoftware.kryonet.Server;
import com.locus.game.Main;
import com.locus.game.levels.Level;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.sprites.bullets.BulletLoader;
import com.locus.game.sprites.entities.Entity;
import com.locus.game.sprites.entities.EntityLoader;
import com.locus.game.sprites.entities.Moon;
import com.locus.game.sprites.entities.Planet;
import com.locus.game.sprites.entities.Player;
import com.locus.game.sprites.entities.Ship;
import com.locus.game.tools.InputController;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Divya Mamgai on 9/6/2016.
 * Multi Ship Play Screen
 */
public class PlayScreen implements Screen {

    private static final float CAMERA_FOLLOW_SPEED = 2f;

    private static float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
    private static float cameraWidth = 100f;
    private static float cameraHeight = 100f;

    private OrthographicCamera camera;
    private InputController inputController;
    public Main game;
    public World gameWorld;
    public BulletLoader bulletLoader;
    public EntityLoader entityLoader;
    public Timer timer;

    private Player player;
    private Level level;
    public ArrayList<Entity> entityList;
    public Stack<Entity> destroyEntityStack;
    public ArrayList<Bullet> bulletList;
    public Stack<Bullet> destroyBulletStack;

    public Texture healthBackgroundTexture, healthForegroundTexture;

    // Debugging
    private Box2DDebugRenderer box2DDebugRenderer;
    private ShapeRenderer shapeRenderer;

    public PlayScreen(Main game) {

        Server server = new Server();

        this.game = game;

        camera = new OrthographicCamera(cameraWidth * aspectRatio, cameraHeight);
        camera.position.set(Main.HALF_WORLD_WIDTH, Main.HALF_WORLD_HEIGHT, 0);
        camera.update();

//        camera.zoom += 1f;

        // Box2D Variables
        gameWorld = new World(Main.GRAVITY, true);
        gameWorld.setContactListener(new CollisionDetector());

        // Can load BulletLoader and EntityLoader only after World has been loaded.
        // This is because without Box2D initialization we cannot get access to the Shapes
        // and its other sub-classes.
        bulletLoader = new BulletLoader();
        entityLoader = new EntityLoader();

        entityList = new ArrayList<Entity>();
        destroyEntityStack = new Stack<Entity>();
        bulletList = new ArrayList<Bullet>();
        destroyBulletStack = new Stack<Bullet>();

        player = new Player(this, Ship.Type.Human, Main.HALF_WORLD_WIDTH + 200f, Main.HALF_WORLD_HEIGHT);

        entityList.add(new Ship(this, Ship.Type.Alien, Main.HALF_WORLD_WIDTH - 200f, Main.HALF_WORLD_HEIGHT));

        level = new Level(this, Planet.Type.Gas, Moon.Type.Organic, MathUtils.random(1, 8));

        inputController = new InputController(player);

        Gdx.input.setInputProcessor(inputController);

        timer = new Timer();

        healthBackgroundTexture = new Texture(Gdx.files.internal("ui/health/background.png"));
        healthForegroundTexture = new Texture(Gdx.files.internal("ui/health/foreground.png"));

        // Debugging
        box2DDebugRenderer = new Box2DDebugRenderer();
        shapeRenderer = new ShapeRenderer();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        inputController.update();
        player.handleInput();

        gameWorld.step(Main.FPS, Main.VELOCITY_ITERATIONS, Main.POSITION_ITERATIONS);

        level.update();

        player.update();
        level.planet.applyGravitationalForce(player);

        camera.position.x += (player.getX() - camera.position.x) * CAMERA_FOLLOW_SPEED * delta;
        camera.position.y += (player.getY() - camera.position.y) * CAMERA_FOLLOW_SPEED * delta;

        camera.update();

        game.spriteBatch.setProjectionMatrix(camera.combined);
        game.spriteBatch.begin();

        level.draw(game.spriteBatch, camera.frustum);

        if (level.isInLevel(player.body.getPosition())) {
            player.draw(game.spriteBatch);
            player.drawHealth();
        }

        for (Entity entity : entityList) {
            entity.update();
            level.planet.applyGravitationalForce(entity);
            if (entity.inFrustum(camera.frustum)) {
                entity.draw(game.spriteBatch);
                entity.drawHealth();
            }
        }

        for (Bullet bullet : bulletList) {
            bullet.update();
            if (bullet.inFrustum(camera.frustum)) {
                bullet.draw(game.spriteBatch);
            }
        }

        game.spriteBatch.end();

        while (destroyEntityStack.size() > 0) {
            destroyEntityStack.pop().destroy();
        }

        while (destroyBulletStack.size() > 0) {
            destroyBulletStack.pop().destroy();
        }

        // Debugging
//        box2DDebugRenderer.render(gameWorld, camera.combined);
//
//        shapeRenderer.setProjectionMatrix(camera.combined);
//        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//        for (Vector3 point : camera.frustum.planePoints) {
//            shapeRenderer.circle(point.x, point.y, 1f);
//        }
//        shapeRenderer.end();

    }

    @Override
    public void resize(int width, int height) {
        aspectRatio = (float) width / (float) height;
        camera.setToOrtho(
                false,
                (cameraWidth = (cameraHeight * aspectRatio)),
                cameraHeight);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        gameWorld.dispose();
        box2DDebugRenderer.dispose();
        shapeRenderer.dispose();
    }

}
