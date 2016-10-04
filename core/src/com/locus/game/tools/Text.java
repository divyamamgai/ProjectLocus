package com.locus.game.tools;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Text
 */

public class Text {

    private static GlyphLayout glyphLayout = new GlyphLayout();

    private String text;
    private float width, halfWidth, height, halfHeight;
    private BitmapFont font;

    Text() {

    }

    public Text(BitmapFont font, String text) {

        this.font = font;

        setText(text);

    }

    public float getWidth() {
        return width;
    }

    public float getHalfWidth() {
        return halfWidth;
    }

    public float getHeight() {
        return height;
    }

    public float getHalfHeight() {
        return halfHeight;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {

        this.text = text;

        glyphLayout.setText(font, text);
        width = glyphLayout.width;
        halfWidth = width / 2f;
        height = glyphLayout.height;
        halfHeight = height / 2f;

    }

    public BitmapFont getFont() {
        return font;
    }

    public void setFont(BitmapFont font) {

        this.font = font;

        setText(text);

    }

    public void draw(SpriteBatch spriteBatch, float x, float y) {
        font.draw(spriteBatch, text, x, y);
    }

}
