package com.locus.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.sprites.bullets.Bullet;
import com.locus.game.tools.BodyEditorLoader;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Divya Mamgai on 9/23/2016.
 * EntityLoader
 */

public class EntityLoader implements Disposable {

    private static BodyEditorLoader physicsLoader;
    private static Entity.Type[] entityTypeArray = Entity.Type.values();
    private static Bullet.Type[] bulletTypeArray = Bullet.Type.values();

    public static class Definition implements Disposable {

        public Entity.Type type;
        int secondaryType;
        String path;
        BodyDef bodyDef;
        FixtureDef fixtureDef;
        TextureRegion textureRegion;
        public float width, height, halfWidth, halfHeight, maxSpeed, maxHealth;
        public int maxDamage;
        float radius, mass, gravitationalMass, thrustSpeed, rotationSpeed, maxSpeed2,
                orbitalVelocity;
        Vector2 bodyOrigin;
        public HashMap<Bullet.Type, ArrayList<Vector2>> weaponPositionMap;
        CircleShape circleShape;

        Definition(ProjectLocus projectLocus, Entity.Type type, int secondaryType, JsonValue entityJson) {

            this.type = type;
            this.secondaryType = secondaryType;

            path = type.toString() + "/" + String.valueOf(secondaryType);

            bodyDef = new BodyDef();
            bodyDef.bullet = false;

            fixtureDef = new FixtureDef();
            fixtureDef.isSensor = false;
            fixtureDef.density = entityJson.getFloat("density");
            fixtureDef.friction = entityJson.getFloat("friction");
            fixtureDef.restitution = entityJson.getFloat("restitution");

            switch (type) {
                case Ship:

                    bodyDef.type = BodyDef.BodyType.DynamicBody;

                    fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_SHIP;
                    fixtureDef.filter.maskBits = CollisionDetector.MASK_SHIP;

                    weaponPositionMap = new HashMap<Bullet.Type, ArrayList<Vector2>>();
                    JsonValue weaponPositionJsonArray = entityJson.get("weaponPosition");
                    Bullet.Type bulletType;
                    for (JsonValue weaponPositionJson : weaponPositionJsonArray.iterator()) {
                        bulletType = bulletTypeArray[weaponPositionJson.getInt("bulletType")];
                        if (!weaponPositionMap.containsKey(bulletType)) {
                            weaponPositionMap.put(bulletType, new ArrayList<Vector2>());
                        }
                        weaponPositionMap.get(bulletType).add(new Vector2(
                                weaponPositionJson.getFloat("x"),
                                weaponPositionJson.getFloat("y")));
                        maxDamage += projectLocus.bulletLoader.get(bulletType).damage;
                    }

                    width = entityJson.getFloat("width");
                    height = entityJson.getFloat("height");

                    thrustSpeed = entityJson.getFloat("thrustSpeed");
                    rotationSpeed = entityJson.getFloat("rotationSpeed");
                    maxSpeed = entityJson.getFloat("maxSpeed");
                    maxSpeed2 = maxSpeed * maxSpeed;
                    maxHealth = entityJson.getFloat("maxHealth");

                    bodyOrigin = physicsLoader.getOrigin(path, width).cpy();

                    textureRegion = projectLocus.shipTextureAtlas
                            .findRegion(String.valueOf(secondaryType));

                    break;
                case Planet:
                case Moon:

                    bodyDef.type = BodyDef.BodyType.KinematicBody;
                    bodyDef.angularVelocity = entityJson.getFloat("angularVelocity");

                    radius = entityJson.getFloat("radius");

                    if (type == Entity.Type.Planet) {

                        fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_PLANET;
                        fixtureDef.filter.maskBits = CollisionDetector.MASK_PLANET;

                        textureRegion = projectLocus.planetTextureAtlas
                                .findRegion(String.valueOf(secondaryType));

                    } else {

                        fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_MOON;
                        fixtureDef.filter.maskBits = CollisionDetector.MASK_MOON;

                        orbitalVelocity = entityJson.getFloat("orbitalVelocity");

                        textureRegion = projectLocus
                                .moonTextureAtlas.findRegion(String.valueOf(secondaryType));

                    }

                    circleShape = new CircleShape();
                    circleShape.setRadius(radius);
                    fixtureDef.shape = circleShape;

                    width = height = radius * 2;

                    bodyOrigin = new Vector2(radius, radius);

                    break;
                case Asteroid:

                    fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_ASTEROID;
                    fixtureDef.filter.maskBits = CollisionDetector.MASK_ASTEROID;

                    bodyDef.type = BodyDef.BodyType.DynamicBody;
                    bodyDef.angularVelocity = entityJson.getFloat("angularVelocity");

                    width = entityJson.getFloat("width");
                    height = entityJson.getFloat("height");

                    bodyOrigin = physicsLoader.getOrigin(path, width).cpy();

                    textureRegion = projectLocus.asteroidTextureAtlas
                            .findRegion(String.valueOf(secondaryType));
                    
                    maxHealth = entityJson.getFloat("maxHealth");

                    break;
                default:
                    Gdx.app.log("EntityLoader.Definition", "Type is invalid for - " + path);
            }
            mass = entityJson.getFloat("mass");
            gravitationalMass = mass * ProjectLocus.GRAVITATIONAL_CONSTANT;
            halfWidth = width / 2f;
            halfHeight = height / 2f;
        }

        void attachFixture(Body body) {
            switch (type) {
                case Planet:
                case Moon:
                    body.createFixture(fixtureDef);
                    break;
                default:
                    physicsLoader.attachFixture(body, path, fixtureDef, width);
            }
        }

        @Override
        public void dispose() {
            if (circleShape != null)
                circleShape.dispose();
        }

    }

    private HashMap<Entity.Type, ArrayList<Definition>> definitionMap;

    public EntityLoader(ProjectLocus projectLocus) {

        physicsLoader = new BodyEditorLoader(
                Gdx.files.internal("sprites/entities/EntityPhysicsDefinition.json"));

        definitionMap = new HashMap<Entity.Type, ArrayList<Definition>>();

        JsonValue entityJsonArray = new JsonReader()
                .parse(Gdx.files.internal("sprites/entities/EntityDefinition.json").readString())
                .get("entities");

        Entity.Type entityType;
        int secondaryType;

        for (JsonValue entityJson : entityJsonArray.iterator()) {

            entityType = entityTypeArray[entityJson.getInt("type")];
            secondaryType = entityJson.getInt("secondaryType");

            if (!definitionMap.containsKey(entityType)) {
                definitionMap.put(entityType, new ArrayList<Definition>());
            }

            definitionMap.get(entityType).add(
                    new Definition(projectLocus, entityType, secondaryType, entityJson));

        }

    }

    public Definition get(Entity.Type type, int secondaryType) {
        return definitionMap.get(type).get(secondaryType);
    }

    public Definition getShip(int shipType) {
        return get(Entity.Type.Ship, shipType);
    }

    public Definition getPlanet(int planetType) {
        return get(Entity.Type.Planet, planetType);
    }

    public Definition getMoon(int moonType) {
        return get(Entity.Type.Moon, moonType);
    }

    @Override
    public void dispose() {
        for (Entity.Type entityType : entityTypeArray) {
            for (Definition definition : definitionMap.get(entityType)) {
                definition.dispose();
            }
        }
    }

}
