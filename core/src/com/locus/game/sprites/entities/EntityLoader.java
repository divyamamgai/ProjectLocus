package com.locus.game.sprites.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.locus.game.Main;
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

    public class Definition implements Disposable {

        public Entity.Type type;
        int secondaryType;
        String path;
        BodyDef bodyDef;
        FixtureDef fixtureDef;
        Texture texture;
        float width, halfWidth, height, halfHeight, radius, mass, gravitationalMass,
                health, orbitalVelocity;
        Vector2 bodyOrigin;
        HashMap<Bullet.Type, ArrayList<Vector2>> weaponPositionMap;
        CircleShape circleShape;

        Definition(Entity.Type type, int secondaryType, JsonValue entityJson) {

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
                    }

                    width = entityJson.getFloat("width");
                    height = entityJson.getFloat("height");

                    health = entityJson.getFloat("health");

                    bodyOrigin = physicsLoader.getOrigin(path, width).cpy();

                    break;
                case Planet:
                case Moon:

                    bodyDef.type = BodyDef.BodyType.KinematicBody;
                    bodyDef.angularVelocity = entityJson.getFloat("angularVelocity");

                    radius = entityJson.getFloat("radius");

                    if (type == Entity.Type.Planet) {

                        fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_PLANET;
                        fixtureDef.filter.maskBits = CollisionDetector.MASK_PLANET;

                    } else {

                        fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_MOON;
                        fixtureDef.filter.maskBits = CollisionDetector.MASK_MOON;

                        orbitalVelocity = entityJson.getFloat("orbitalVelocity");

                    }

                    circleShape = new CircleShape();
                    circleShape.setRadius(radius);
                    fixtureDef.shape = circleShape;

                    width = height = radius * 2;

                    bodyOrigin = new Vector2(radius, radius);

                    break;
                case Asteroid:

                    bodyDef.type = BodyDef.BodyType.DynamicBody;

                    fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_ASTEROID;
                    fixtureDef.filter.maskBits = CollisionDetector.MASK_ASTEROID;

                    width = entityJson.getFloat("width");
                    height = entityJson.getFloat("height");

                    health = entityJson.getFloat("health");

                    bodyOrigin = physicsLoader.getOrigin(path, width);

                    break;
                default:
                    Gdx.app.log("EntityLoader.Definition", "Type is invalid for - " + path);
            }
            texture = new Texture("sprites/entities/" + path + ".png");
            mass = entityJson.getFloat("mass");
            gravitationalMass = mass * Main.GRAVITATIONAL_CONSTANT;
            halfWidth = width / 2f;
            halfHeight = height / 2f;
        }

        void attachFixture(Body body) {
            switch (type) {
                case Planet:
                case Moon:
                    body.createFixture(fixtureDef);
                    circleShape.dispose();
                    break;
                default:
                    physicsLoader.attachFixture(body, path, fixtureDef, width);
            }
        }

        @Override
        public void dispose() {
            texture.dispose();
        }

    }

    private HashMap<Entity.Type, ArrayList<Definition>> definitionMap;

    public EntityLoader() {
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
                    new Definition(entityType, secondaryType, entityJson));
        }
    }

    public Definition get(Entity.Type type, int secondaryType) {
        return definitionMap.get(type).get(secondaryType);
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
