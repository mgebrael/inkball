package inkball;

/**
 * A set of two points. Used as the hitboxes for walls, as well as for drawing lines.
 */
public class Segment {
    private int x1;
    private int y1;
    private int x2;
    private int y2;

    public Segment(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int getX1() {
        return this.x1;
    }

    public int getY1() {
        return this.y1;
    }

    public int getX2() {
        return this.x2;
    }

    public int getY2() {
        return this.y2;
    }

    /**
     * Calculate the distance between two points.
     */
    public double distance(int x1, int y1, int x2, int y2) {
        double dSquared = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        return Math.sqrt(dSquared);
    }

    /**
     * Method for determining if a segment collides with a point/ball.
     */
    public boolean collidesWith(int x, int y, int radius) {
        return distance(x1, y1, x, y) + distance(x2, y2, x, y) <= distance(x1, y1, x2, y2) + radius;
    }
}
