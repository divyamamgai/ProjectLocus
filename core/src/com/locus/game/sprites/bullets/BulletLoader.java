package com.locus.game.sprites.bullets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.locus.game.Main;
import com.locus.game.sprites.CollisionDetector;
import com.locus.game.tools.BodyEditorLoader;

import java.util.HashMap;

/**
 * Created by Divya Mamgai on 9/20/2016.
 * BulletLoader
 */

public class BulletLoader implements Disposable {

    private static BodyEditorLoader physicsLoader;

    public class Definition implements Disposable {

        public Bullet.Type type;
        BodyDef bodyDef;
        FixtureDef fixtureDef;
        Texture texture;
        float width, halfWidth, height, halfHeight, speed;
        Vector2 bodyOrigin;
        int life;
        public int damage;

        Definition(Bullet.Type type, JsonValue bulletJson) {
            this.type = type;
            String typeString = type.toString();
            bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.bullet = true;
            fixtureDef = new FixtureDef();
            fixtureDef.isSensor = false;
            fixtureDef.friction = 0f;
            fixtureDef.restitution = 0f;
            // Bullets do not collide with each other.
            fixtureDef.filter.categoryBits = CollisionDetector.CATEGORY_BULLET;
            fixtureDef.filter.maskBits = CollisionDetector.MASK_BULLET;
            texture = new Texture("sprites/bullets/" + typeString + ".png");
            fixtureDef.density = bulletJson.getFloat("density");
            width = bulletJson.getFloat("width");
            halfWidth = width / 2f;
            bodyOrigin = physicsLoader.getOrigin(typeString, width);
            height = bulletJson.getFloat("height");
            halfHeight = height / 2f;
            speed = bulletJson.getFloat("speed");
            // Set the life of the Bullets till they can travel across the World.
            life = (int) Math.ceil(Main.WORLD_DIAGONAL / speed);
            damage = bulletJson.getInt("damage");
        }

        void attachFixture(Body body) {
            physicsLoader.attachFixture(body, type.toString(), fixtureDef, width);
        }

        @Override
        public void dispose() {
            texture.dispose();
        }

    }

    private HashMap<Bullet.Type, Definition> definitionMap;

    public BulletLoader() {
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
            definitionMap.put(bulletType, new Definition(bulletType, bulletJson));
        }
    }

    Definition get(Bullet.Type type) {
        return definitionMap.get(type);
    }

    @Override
    public void dispose() {
        for (Bullet.Type bulletType : Bullet.Type.values()) {
            definitionMap.get(bulletType).dispose();
        }
    }

}
