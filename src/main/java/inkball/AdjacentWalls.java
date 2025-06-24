package inkball;

/**
 * Enumerated class that locates any walls adjacent to the tile that is occupied by the ball.
 */
public enum AdjacentWalls {
    NORTH(0, -1),
    NORTHEAST_SIDE(1, -1),
    NORTHEAST_BOTTOM(1, -1),
    EAST(1, 0),
    SOUTHEAST_SIDE(1, 1),
    SOUTHEAST_TOP(1, 1),
    SOUTH(0, 1),
    SOUTHWEST_SIDE(-1, 1),
    SOUTHWEST_TOP(-1, 1),
    WEST(-1, 0),
    NORTHWEST_SIDE(-1, -1),
    NORTHWEST_BOTTOM(-1, -1);

    public int xOffset;
    public int yOffset;

    private AdjacentWalls(int x, int y) {
        xOffset = x;
        yOffset = y;
    }

    /**
     * Get the wall adjacent to the tile.
     */
    public Tile getWall(Tile tile) {
        try {
            Tile wall = App.currentLevel.grid[tile.getY()+yOffset][tile.getX()+xOffset];
            if (wall.isWall())
                return wall;
        } catch (ArrayIndexOutOfBoundsException e) {}
        return null;
    }

    /**
     * Return the hitbox of the adjacent wall that collides with the ball.
     */
    public Segment getHitbox(Tile tile) {
        Tile wall = this.getWall(tile);
        int xPosition = wall.getX() * App.CELLSIZE;
        int yPosition = wall.getY() * App.CELLSIZE + App.TOPBAR;

        if (this == EAST || this == NORTHEAST_SIDE || this == SOUTHEAST_SIDE)
            return new Segment(xPosition, yPosition + App.CELLSIZE, xPosition, yPosition);
        if (this == SOUTH || this == SOUTHEAST_TOP || this == SOUTHWEST_TOP)
            return new Segment(xPosition, yPosition, xPosition + App.CELLSIZE, yPosition);
        if (this == NORTH || this == NORTHEAST_BOTTOM || this == NORTHWEST_BOTTOM)
            return new Segment(xPosition + App.CELLSIZE, yPosition + App.CELLSIZE, xPosition, yPosition + App.CELLSIZE);
        if (this == WEST || this == NORTHWEST_SIDE || this == SOUTHWEST_SIDE)
            return new Segment(xPosition + App.CELLSIZE, yPosition, xPosition + App.CELLSIZE, yPosition + App.CELLSIZE);

        return null;
    }
}
