package inkball;

/**
 * A larger type of tile that attracts and captures balls.
 */
public class Hole extends Tile {
    private int colour;

    public Hole(int x, int y, int c) {
        super(x, y);
        this.type = "hole"+c;
        colour = c;
    }

    @Override
    public int getColour() {
        return colour;
    }

    public int getCentreX() {
        return (getX() + 1) * App.CELLSIZE;
    }

    public int getCentreY() {
        return (getY() + 1) * App.CELLSIZE + App.TOPBAR;
    }

    /**
     * Attract the ball towards the centre of the hole.
     */
    public void attract(Ball ball) {
        int dx = getCentreX() - ball.getX();
        int dy = getCentreY() - ball.getY();

        double distance = Math.sqrt(dx*dx + dy*dy);
        if (distance > 32) 
            return;

        ball.changeSize(distance);
        
        ball.i += 0.005 * dx;
        ball.j += 0.005 * dy;
    }

    /**
     * Return true if the ball is positioned directly over the centre of the hole.
     */
    public boolean captures(Ball ball) {
        int dx = getCentreX() - ball.getX();
        int dy = getCentreY() - ball.getY();

        return (Math.sqrt(dx*dx + dy*dy) < ball.getRadius());
    }

    /**
     * Return true if the colour of the ball matches the colour of the hole. Grey balls/holes need not match.
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
