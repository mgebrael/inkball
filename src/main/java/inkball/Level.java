package inkball;

import processing.core.PImage;
import processing.data.JSONObject;
import java.util.*;
import java.io.*;

/**
 * The class containing objects specific to the level being played.
 */
public class Level {
    public Tile[][] grid;
    public LinkedList<Ball> balls = new LinkedList<>();
    public LinkedList<Ball> topbarBalls = new LinkedList<>();

    public LinkedList<Line> drawnLines = new LinkedList<>();
    public LinkedList<Spawner> spawners = new LinkedList<>();
    public LinkedList<Hole> holes = new LinkedList<>();

    private int spawnInterval;
    private int spawnTimer = 9;
    private boolean shiftTopbarBalls;

    private int score;
    private JSONObject scoreIncrease;
    private JSONObject scoreDecrease;
    private double scoreIncreaseModifier;
    private double scoreDecreaseModifier;

    private int timer;
    private boolean paused;
    private boolean lost;

    public Level(App app, JSONObject config, JSONObject layout) {
        String filename = layout.get("layout").toString();
        File f = new File(filename);

        grid = new Tile[App.BOARD_HEIGHT][App.BOARD_WIDTH];
        tileGrid(f);

        addBalls(layout.getJSONArray("balls").getStringArray());

        spawnInterval = layout.getInt("spawn_interval");
        timer = layout.getInt("time");

        score = app.getScore();
        scoreIncrease = config.getJSONObject("score_increase_from_hole_capture");
        scoreDecrease = config.getJSONObject("score_decrease_from_wrong_hole");
        scoreIncreaseModifier = layout.getDouble("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier = layout.getDouble("score_decrease_from_wrong_hole_modifier");
    }

    /**
     * Returns true if the tobar is in the process of shifting the topbar balls.
     */
    public boolean topbarBallsShifting() {
        return shiftTopbarBalls;
    }

    /**
     * Stops the topbar balls from shifting.
     */
    public void stopShifting() {
        shiftTopbarBalls = false;
    }

    /**
     * Stop the timer and freeze moving balls.
     */
    public void pause() {
        paused = !paused;
    }

    /**
     * Returns true if the level is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns true if the timer runs out.
     */
    public boolean isLost() {
        return lost;
    }

    /**
     * Method for adding tiles to the grid, as specified by the config file.
     */
    public void tileGrid(File f) {
        try {
            Scanner scan = new Scanner(f);

            for (int i = 0; i < grid.length; i++) {
                String row = "";
                if (scan.hasNextLine())
                    row = scan.nextLine();

                row += "XXXXXXXXXXXXXXXXXX";
                constructRow(row, i);
            }
            scan.close();
            
        } catch (FileNotFoundException e) {}
    }

    /**
     * Initialize balls that will be used for the level, as specified by the config file.
     */
    public void addBalls(String[] colours) {
        int x = 0;
        for (String ballColour : colours) {
            topbarBalls.add(new Ball(x++, 0, ballColour));
        }
    }

    /**
     * Method for creating a row of tiles for the level.
     */
    public void constructRow(String row, int i) {
        boolean skipNext = false;

        for (int j = 0; j < grid[i].length; j++) {
            char spot = row.charAt(j);

            if (spot == 'X') {
                grid[i][j] = new Wall(j, i, 0);
            }
            else if ((spot == '1' || spot == '2' || spot == '3' || spot == '4') && !skipNext) {
                grid[i][j] = new Wall(j, i, spot - '0'); // gives the int value for spot
            }
            else if (spot == '#') {
                grid[i][j] = new Brick(j, i, 0);
            }
            else if (spot == '6' || spot == '7' || spot == '8' || spot == '9') {
                grid[i][j] = new Brick(j, i, spot - '5');
            }
            else if (spot == 'S') {
                spawners.add(new Spawner(j, i));
                grid[i][j] = spawners.getLast();
            }
            else if (spot == 'B') {
                grid[i][j] = new Tile(j, i);
                int colour = row.charAt(j+1) - '0';
                balls.add(new Ball(j, i, colour));
                skipNext = true;
                continue;
            }
            else if (spot == 'H') {
                int colour = row.charAt(j+1) - '0';
                holes.add(new Hole(j, i, colour));
                grid[i][j] = holes.getLast();
                skipNext = true;
                continue;
            }
            else
                grid[i][j] = new Tile(j, i);

            if (skipNext)
                skipNext = false;
        }
    }

    /**
     * Display time remaining in the topbar.
     */
    public void displayTimer(App app) {
        app.text("Time: "+timer, 440, App.TOPBAR-8);

        if (paused || remainingBalls() == 0) return;

        if (timer == 0) {
            lost = true;
            paused = true;
        }
        else if (app.frameCount % 30 == 0)
            timer--;
    }

    /**
     * Display total score in the topbar.
     */
    public void displayScore(App app) {
        app.text("Score: "+score, 440, App.TOPBAR-36);
    }

    /**
     * Display the cooldown timer for respawning balls.
     */
    public void displaySpawnTimer(App app) {
        float time = 10f * spawnTimer / App.FPS;
        app.text((int)time/10+"."+(int)time%10, 192, 48);

        if (paused) return;

        spawnTimer--;
        if (spawnTimer < 0) {
            spawnBall();
        }
    }

    /**
     * Spawn a ball from a random spawner in the level.
     */
    public void spawnBall() {
        spawnTimer = spawnInterval * App.FPS;
        if (!topbarBalls.isEmpty())
            topbarBalls.removeFirst().spawn(this);
        shiftTopbarBalls = true;
        
    }

    /**
     * Increase/decrease the score depending on the ball/hole colour, and adds ball to respawn queue if necessary.
     */
    public void updateScore(Ball ball, Hole hole) {
        // Capture
        if (hole.colourMatches(ball)) {
            score += scoreIncrease.getInt(ball.getColour()) * scoreIncreaseModifier;
            return;
        }

        // Respawn
        score -= scoreDecrease.getInt(ball.getColour()) * scoreDecreaseModifier;
        Ball respawnBall = new Ball(0, 0, ball.getColour());
        if (!topbarBalls.isEmpty())
            respawnBall.setPosition(topbarBalls.getLast().getX() + App.CELLSIZE, App.CELLSIZE);
        topbarBalls.add(respawnBall);
    }

    /**
     * Initialise score from the total of the previously played levels.
     */
    public void setScore(int total) {
        score = total;
    }

    /**
     * Return cumulative game score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the count of all total balls not yet captured in the level.
     */
    public int remainingBalls() {
        return topbarBalls.size() + balls.size();
    }

    /**
     * Return true if there are no more remaining balls and the time has ended.
     */
    public boolean hasEnded() {
        return remainingBalls() == 0 && timer == 0;
    }

    /**
     * Decrease timer and increase score.
     */
    public void countRemainingTime(App app) {
        timer--;
        score++;
    }
}
