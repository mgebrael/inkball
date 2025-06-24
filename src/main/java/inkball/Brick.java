package inkball;

import processing.core.PImage;

/**
 * Walls that take damage and break upon being hit by balls of the same colour.
 */
public class Brick extends Wall {
    public int damageState;

    public Brick(int x, int y, int c) {
        super(x, y, c);
        this.type = "brick"+c;
        damageState = 3;
    }

    @Override
    public boolean isWall() {
        return damageState > 0;
    }

    @Override
    public boolean isBrick() {
        return true;
    }

    @Override
    public void damage(Ball ball) {
        if (!this.colourMatches(ball))
            return;
        damageState--;
    }

    @Override
    public void draw(App app) {
        PImage tile = app.getSprite(type);
        app.image(app.getSprite("tile"), getX()*App.CELLSIZE, getY()*App.CELLSIZE+App.TOPBAR);
        app.tint(255, (int) 85.4*damageState);
        app.image(tile, getX()*App.CELLSIZE, getY()*App.CELLSIZE+App.TOPBAR);
        app.tint(255, 256);
    }
}
