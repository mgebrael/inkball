package inkball;

import processing.core.PImage;

/**
 * Ball objects that can move and bounce off walls and lines.
 */
public class Ball implements Entity {
    private float x;
    private float y;
    public double i;
    public double j;

    private int radius;
    private int size;
    public String type = "ball";

    public Ball(int x, int y, int c) {
        this.x = (int) (App.CELLSIZE*(x+0.5));
        this.y = (int) (App.CELLSIZE*(y+0.5) + App.TOPBAR);

        this.i = App.random.nextInt(2)*4 - 2; // 2 or -2
        this.j = App.random.nextInt(2)*4 - 2; // 2 or -2

        this.radius = 12;
        this.size = 24;
        this.type += c;
    }

    public Ball(int x, int y, String colour) {
        this(0, 0, 0);
        this.x = (App.CELLSIZE*(x+1));
        this.y = (App.CELLSIZE*(y+1));
        
        if (colour.equals("orange")) this.type = "ball1";
        if (colour.equals("blue")) this.type = "ball2";
        if (colour.equals("green")) this.type = "ball3";
        if (colour.equals("yellow")) this.type = "ball4";
    }

    public void draw(App app) {
        PImage ball = app.getSprite(type);
        app.imageMode(App.CENTER);
        app.image(ball, x, y, size, size);
        app.imageMode(App.CORNER);
    }

    public int getX() {
        return (int) this.x;
    }

    public int getY() {
        return (int) this.y;
    }

    public int getRadius() {
        return this.radius;
    }

    public String getColour() {
        if (this.type.equals("ball1")) return "orange";
        if (this.type.equals("ball2")) return "blue";
        if (this.type.equals("ball3")) return "green";
        if (this.type.equals("ball4")) return "yellow";
        return "grey";
    }

    public void changeColour(int c) {
        if (c != 0) 
            this.type = "ball"+c;
    }

    /**
     * Changes the size of the ball depending on it's distance from a hole.
     */
    public void changeSize(double distance) {
        this.size = (int) (distance * 0.75);
    }

    /**
     * Shift a ball to the left, for use in the topbar.
     */
    public void shift() {
        this.x -= 1;
    }

    /**
     * Set a specific location for the ball.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns true if the ball is travelling in the direction specified.
     */
    public boolean isDirected(String direction) {
        if (direction.equals("EAST") || direction.equals("NORTHEAST_SIDE") || direction.equals("SOUTHEAST_SIDE"))
            return this.i > 0;
        if (direction.equals("SOUTH") || direction.equals("SOUTHEAST_TOP") || direction.equals("SOUTHWEST_TOP"))
            return this.j > 0;
        if (direction.equals("NORTH") || direction.equals("NORTHEAST_BOTTOM") || direction.equals("NORTHWEST_BOTTOM"))
            return this.j < 0;
        if (direction.equals("WEST") || direction.equals("NORTHWEST_SIDE") || direction.equals("SOUTHWEST_SIDE"))
            return this.i < 0;

        return false;
    }

    /**
     * Move the ball according to it's (i, j) velocity vector.
     */
    public void move(Level level) {
        this.x += this.i;
        this.y += this.j;

        // Ball cannot exit the game board
        if (this.x - radius <= 0 || this.x + radius >= App.WIDTH) {
            this.reflectOff(new Segment(0, 0, 0, 1));
        }
        else if (this.y - radius <= App.TOPBAR || this.y + radius >= App.HEIGHT) {
            this.reflectOff(new Segment(0, 0, 1, 0));
        }

        // Collide with walls in the level
        this.hitWalls(level);
    }

    /**
     * Reflect the ball off the walls when a collision is detected.
     */
    public void hitWalls(Level level) {
        Tile occupiedTile = level.grid[(this.getY()-App.TOPBAR)/App.CELLSIZE][this.getX()/App.CELLSIZE];
       
        for (AdjacentWalls nextWall : AdjacentWalls.values()) {
            Tile adjacentWall = nextWall.getWall(occupiedTile);

            if (adjacentWall == null)
                continue;

            Segment hitbox = nextWall.getHitbox(occupiedTile);

            if (this.isDirected(nextWall.name()) && hitbox.collidesWith(this.getX(), this.getY(), this.getRadius())) {
                if (adjacentWall.isBrick()) {
                    adjacentWall.damage(this);
                }
                this.reflectOff(hitbox);
                this.changeColour(adjacentWall.getColour());
                break;
            }
        }
    }

    /**
     * Determine the new (i, j) velocity vector of the ball after colliding with a wall/line.
     */
    public void reflectOff(Segment segment) {
        int dx = segment.getX2() - segment.getX1();
        int dy = segment.getY2() - segment.getY1();

        double angle = Math.max(Math.atan2(dx, -dy), Math.atan2(-dx, dy));
        double direction = Math.atan2(this.j, this.i);
        if (direction - angle > -Math.PI/2 && direction - angle <= Math.PI/2) {
            angle -= Math.PI;
        }

        double normalX = Math.cos(angle);
        double normalY = Math.sin(angle);
        double scalar = 2 * (this.i * normalX + this.j * normalY);

        this.i -= normalX * scalar;
        this.j -= normalY * scalar;
    }

    /**
     * Spawn a ball from a random spawner in the level.
     */
    public void spawn(Level level) {
        int rand = App.random.nextInt(level.spawners.size());
        Spawner spawner = level.spawners.get(rand);
        this.x = (int) (App.CELLSIZE*(spawner.getX()+0.5));
        this.y = (int) (App.CELLSIZE*(spawner.getY()+0.5) + App.TOPBAR);
        level.balls.add(this);
    }
}
