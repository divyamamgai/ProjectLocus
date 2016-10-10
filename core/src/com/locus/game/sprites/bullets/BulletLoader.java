package com.locus.game.sprites.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.locus.game.ProjectLocus;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.tools.BodyEditorLoader;

import java.util.HashMap;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * BulletLoader
 */

public class BulletLoader {

    private static BodyEditorLoader physicsLoader;

    public static class Definition {

        public Bullet.Type type;
        BodyDef bodyDef;
        FixtureDef fixtureDef;
        TextureRegion textureRegion;
        float width, halfWidth, height, halfHeight, speed;
        Vector2 bodyOrigin;
        short life;
        public short damage, fireRate;

        Definition(TextureAtlas bulletTextureAtlas, Bullet.Type type, JsonValue bulletJson) {

            this.type = type;
            String typeString = type.toString();

            bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.bullet = true;
            bodyDef.fixedRotation = true;

            fixtureDef = new FixtureDef();
            fixtureDef.isSensor = false;
            fixtureDef.friction = 0f;
            fixtureDef.restitution = 0f;
            fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_BULLET;
            fixtureDef.filter.maskBits = CollisionDetector.MASK_BULLET;
            fixtureDef.density = bulletJson.getFloat("density");

            textureRegion = bulletTextureAtlas.findRegion(typeString);

            width = bulletJson.getFloat("width");
            halfWidth = width / 2f;

            height = bulletJson.getFloat("height");
            halfHeight = height / 2f;

            bodyOrigin = physicsLoader.getOrigin(typeString, width).cpy();

            speed = bulletJson.getFloat("speed");

            // Set the life of the Bullets till they can travel across the World.
            life = (short) Math.ceil(ProjectLocus.WORLD_DIAGONAL / speed);

            damage = bulletJson.getShort("damage");
            fireRate = bulletJson.getShort("fireRate");

        }

        void attachFixture(Body body) {
            physicsLoader.attachFixture(body, type.toString(), fixtureDef, width);
        }

    }

    private HashMap<Bullet.Type, Definition> definitionMap;

    public BulletLoader(TextureAtlas bulletTextureAtlas) {

        physicsLoader = new BodyEditorLoader(
                Gdx.files.internal("sprites/bullets/BulletPhysicsDefinition.json"));

        definitionMap = new HashMap<Bullet.Type, Definition>();

        JsonValue bulletJsonArray = new JsonReader()
                .parse(Gdx.files.internal("sprites/bullets/BulletDefinition.json").readString())
                .get("bullets");

        Bullet.Type[] bulletTypeArray = Bullet.Type.values();
        Bullet.Type bulletType;

        for (JsonValue bulletJson : bulletJsonArray.iterator()) {
            bulletType = bulletTypeArray[bulletJson.getInt("type")];
            definitionMap.put(bulletType, new Definition(bulletTextureAtlas, bulletType,
                    bulletJson));
        }

    }

    public Definition get(Bullet.Type type) {
        return definitionMap.get(type);
    }

}
