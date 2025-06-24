package inkball;

import processing.core.PImage;

/**
 * An empty space. Each grid space of the level is occupied by a tile.
 */
public class Tile implements Entity {
    private int x;
    private int y;
    public String type = "tile";

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    /**
     * Return true if the tile is a wall, false otherwise.
     */
    public boolean isWall() {
        return false;
    }

    /**
     * Return true if the tile is a brick, false otherwise.
     */
    public boolean isBrick() {
        return false;
    }

    /**
     * Increase the damage taken for a brick.
     */
    public void damage(Ball ball) {}

    public int getColour() {
        return 0;
    }

    public void draw(App app) {
        PImage tile = app.getSprite(type);
        app.image(tile, x*App.CELLSIZE, y*App.CELLSIZE+App.TOPBAR);
    }
}
