package inkball;

/**
 * Solid objects that reflect and change the colour of balls.
 */
public class Wall extends Tile {
    private int colour;

    public Wall(int x, int y, int c) {
        super(x, y);
        this.type = "wall"+c;
        colour = c;
    }

    @Override
    public boolean isWall() {
        return true;
    }

    @Override
    public int getColour() {
        return colour;
    }

    /**
     * Return true if the colour of the ball matches the wall.
     */
    public boolean colourMatches(Ball ball) {
        if (ball.getColour().equals("grey")) return true;
        if (this.getColour() == 1) return ball.getColour().equals("orange");
        if (this.getColour() == 2) return ball.getColour().equals("blue");
        if (this.getColour() == 3) return ball.getColour().equals("green");
        if (this.getColour() == 4) return ball.getColour().equals("yellow");
        return true;
    }
}
