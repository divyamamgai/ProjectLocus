package com.locus.game.tools;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Divya Mamgai on 10/3/2016.
 * Text
 */

public class Text {

    private static GlyphLayout glyphLayout = new GlyphLayout();

    private String text;
    private float width, halfWidth, height, halfHeight, x, y;
    private BitmapFont font;
    private BoundingBox textBoundingBox;
    private Vector3 textBBMinimum, textBBMaximum;
    private static final int PADDING = 5;

    Text() {

    }

    public Text(BitmapFont font, String text) {

        this.font = font;
        x = y = 0f;

        textBoundingBox = new BoundingBox();
        textBBMinimum = new Vector3();
        textBBMaximum = new Vector3();

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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getText() {
        return text;
    }

    public BoundingBox getTextBoundingBox() {
        return textBoundingBox;
    }

    private void setTextBoundingBox() {
        textBBMinimum.set(x - PADDING, y + PADDING, 0);
        textBBMaximum.set(x + width + PADDING, y - height - PADDING, 0);
        textBoundingBox.set(textBBMinimum, textBBMaximum);
    }

    public void setText(String text) {

        this.text = text;

        glyphLayout.reset();
        glyphLayout.setText(font, text);
        width = glyphLayout.width;
        halfWidth = width / 2f;
        height = glyphLayout.height;
        halfHeight = height / 2f;

        setTextBoundingBox();

    }

    public void setTextFast(String text) {
        this.text = text;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        setTextBoundingBox();
    }

    public BitmapFont getFont() {
        return font;
    }

    public void setFont(BitmapFont font) {

        this.font = font;

        setText(text);

    }

    public void setFontFast(BitmapFont font) {
        this.font = font;
    }

    public void draw(SpriteBatch spriteBatch, float x, float y) {
        font.draw(spriteBatch, text, x, y);
    }

    public void draw(SpriteBatch spriteBatch) {
        font.draw(spriteBatch, text, x, y);
    }

}
